import Fastify from 'fastify';
import assert from 'node:assert';

// Auth middleware contract test — mirrors backend/src/server.js preHandler.
// Security: there is NO ALLOW_DEV_TOKENS / NODE_ENV=test bypass by design.
// verifyIdToken is mocked (no Firebase credentials needed), but the mock
// genuinely rejects unknown/malformed tokens — it never always-resolves.

const VALID_TOKENS = new Map([
  ['valid-firebase-token-alice', { uid: 'alice123', email: 'alice123@test.local' }],
  ['valid-firebase-token-bob', { uid: 'bob456', email: 'bob456@test.local' }],
]);

// Mock of getAuth().verifyIdToken: resolves only for known-good tokens,
// rejects everything else (including any test-token-* prefix).
async function mockVerifyIdToken(idToken) {
  const user = VALID_TOKENS.get(idToken);
  if (user) return user;
  const err = new Error('Invalid or expired Firebase ID token.');
  err.code = 'auth/invalid-token';
  throw err;
}

async function runTests() {
  console.log('Testing Fastify Auth & Rate Limiting preHandler Hook (mocked verifyIdToken)...');
  console.log('Bypass gate: ALLOW_DEV_TOKENS must be unset ->', process.env.ALLOW_DEV_TOKENS ?? 'unset');

  const testApp = Fastify({ logger: false });

  const rateLimits = new Map();
  const RATE_LIMIT_WINDOW_MS = 60 * 60 * 1000;
  const RATE_LIMIT_MAX_CALLS = 30;

  function checkRateLimit(uid) {
    const now = Date.now();
    const windowStart = now - RATE_LIMIT_WINDOW_MS;
    const timestamps = (rateLimits.get(uid) || []).filter((t) => t > windowStart);

    if (timestamps.length >= RATE_LIMIT_MAX_CALLS) {
      const oldest = timestamps[0];
      const resetSeconds = Math.ceil((oldest + RATE_LIMIT_WINDOW_MS - now) / 1000);
      return { allowed: false, remaining: 0, resetSeconds };
    }

    timestamps.push(now);
    rateLimits.set(uid, timestamps);
    return { allowed: true, remaining: RATE_LIMIT_MAX_CALLS - timestamps.length, resetSeconds: 0 };
  }

  // Mirror of src/server.js preHandler (including test-token- hard reject).
  testApp.addHook('preHandler', async (request, reply) => {
    if (request.url === '/health' || request.url === '/') {
      return;
    }

    const authHeader = request.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return reply.code(401).send({
        error: 'Unauthorized',
        message: 'Missing or invalid Authorization header. A valid Firebase ID token is required.',
      });
    }

    const idToken = authHeader.split('Bearer ')[1].trim();

    // No test-token bypass in ANY env — must 401 before verifyIdToken.
    if (idToken.startsWith('test-token-')) {
      return reply.code(401).send({
        error: 'Unauthorized',
        message: 'Invalid or expired Firebase ID token.',
        details: 'Test tokens are never accepted. A valid Firebase ID token is required.',
      });
    }

    try {
      const decodedToken = await mockVerifyIdToken(idToken);
      request.user = decodedToken;
      const rateStatus = checkRateLimit(decodedToken.uid);
      reply.header('X-RateLimit-Limit', RATE_LIMIT_MAX_CALLS);
      reply.header('X-RateLimit-Remaining', rateStatus.remaining);
      if (!rateStatus.allowed) {
        reply.header('Retry-After', rateStatus.resetSeconds);
        return reply.code(429).send({
          error: 'Too Many Requests',
          message: `Hourly AI limit reached. Please try again in ${rateStatus.resetSeconds} seconds.`,
        });
      }
      return;
    } catch (err) {
      return reply.code(401).send({
        error: 'Unauthorized',
        message: 'Invalid or expired Firebase ID token.',
        details: err.message,
        code: err.code,
      });
    }
  });

  testApp.get('/health', async () => ({ status: 'ok' }));
  testApp.post('/ai/analyze-text', async (request) => ({
    success: true,
    user: request.user,
  }));

  // Test 1: GET /health allows unauthenticated access
  const resHealth = await testApp.inject({ method: 'GET', url: '/health' });
  assert.strictEqual(resHealth.statusCode, 200, 'GET /health must return 200');
  console.log('✓ Test 1: /health is accessible without authentication');

  // Test 2: POST without header is rejected with 401
  const resNoAuth = await testApp.inject({
    method: 'POST',
    url: '/ai/analyze-text',
    payload: { text: '1 apple' },
  });
  assert.strictEqual(resNoAuth.statusCode, 401, 'Request without token must be 401');
  assert.strictEqual(JSON.parse(resNoAuth.body).error, 'Unauthorized');
  console.log('✓ Test 2: Unauthenticated request rejected with 401 Unauthorized');

  // Test 3 (anti-drift A): invalid token is rejected via mock rejection
  const resInvalid = await testApp.inject({
    method: 'POST',
    url: '/ai/analyze-text',
    headers: { authorization: 'Bearer invalid-token-xyz' },
    payload: { text: '1 apple' },
  });
  assert.strictEqual(resInvalid.statusCode, 401, 'Invalid token must be 401');
  assert.strictEqual(JSON.parse(resInvalid.body).error, 'Unauthorized');
  console.log('✓ Test 3: Invalid token rejected with 401 (mock genuinely rejects)');

  // Test 4 (anti-drift B): test-token- prefix is ALWAYS 401, even though it
  // looks like a "test convenience" token. Guards against bypass regression.
  const resTestPrefix = await testApp.inject({
    method: 'POST',
    url: '/ai/analyze-text',
    headers: { authorization: 'Bearer test-token-attack-probe-xyz' },
    payload: { text: 'attack probe' },
  });
  assert.strictEqual(resTestPrefix.statusCode, 401, 'test-token- prefix must be 401');
  assert.strictEqual(JSON.parse(resTestPrefix.body).error, 'Unauthorized');
  console.log('✓ Test 4: test-token-* rejected with 401 (no bypass in any env)');

  // Test 5: valid mocked Firebase token succeeds and populates user
  const resValid = await testApp.inject({
    method: 'POST',
    url: '/ai/analyze-text',
    headers: { authorization: 'Bearer valid-firebase-token-alice' },
    payload: { text: '1 apple' },
  });
  assert.strictEqual(resValid.statusCode, 200, 'Valid token must return 200');
  const bodyValid = JSON.parse(resValid.body);
  assert.strictEqual(bodyValid.user.uid, 'alice123');
  assert.strictEqual(resValid.headers['x-ratelimit-limit'], '30');
  assert.strictEqual(resValid.headers['x-ratelimit-remaining'], '29');
  console.log('✓ Test 5: Authenticated request accepted, user UID verified, rate headers set');

  // Test 6: Rate limiter blocks 31st call from same user with 429
  console.log('Testing rate limiter (exhausting 30 calls)...');
  for (let i = 0; i < 29; i++) {
    await testApp.inject({
      method: 'POST',
      url: '/ai/analyze-text',
      headers: { authorization: 'Bearer valid-firebase-token-alice' },
      payload: { text: '1 apple' },
    });
  }

  const resRateLimited = await testApp.inject({
    method: 'POST',
    url: '/ai/analyze-text',
    headers: { authorization: 'Bearer valid-firebase-token-alice' },
    payload: { text: '1 apple' },
  });
  assert.strictEqual(resRateLimited.statusCode, 429, '31st request must return 429');
  assert(parseInt(resRateLimited.headers['retry-after'], 10) > 0, 'Must include Retry-After header');
  console.log('✓ Test 6: Rate limiter triggered 429 Too Many Requests with Retry-After header');

  // Test 7: Different user UID is unaffected by first user rate limit
  const resBob = await testApp.inject({
    method: 'POST',
    url: '/ai/analyze-text',
    headers: { authorization: 'Bearer valid-firebase-token-bob' },
    payload: { text: '1 banana' },
  });
  assert.strictEqual(resBob.statusCode, 200, 'Different user must not be blocked by other user limit');
  assert.strictEqual(resBob.headers['x-ratelimit-remaining'], '29');
  console.log('✓ Test 7: Per-user isolation verified (User Bob unaffected by User Alice limits)');

  console.log('\nAll 7 Backend Auth & Rate-Limiting Tests Passed Successfully!');
}

runTests().catch((err) => {
  console.error('Test failed:', err);
  process.exit(1);
});
