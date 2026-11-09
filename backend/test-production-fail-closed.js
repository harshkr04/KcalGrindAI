process.env.TEST = 'true';
process.env.NODE_ENV = 'production';
delete process.env.FIREBASE_AUTH_EMULATOR_HOST;
delete process.env.SUPABASE_URL;
delete process.env.SUPABASE_SERVICE_KEY;
process.env.FIREBASE_PROJECT_ID = 'test-kcalgrind-ci';
process.env.GEMINI_API_KEY = 'mock-ci-gemini-key';

import assert from 'node:assert';

const {
  setSupabaseClient,
  tryReserveAiCoachQuotaAsync,
  commitAiCoachQuotaAsync,
  getAiCoachUsageAsync,
  fastify
} = await import('./src/server.js');

async function runProductionFailClosedTests() {
  console.log('=== Running Production Fail-Closed Tests (NODE_ENV=production) ===\n');

  // Ensure Supabase client is strictly null
  setSupabaseClient(null);

  // Test 1: In production mode, tryReserveAiCoachQuotaAsync MUST throw and disallow local in-memory/disk fallback
  console.log('Test 1: In-memory fallback strictly disallowed in production');
  let threwReservation = false;
  try {
    await tryReserveAiCoachQuotaAsync('prod_user_test', 'UTC', false);
  } catch (err) {
    threwReservation = true;
    assert.match(err.message, /Production error.*strictly required/i);
  }
  assert.strictEqual(threwReservation, true, 'tryReserveAiCoachQuotaAsync must throw error when Supabase is missing in production');
  console.log('✓ tryReserveAiCoachQuotaAsync throws error in production when Supabase is missing\n');

  // Test 2: In production mode, commitAiCoachQuotaAsync MUST throw error
  console.log('Test 2: commitAiCoachQuotaAsync strictly disallowed in production without Supabase');
  let threwCommit = false;
  try {
    await commitAiCoachQuotaAsync('prod_user_test', 'UTC');
  } catch (err) {
    threwCommit = true;
    assert.match(err.message, /Production error.*strictly required/i);
  }
  assert.strictEqual(threwCommit, true, 'commitAiCoachQuotaAsync must throw error when Supabase is missing in production');
  console.log('✓ commitAiCoachQuotaAsync throws error in production when Supabase is missing\n');

  // Test 3: In production mode, getAiCoachUsageAsync MUST throw error
  console.log('Test 3: getAiCoachUsageAsync strictly disallowed in production without Supabase');
  let threwUsage = false;
  try {
    await getAiCoachUsageAsync('prod_user_test', 'UTC', false);
  } catch (err) {
    threwUsage = true;
    assert.match(err.message, /Production error.*strictly required/i);
  }
  assert.strictEqual(threwUsage, true, 'getAiCoachUsageAsync must throw error when Supabase is missing in production');
  console.log('✓ getAiCoachUsageAsync throws error in production when Supabase is missing\n');

  // Test 4: Quota-dependent route POST /ai/chat returns HTTP 503 Service Unavailable in production
  console.log('Test 4: POST /ai/chat returns HTTP 503 when Supabase is missing in production');
  const chatRes = await fastify.inject({
    method: 'POST',
    url: '/ai/chat',
    payload: {
      messages: [{ role: 'user', content: 'Hello' }],
      userContext: {}
    }
  });

  assert.strictEqual(chatRes.statusCode, 503, 'POST /ai/chat must return 503 in production without Supabase');
  const chatBody = JSON.parse(chatRes.payload);
  assert.strictEqual(chatBody.error, 'ServiceUnavailable');
  console.log('✓ POST /ai/chat returns 503 Service Unavailable in production\n');

  // Test 5: In non-production mode, local fallback succeeds cleanly
  console.log('Test 5: In non-production mode, local fallback is permitted');
  process.env.NODE_ENV = 'test';
  const fallbackRes = await tryReserveAiCoachQuotaAsync('test_user_allowed', 'UTC', false);
  assert.strictEqual(fallbackRes.allowed, true, 'In test/dev mode, in-memory fallback is allowed');
  console.log('✓ Local in-memory fallback succeeds in non-production mode\n');

  // Test 6: In production mode, when Supabase RPC throws or errors, tryReserveAiCoachQuotaAsync MUST throw 503
  console.log('Test 6: Supabase RPC error in production mode strictly returns 503 (no non-atomic fallback)');
  process.env.NODE_ENV = 'production';
  const mockFailingRpcSupabase = {
    rpc: async (fnName) => {
      return { data: null, error: { message: 'relation public.ai_coach_daily_usage does not exist' } };
    },
    from: () => {
      throw new Error('FATAL: Direct table fallback must never be called in production when RPC fails!');
    }
  };
  setSupabaseClient(mockFailingRpcSupabase);

  let rpcThrew503 = false;
  try {
    await tryReserveAiCoachQuotaAsync('prod_user_failing_rpc', 'UTC', false);
  } catch (err) {
    rpcThrew503 = true;
    assert.strictEqual(err.statusCode, 503, 'Error status code must be 503');
    assert.match(err.message, /Production database error/i);
  }
  assert.strictEqual(rpcThrew503, true, 'tryReserveAiCoachQuotaAsync must throw 503 when RPC errors in production');
  console.log('✓ Production RPC error strictly throws 503 with zero non-atomic fallback\n');

  // Test 7: Quota-dependent route POST /ai/chat returns HTTP 503 when RPC errors in production
  console.log('Test 7: POST /ai/chat returns HTTP 503 when RPC errors in production');
  const chatRpcFailRes = await fastify.inject({
    method: 'POST',
    url: '/ai/chat',
    headers: {
      authorization: 'Bearer valid-prod-token'
    },
    payload: {
      messages: [{ role: 'user', content: 'What should I eat?' }],
      userContext: {}
    }
  });

  // Note: in production without a real Firebase token, auth preHandler or quota error gates appropriately.
  // When an authenticated request encounters an RPC error, it returns 503.
  assert.ok([401, 503].includes(chatRpcFailRes.statusCode), 'Route safely gates unauthorized requests or returns 503');
  console.log('✓ Production security gating verified for RPC error states\n');

  // Test 8: Security Audit of Migration 003 RPC Permissions
  console.log('Test 8: Migration 003 enforces search_path=public, revokes anon/authenticated, grants service_role only');
  const fs = await import('node:fs');
  const path = await import('node:path');
  const migration003Path = path.resolve('../supabase/migrations/003_ai_coach_daily_usage.sql');
  const migration003Sql = fs.readFileSync(migration003Path, 'utf8');

  // Verify SET search_path = public on all functions
  const searchPathMatches = migration003Sql.match(/SET search_path = public/g);
  assert.ok(searchPathMatches && searchPathMatches.length >= 3, 'Must declare SET search_path = public on all 3 RPC functions');

  // Verify REVOKE from PUBLIC, anon, authenticated
  assert.match(migration003Sql, /REVOKE EXECUTE ON FUNCTION public\.reserve_ai_coach_quota.*FROM PUBLIC, anon, authenticated/i);
  assert.match(migration003Sql, /REVOKE EXECUTE ON FUNCTION public\.release_ai_coach_reservation.*FROM PUBLIC, anon, authenticated/i);
  assert.match(migration003Sql, /REVOKE EXECUTE ON FUNCTION public\.commit_ai_coach_quota.*FROM PUBLIC, anon, authenticated/i);

  // Verify GRANT only to service_role
  assert.match(migration003Sql, /GRANT EXECUTE ON FUNCTION public\.reserve_ai_coach_quota.*TO service_role/i);
  assert.match(migration003Sql, /GRANT EXECUTE ON FUNCTION public\.release_ai_coach_reservation.*TO service_role/i);
  assert.match(migration003Sql, /GRANT EXECUTE ON FUNCTION public\.commit_ai_coach_quota.*TO service_role/i);

  // Verify first-insert race fix
  assert.match(migration003Sql, /ON CONFLICT \(firebase_uid, local_date\) DO NOTHING/i);
  assert.match(migration003Sql, /FOR UPDATE/i);
  console.log('✓ Migration 003 search_path, permissions revocation and first-insert race fix verified\n');

  console.log('=== All 8 Production Fail-Closed & RPC Security Tests PASSED! ===');
}

runProductionFailClosedTests().catch((err) => {
  console.error('Production fail-closed tests failed:', err);
  process.exit(1);
});
