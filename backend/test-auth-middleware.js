import Fastify from 'fastify';
import assert from 'node:assert';

// Set environment for test
process.env.ALLOW_DEV_TOKENS = 'true';
process.env.FIREBASE_PROJECT_ID = 'lumina-nutrition-app';

async function runTests() {
  console.log('Testing Fastify Auth & Rate Limiting preHandler Hook...');

  // Create isolated test server with identical hook and limiter
  const testApp = Fastify({ logger: false });

  const rateLimits = new Map();
  const RATE_LIMIT_WINDOW_MS = 60 * 60 * 1000;
  const RATE_LIMIT_MAX_CALLS = 30;

  function checkRateLimit(uid) {
    const now = Date.now();
    const windowStart = now - RATE_LIMIT_WINDOW_MS;
    const timestamps = (rateLimits.get(uid) || []).filter(t => t > windowStart);

    if (timestamps.length >= RATE_LIMIT_MAX_CALLS) {
      const oldest = timestamps[0];
      const resetSeconds = Math.ceil((oldest + RATE_LIMIT_WINDOW_MS - now) / 1000);
      return { allowed: false, remaining: 0, resetSeconds };
    }

    timestamps.push(now);
    rateLimits.set(uid, timestamps);
    return { allowed: true, remaining: RATE_LIMIT_MAX_CALLS - timestamps.length, resetSeconds: 0 };
  }

  testApp.addHook('preHandler', async (request, reply) => {
    if (request.url === '/health' || request.url === '/') {
      return;
    }

    const authHeader = request.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return reply.code(401).send({
        error: 'Unauthorized',
        message: 'Missing or invalid Authorization header. A valid Firebase ID token is required.'
      });
    }

    const idToken = authHeader.split('Bearer ')[1].trim();

    if (process.env.ALLOW_DEV_TOKENS === 'true' && idToken.startsWith('test-token-')) {
      const fakeUid = idToken.replace('test-token-', '') || 'test-user';
      request.user = { uid: fakeUid, email: `${fakeUid}@test.local` };
      const rateStatus = checkRateLimit(fakeUid);
      reply.header('X-RateLimit-Limit', RATE_LIMIT_MAX_CALLS);
      reply.header('X-RateLimit-Remaining', rateStatus.remaining);
      if (!rateStatus.allowed) {
        reply.header('Retry-After', rateStatus.resetSeconds);
        return reply.code(429).send({
          error: 'Too Many Requests',
          message: `Hourly AI limit reached. Please try again in ${rateStatus.resetSeconds} seconds.`
        });
      }
      return;
    }

    return reply.code(401).send({
      error: 'Unauthorized',
      message: 'Invalid or expired Firebase ID token.'
    });
  });

  testApp.get('/health', async () => ({ status: 'ok' }));
  testApp.post('/ai/analyze-text', async (request) => ({
    success: true,
    user: request.user
  }));

  // Test 1: GET /health allows unauthenticated access
  const resHealth = await testApp.inject({
    method: 'GET',
    url: '/health'
  });
  assert.strictEqual(resHealth.statusCode, 200, 'GET /health must return 200');
  console.log('✓ Test 1: /health is accessible without authentication');

  // Test 2: POST /ai/analyze-text without header is rejected with 401
  const resNoAuth = await testApp.inject({
    method: 'POST',
    url: '/ai/analyze-text',
    payload: { text: '1 apple' }
  });
  assert.strictEqual(resNoAuth.statusCode, 401, 'Request without token must be 401');
  const bodyNoAuth = JSON.parse(resNoAuth.body);
  assert.strictEqual(bodyNoAuth.error, 'Unauthorized');
  console.log('✓ Test 2: Unauthenticated request rejected with 401 Unauthorized');

  // Test 3: POST /ai/analyze-text with invalid token format is rejected with 401
  const resInvalid = await testApp.inject({
    method: 'POST',
    url: '/ai/analyze-text',
    headers: { authorization: 'Bearer invalid-token-xyz' },
    payload: { text: '1 apple' }
  });
  assert.strictEqual(resInvalid.statusCode, 401, 'Invalid token must be 401');
  console.log('✓ Test 3: Invalid token rejected with 401 Unauthorized');

  // Test 4: POST /ai/analyze-text with valid token succeeds and populates user
  const resValid = await testApp.inject({
    method: 'POST',
    url: '/ai/analyze-text',
    headers: { authorization: 'Bearer test-token-alice123' },
    payload: { text: '1 apple' }
  });
  assert.strictEqual(resValid.statusCode, 200, 'Valid token must return 200');
  const bodyValid = JSON.parse(resValid.body);
  assert.strictEqual(bodyValid.user.uid, 'alice123');
  assert.strictEqual(resValid.headers['x-ratelimit-limit'], '30');
  assert.strictEqual(resValid.headers['x-ratelimit-remaining'], '29');
  console.log('✓ Test 4: Authenticated request accepted, user UID verified, rate headers set');

  // Test 5: Rate limiter blocks 31st call from same user with 429
  console.log('Testing rate limiter (exhausting 30 calls)...');
  for (let i = 0; i < 29; i++) {
    await testApp.inject({
      method: 'POST',
      url: '/ai/analyze-text',
      headers: { authorization: 'Bearer test-token-alice123' },
      payload: { text: '1 apple' }
    });
  }

  const resRateLimited = await testApp.inject({
    method: 'POST',
    url: '/ai/analyze-text',
    headers: { authorization: 'Bearer test-token-alice123' },
    payload: { text: '1 apple' }
  });
  assert.strictEqual(resRateLimited.statusCode, 429, '31st request must return 429 Too Many Requests');
  assert(parseInt(resRateLimited.headers['retry-after'], 10) > 0, 'Must include Retry-After header');
  console.log('✓ Test 5: Rate limiter triggered 429 Too Many Requests with Retry-After header');

  // Test 6: Different user UID is unaffected by first user rate limit
  const resBob = await testApp.inject({
    method: 'POST',
    url: '/ai/analyze-text',
    headers: { authorization: 'Bearer test-token-bob456' },
    payload: { text: '1 banana' }
  });
  assert.strictEqual(resBob.statusCode, 200, 'Different user must not be blocked by other user limit');
  assert.strictEqual(resBob.headers['x-ratelimit-remaining'], '29');
  console.log('✓ Test 6: Per-user isolation verified (User Bob unaffected by User Alice limits)');

  console.log('\nAll 6 Backend Auth & Rate-Limiting Tests Passed Successfully!');
}

runTests().catch(err => {
  console.error('Test failed:', err);
  process.exit(1);
});
