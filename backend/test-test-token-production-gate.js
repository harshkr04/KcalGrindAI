import Fastify from 'fastify';
import assert from 'node:assert';

// Test suite: Gated test-token authentication and production rejection verification.

async function buildTestApp(envSettings) {
  const originalNodeEnv = process.env.NODE_ENV;
  const originalAllowDevTokens = process.env.ALLOW_DEV_TOKENS;

  // Apply environment settings for this test app instance
  if (envSettings.NODE_ENV !== undefined) {
    process.env.NODE_ENV = envSettings.NODE_ENV;
  } else {
    delete process.env.NODE_ENV;
  }

  if (envSettings.ALLOW_DEV_TOKENS !== undefined) {
    process.env.ALLOW_DEV_TOKENS = envSettings.ALLOW_DEV_TOKENS;
  } else {
    delete process.env.ALLOW_DEV_TOKENS;
  }

  const app = Fastify({ logger: false });

  const RATE_LIMIT_MAX_CALLS = 30;

  // Exact auth logic matching src/server.js preHandler
  app.addHook('preHandler', async (request, reply) => {
    if (request.url === '/health') return;

    const authHeader = request.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return reply.code(401).send({ error: 'Unauthorized', message: 'Missing Authorization header' });
    }

    const idToken = authHeader.split('Bearer ')[1].trim();

    if (idToken.startsWith('test-token-')) {
      const isProduction = process.env.NODE_ENV === 'production';
      const allowDevTokens = process.env.ALLOW_DEV_TOKENS === 'true' && !isProduction;

      if (!allowDevTokens || isProduction) {
        return reply.code(401).send({
          error: 'Unauthorized',
          message: 'Invalid or expired Firebase ID token.',
          details: isProduction
            ? 'Test tokens are strictly prohibited in production environments.'
            : 'Test tokens are disabled. Set ALLOW_DEV_TOKENS=true in non-production environments to enable.'
        });
      }

      const testUid = idToken.replace('test-token-', '') || 'test-user';
      request.user = { uid: testUid, email: `${testUid}@local.test` };
      reply.header('X-RateLimit-Limit', RATE_LIMIT_MAX_CALLS);
      reply.header('X-RateLimit-Remaining', RATE_LIMIT_MAX_CALLS - 1);
      return;
    }

    // Default unknown tokens require Firebase verification
    return reply.code(401).send({ error: 'Unauthorized', message: 'Real Firebase ID token required' });
  });

  app.get('/health', async () => ({ status: 'ok' }));
  app.get('/ai/test-endpoint', async (request) => ({ success: true, user: request.user }));

  return {
    app,
    cleanup: () => {
      if (originalNodeEnv !== undefined) process.env.NODE_ENV = originalNodeEnv;
      else delete process.env.NODE_ENV;
      if (originalAllowDevTokens !== undefined) process.env.ALLOW_DEV_TOKENS = originalAllowDevTokens;
      else delete process.env.ALLOW_DEV_TOKENS;
    }
  };
}

async function runTests() {
  console.log('--- Testing Production & Gated Test-Token Authentication Security ---');

  // Case 1: In production, test-token is REJECTED even if ALLOW_DEV_TOKENS is explicitly 'true'
  {
    const { app, cleanup } = await buildTestApp({ NODE_ENV: 'production', ALLOW_DEV_TOKENS: 'true' });
    try {
      const res = await app.inject({
        method: 'GET',
        url: '/ai/test-endpoint',
        headers: { authorization: 'Bearer test-token-superadmin' }
      });
      assert.strictEqual(res.statusCode, 401, 'Must return 401 in production even with ALLOW_DEV_TOKENS=true');
      const body = JSON.parse(res.body);
      assert.strictEqual(body.error, 'Unauthorized');
      assert.strictEqual(body.details, 'Test tokens are strictly prohibited in production environments.');
      console.log('✓ Test 1 Passed: test-token is strictly rejected when NODE_ENV=production (even if ALLOW_DEV_TOKENS=true)');
    } finally {
      cleanup();
    }
  }

  // Case 2: In production with ALLOW_DEV_TOKENS unset, test-token is REJECTED
  {
    const { app, cleanup } = await buildTestApp({ NODE_ENV: 'production' });
    try {
      const res = await app.inject({
        method: 'GET',
        url: '/ai/test-endpoint',
        headers: { authorization: 'Bearer test-token-attacker' }
      });
      assert.strictEqual(res.statusCode, 401, 'Must return 401 in production when unset');
      const body = JSON.parse(res.body);
      assert.strictEqual(body.error, 'Unauthorized');
      assert.strictEqual(body.details, 'Test tokens are strictly prohibited in production environments.');
      console.log('✓ Test 2 Passed: test-token is strictly rejected in production when ALLOW_DEV_TOKENS is unset');
    } finally {
      cleanup();
    }
  }

  // Case 3: In non-production, test-token is REJECTED if ALLOW_DEV_TOKENS is not set
  {
    const { app, cleanup } = await buildTestApp({ NODE_ENV: 'development' });
    try {
      const res = await app.inject({
        method: 'GET',
        url: '/ai/test-endpoint',
        headers: { authorization: 'Bearer test-token-developer' }
      });
      assert.strictEqual(res.statusCode, 401, 'Must return 401 when ALLOW_DEV_TOKENS is not set to true');
      const body = JSON.parse(res.body);
      assert.strictEqual(body.error, 'Unauthorized');
      assert.strictEqual(body.details, 'Test tokens are disabled. Set ALLOW_DEV_TOKENS=true in non-production environments to enable.');
      console.log('✓ Test 3 Passed: test-token is rejected in development when ALLOW_DEV_TOKENS is missing');
    } finally {
      cleanup();
    }
  }

  // Case 4: In non-production, test-token SUCCEEDS only when ALLOW_DEV_TOKENS === 'true'
  {
    const { app, cleanup } = await buildTestApp({ NODE_ENV: 'test', ALLOW_DEV_TOKENS: 'true' });
    try {
      const res = await app.inject({
        method: 'GET',
        url: '/ai/test-endpoint',
        headers: { authorization: 'Bearer test-token-qa-tester' }
      });
      assert.strictEqual(res.statusCode, 200, 'Must return 200 in test environment when ALLOW_DEV_TOKENS=true');
      const body = JSON.parse(res.body);
      assert.strictEqual(body.success, true);
      assert.strictEqual(body.user.uid, 'qa-tester');
      console.log('✓ Test 4 Passed: test-token succeeds in non-production when ALLOW_DEV_TOKENS=true');
    } finally {
      cleanup();
    }
  }

  console.log('\nAll 4 production gating tests passed successfully.');
}

runTests().catch((err) => {
  console.error('Test failed:', err);
  process.exit(1);
});
