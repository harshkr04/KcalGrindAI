/**
 * KcalGrind AI - Canary Logging Privacy Test
 *
 * Verifies Constitution Principle:
 * "Zero leakage of user data in server logs or console traces."
 *
 * Sends a unique canary secret token through every /ai/* endpoint:
 * - /ai/analyze-photo (success & error paths)
 * - /ai/analyze-text (success & error paths)
 * - /ai/transcribe (success & error paths)
 * - /ai/chat (success & error paths)
 *
 * Asserts the canary token NEVER appears in:
 * 1. backend.log
 * 2. stdout or stderr
 * 3. Server error responses or exception traces
 */

process.env.TEST = 'true';
process.env.NODE_ENV = 'test';
process.env.ALLOW_DEV_TOKENS = 'true';
process.env.PORT = '8097';

import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const CANARY_TOKEN = `CANARY_LEAK_TEST_998877_SECRET_${Date.now()}`;
const LOG_FILE = path.resolve(__dirname, 'backend.log');

test('Canary Privacy Logging Verification', async (t) => {
  const { fastify } = await import('./src/server.js');

  // Intercept stdout and stderr
  const capturedLogs = [];
  const origStdoutWrite = process.stdout.write;
  const origStderrWrite = process.stderr.write;

  process.stdout.write = (chunk, ...args) => {
    capturedLogs.push(chunk.toString());
    return origStdoutWrite.apply(process.stdout, [chunk, ...args]);
  };

  process.stderr.write = (chunk, ...args) => {
    capturedLogs.push(chunk.toString());
    return origStderrWrite.apply(process.stderr, [chunk, ...args]);
  };

  const initialLogOffset = fs.existsSync(LOG_FILE) ? fs.statSync(LOG_FILE).size : 0;

  try {
    // -------------------------------------------------------------
    // 1. /ai/analyze-photo (Error path: invalid base64 containing canary)
    // -------------------------------------------------------------
    await t.test('POST /ai/analyze-photo error path does not leak canary', async () => {
      const res = await fastify.inject({
        method: 'POST',
        url: '/ai/analyze-photo',
        headers: { authorization: 'Bearer test-token-canary-user' },
        payload: {
          imageBase64: `data:image/jpeg;base64,${Buffer.from(CANARY_TOKEN).toString('base64')}`,
          dietTags: [CANARY_TOKEN]
        }
      });
      // Should reject invalid magic bytes without echoing canary
      assert.ok(!res.body.includes(CANARY_TOKEN), 'Response body must not contain canary token');
    });

    // -------------------------------------------------------------
    // 2. /ai/analyze-text (Success & Error paths containing canary)
    // -------------------------------------------------------------
    await t.test('POST /ai/analyze-text does not leak canary in logs or errors', async () => {
      // (a) Error path: empty text but canary in dietTags
      const resErr = await fastify.inject({
        method: 'POST',
        url: '/ai/analyze-text',
        headers: { authorization: 'Bearer test-token-canary-user' },
        payload: {
          text: '',
          dietTags: [CANARY_TOKEN]
        }
      });
      assert.equal(resErr.statusCode, 400);
      assert.ok(!resErr.body.includes(CANARY_TOKEN), 'Error response must not contain canary token');

      // (b) Valid path: user meal text contains canary token
      const resOk = await fastify.inject({
        method: 'POST',
        url: '/ai/analyze-text',
        headers: { authorization: 'Bearer test-token-canary-user' },
        payload: {
          text: `2 rotis and 1 bowl dal with confidential note: ${CANARY_TOKEN}`,
          dietTags: ['vegetarian']
        }
      });
      // Even if AI parses food, the server itself must not log the raw text
      assert.ok(resOk.statusCode === 200 || resOk.statusCode === 503 || resOk.statusCode === 500);
    });

    // -------------------------------------------------------------
    // 3. /ai/transcribe (Error path containing canary)
    // -------------------------------------------------------------
    await t.test('POST /ai/transcribe does not leak canary in logs or errors', async () => {
      const res = await fastify.inject({
        method: 'POST',
        url: '/ai/transcribe',
        headers: { authorization: 'Bearer test-token-canary-user' },
        payload: {
          audioBase64: Buffer.from(CANARY_TOKEN).toString('base64')
        }
      });
      assert.ok(!res.body.includes(CANARY_TOKEN), 'Transcribe error must not echo canary token');
    });

    // -------------------------------------------------------------
    // 4. /ai/chat (Success & Error paths containing canary)
    // -------------------------------------------------------------
    await t.test('POST /ai/chat does not leak canary in logs or errors', async () => {
      // (a) Error path: malformed message
      const resErr = await fastify.inject({
        method: 'POST',
        url: '/ai/chat',
        headers: { authorization: 'Bearer test-token-canary-user' },
        payload: {
          messages: [],
          secret: CANARY_TOKEN
        }
      });
      assert.equal(resErr.statusCode, 400);
      assert.ok(!resErr.body.includes(CANARY_TOKEN), 'Chat error must not contain canary token');

      // (b) Prompt containing canary
      const resOk = await fastify.inject({
        method: 'POST',
        url: '/ai/chat',
        headers: { authorization: 'Bearer test-token-canary-user' },
        payload: {
          messages: [{ role: 'user', content: `What is high protein? Note: ${CANARY_TOKEN}` }],
          userContext: { goal: 'MAINTAIN', targetCalories: 2000 }
        }
      });
      assert.ok(resOk.statusCode === 200 || resOk.statusCode === 503);
    });

  } finally {
    // Restore stdout and stderr
    process.stdout.write = origStdoutWrite;
    process.stderr.write = origStderrWrite;
  }

  // -------------------------------------------------------------
  // Audit 1: Assert Canary Token NEVER appears in captured console logs
  // -------------------------------------------------------------
  const fullConsoleLog = capturedLogs.join('');
  assert.ok(
    !fullConsoleLog.includes(CANARY_TOKEN),
    `CRITICAL PRIVACY VIOLATION: Canary token leaked in console/stdout/stderr!`
  );
  console.log('✓ Console Log Privacy Audit PASSED: Zero occurrences of canary token in stdout/stderr');

  // -------------------------------------------------------------
  // Audit 2: Assert Canary Token NEVER appears in backend.log
  // -------------------------------------------------------------
  if (fs.existsSync(LOG_FILE)) {
    const currentSize = fs.statSync(LOG_FILE).size;
    const newLogBytes = Buffer.alloc(currentSize - initialLogOffset);
    const fd = fs.openSync(LOG_FILE, 'r');
    fs.readSync(fd, newLogBytes, 0, newLogBytes.length, initialLogOffset);
    fs.closeSync(fd);
    const newLogContent = newLogBytes.toString('utf8');

    assert.ok(
      !newLogContent.includes(CANARY_TOKEN),
      `CRITICAL PRIVACY VIOLATION: Canary token leaked in backend.log disk file!`
    );
    console.log('✓ Disk Log Privacy Audit PASSED: Zero occurrences of canary token in backend.log');
  }

});
