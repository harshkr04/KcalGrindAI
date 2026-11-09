process.env.TEST = 'true';

import test from 'node:test';
import assert from 'node:assert/strict';

test('Gemini Model Config & Fallback Verification', async (t) => {
  const { executeGeminiWithFallback, fastify } = await import('./src/server.js');

  await t.test('Primary model failure cleanly falls back to fallback model via executeGeminiWithFallback', async () => {
    const originalPrimary = process.env.GEMINI_PRIMARY_MODEL;
    const originalFallback = process.env.GEMINI_FALLBACK_MODEL;

    // Set deliberately invalid primary model ID to prove failover
    process.env.GEMINI_PRIMARY_MODEL = 'deliberately-invalid-primary-model-404';
    process.env.GEMINI_FALLBACK_MODEL = 'gemini-3.5-flash-lite';

    try {
      console.log('  Testing fallback with invalid primary:', process.env.GEMINI_PRIMARY_MODEL);
      const result = await executeGeminiWithFallback({
        contents: 'Hello, reply with pong'
      }, 'TestFallbackVerification');

      assert.ok(result, 'Result must be returned');
      assert.strictEqual(result.fallback, true, 'Result must indicate fallback occurred');
      assert.strictEqual(result.modelUsed, 'gemini-3.5-flash-lite', 'Fallback model must be used');
      assert.ok(result.response.text, 'Response text must be present from fallback model');
      console.log('  ✓ Deliberately invalid primary triggered clean failover to gemini-3.5-flash-lite (Response:', result.response.text.trim().substring(0, 30), ')');
    } finally {
      if (originalPrimary !== undefined) process.env.GEMINI_PRIMARY_MODEL = originalPrimary;
      else delete process.env.GEMINI_PRIMARY_MODEL;

      if (originalFallback !== undefined) process.env.GEMINI_FALLBACK_MODEL = originalFallback;
      else delete process.env.GEMINI_FALLBACK_MODEL;
    }
  });

  await t.test('Primary and Fallback model IDs must be distinct', async () => {
    const { GEMINI_PRIMARY_MODEL, GEMINI_FALLBACK_MODEL } = await import('./src/server.js');
    assert.notStrictEqual(
      GEMINI_PRIMARY_MODEL,
      GEMINI_FALLBACK_MODEL,
      `Primary (${GEMINI_PRIMARY_MODEL}) and Fallback (${GEMINI_FALLBACK_MODEL}) must be distinct model IDs`
    );
    console.log(`  ✓ Models are distinct: Primary=${GEMINI_PRIMARY_MODEL}, Fallback=${GEMINI_FALLBACK_MODEL}`);
  });

  await t.test('/health reports distinct model configuration', async () => {
    const res = await fastify.inject({
      method: 'GET',
      url: '/health'
    });
    assert.equal(res.statusCode, 200);
    const json = JSON.parse(res.body);
    console.log('  [HEALTH RESPONSE]', JSON.stringify(json));
    assert.ok(json.models.primary, 'Health reports primary model');
    assert.ok(json.models.fallback, 'Health reports fallback model');
    assert.notStrictEqual(json.models.primary, json.models.fallback, 'Health primary and fallback must be distinct');
    console.log(`  ✓ Health reports primary: ${json.models.primary}, fallback: ${json.models.fallback}`);
  });
});
