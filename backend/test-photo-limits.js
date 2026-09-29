/**
 * Kcal Grind AI - Photo Limits, Validation & Calibration Test Suite
 *
 * Covers:
 * 1. Body limit enforcement (4 MB route limit -> 413 PayloadTooLarge)
 * 2. Magic byte image validation (reject non-image / corrupt base64 -> 400 InvalidImage)
 * 3. Missing / empty image validation -> 400 InvalidImage
 * 4. Offline suite strictly does not hit any live provider
 * 5. Live calibration (solid black, solid white, non-food, real meal; 3 runs each; gated by RUN_LIVE_TESTS=1)
 *    - Waits >= 13 s between calls (to respect 5 RPM free tier) or honors Retry-After
 *    - Reads backend.log for EVERY run to report provider and model
 *    - Only runs served by 'gemini' count as pass
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { execSync } from 'child_process';
import axios from 'axios';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const BASE_URL = process.env.BASE_URL || 'http://127.0.0.1:8000';
const AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099';
const LOG_FILE_PATH = path.join(__dirname, 'backend.log');
const RUN_LIVE = process.env.RUN_LIVE_TESTS === '1';

async function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function getAuthToken() {
  const url = `http://${AUTH_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:signUp?key=fake-api-key`;
  const res = await axios.post(url, { returnSecureToken: true });
  return res.data.idToken;
}

function generateSolidColorJpeg(r, g, b, width = 1024, height = 1024) {
  const pyCode = `from PIL import Image; import io, sys; img = Image.new('RGB', (${width}, ${height}), color=(${r}, ${g}, ${b})); buf = io.BytesIO(); img.save(buf, format='JPEG', quality=85); sys.stdout.buffer.write(buf.getvalue())`;
  return execSync('python -c "' + pyCode + '"');
}

function loadPrivateAsset(filename, fallbackGenerator) {
  const privatePath = path.join(__dirname, 'test-assets', 'private', filename);
  if (fs.existsSync(privatePath)) {
    return fs.readFileSync(privatePath);
  }
  if (fallbackGenerator) {
    return fallbackGenerator();
  }
  throw new Error(`Private asset not found: ${privatePath}`);
}

function getLatestPhotoAnalyzeLogLine() {
  try {
    if (!fs.existsSync(LOG_FILE_PATH)) return null;
    const content = fs.readFileSync(LOG_FILE_PATH, 'utf8');
    const lines = content.trim().split('\n');
    for (let i = lines.length - 1; i >= 0; i--) {
      if (lines[i].includes('PHOTO_ANALYZE:')) {
        return lines[i];
      }
    }
  } catch (e) {}
  return null;
}

function parseProviderAndModelFromLog(logLine) {
  if (!logLine) return { provider: 'unknown', model: 'unknown' };
  const providerMatch = logLine.match(/provider=([^\s]+)/);
  const modelMatch = logLine.match(/model=([^\s]+)/);
  return {
    provider: providerMatch ? providerMatch[1] : 'unknown',
    model: modelMatch ? modelMatch[1] : 'unknown'
  };
}

async function runTest(name, fn) {
  process.stdout.write(`\n--- [TEST] ${name} ---\n`);
  try {
    await fn();
    console.log(`[PASS] ${name}`);
    return true;
  } catch (err) {
    console.error(`[FAIL] ${name}: ${err.message}`);
    if (err.response) {
      console.error(`Status: ${err.response.status}`);
      console.error('Data:', JSON.stringify(err.response.data, null, 2));
    }
    return false;
  }
}

async function main() {
  console.log('===============================================================');
  console.log(' Kcal Grind AI Photo Limits & Calibration Test Suite');
  console.log(` Target: ${BASE_URL} | Live Mode: ${RUN_LIVE ? 'ENABLED (RUN_LIVE_TESTS=1)' : 'DISABLED (offline only)'}`);
  console.log('===============================================================');

  const token = await getAuthToken();
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  };

  let allPassed = true;

  // -------------------------------------------------------------------------
  // OFFLINE TESTS (Guaranteed 0 calls to any live provider)
  // -------------------------------------------------------------------------

  // Test 1: Route bodyLimit enforcement on /ai/analyze-photo (> 4 MB payload -> 413)
  allPassed = await runTest('POST /ai/analyze-photo payload > 4 MB returns 413 PayloadTooLarge', async () => {
    const largePayload = 'A'.repeat(4.5 * 1024 * 1024);
    try {
      await axios.post(`${BASE_URL}/ai/analyze-photo`, { imageBase64: largePayload }, { headers, maxBodyLength: Infinity, maxContentLength: Infinity });
      throw new Error('Expected 413 PayloadTooLarge, but received 2xx');
    } catch (err) {
      if (err.response && err.response.status === 413) {
        console.log(`Received expected HTTP 413:`, JSON.stringify(err.response.data));
        if (err.response.data?.error !== 'PayloadTooLarge') {
          throw new Error(`Expected error 'PayloadTooLarge', got '${err.response.data?.error}'`);
        }
      } else {
        throw err;
      }
    }
  }) && allPassed;

  // Test 2: Route bodyLimit enforcement on /ai/chat (> 4 MB payload -> 413)
  allPassed = await runTest('POST /ai/chat payload > 4 MB returns 413 PayloadTooLarge', async () => {
    const largeMessage = 'X'.repeat(4.5 * 1024 * 1024);
    try {
      await axios.post(`${BASE_URL}/ai/chat`, { messages: [{ role: 'user', content: largeMessage }] }, { headers, maxBodyLength: Infinity, maxContentLength: Infinity });
      throw new Error('Expected 413 PayloadTooLarge, but received 2xx');
    } catch (err) {
      if (err.response && err.response.status === 413) {
        console.log(`Received expected HTTP 413:`, JSON.stringify(err.response.data));
        if (err.response.data?.error !== 'PayloadTooLarge') {
          throw new Error(`Expected error 'PayloadTooLarge', got '${err.response.data?.error}'`);
        }
      } else {
        throw err;
      }
    }
  }) && allPassed;

  // Test 3: Magic byte validation on /ai/analyze-photo (non-image text -> 400 InvalidImage)
  allPassed = await runTest('POST /ai/analyze-photo rejects text/corrupt base64 with 400 InvalidImage', async () => {
    const textBase64 = Buffer.from('This is plain text and definitely not a JPEG or PNG image').toString('base64');
    try {
      await axios.post(`${BASE_URL}/ai/analyze-photo`, { imageBase64: textBase64 }, { headers });
      throw new Error('Expected 400 InvalidImage, but received 2xx');
    } catch (err) {
      if (err.response && err.response.status === 400) {
        console.log(`Received expected HTTP 400:`, JSON.stringify(err.response.data));
        if (err.response.data?.error !== 'InvalidImage') {
          throw new Error(`Expected error 'InvalidImage', got '${err.response.data?.error}'`);
        }
      } else {
        throw err;
      }
    }
  }) && allPassed;

  // Test 4: Missing / empty imageBase64 -> 400 InvalidImage
  allPassed = await runTest('POST /ai/analyze-photo rejects empty imageBase64 with 400', async () => {
    try {
      await axios.post(`${BASE_URL}/ai/analyze-photo`, { imageBase64: '' }, { headers });
      throw new Error('Expected 400, but received 2xx');
    } catch (err) {
      if (err.response && err.response.status === 400) {
        console.log(`Received expected HTTP 400:`, JSON.stringify(err.response.data));
      } else {
        throw err;
      }
    }
  }) && allPassed;

  // -------------------------------------------------------------------------
  // LIVE CALIBRATION TESTS (gated by RUN_LIVE_TESTS=1)
  // -------------------------------------------------------------------------
  if (RUN_LIVE) {
    console.log('\n===============================================================');
    console.log(' LIVE CALIBRATION TESTS (3 runs each; pacing >= 13s for 5 RPM limit)');
    console.log(' Criteria:');
    console.log(' - Provider MUST be gemini (other provider = FAIL)');
    console.log(' - Blank / non-food: NO item with confidence >= 0.5, empty/no-food result');
    console.log(' - Real Indian meal: at least one plausible item per run');
    console.log('===============================================================');

    // Live Test: Prefix stripping on valid image
    allPassed = await runTest('Input handling strips data-uri prefix and recognizes JPEG magic bytes', async () => {
      const jpegBuffer = generateSolidColorJpeg(0, 0, 0, 64, 64);
      const prefixedBase64 = `data:image/jpeg;base64,${jpegBuffer.toString('base64')}`;
      const res = await axios.post(`${BASE_URL}/ai/analyze-photo`, { imageBase64: prefixedBase64 }, { headers });
      console.log(`Processed prefixed JPEG successfully. Status: ${res.status}`);
      const logLine = getLatestPhotoAnalyzeLogLine();
      const { provider, model } = parseProviderAndModelFromLog(logLine);
      console.log(`    Server Log: provider=${provider} model=${model}`);
      if (provider !== 'gemini') {
        throw new Error(`Expected provider 'gemini', got '${provider}'`);
      }
    }) && allPassed;

    console.log('Pacing wait (13s)...');
    await sleep(13000);

    // Prepare test images in memory
    console.log('\nPreparing test images in memory...');
    const blackJpeg = generateSolidColorJpeg(0, 0, 0, 1024, 1024);
    const whiteJpeg = generateSolidColorJpeg(255, 255, 255, 1024, 1024);
    const nonFoodJpeg = loadPrivateAsset('non_food.jpg');
    const mealJpeg = loadPrivateAsset('indian_meal.jpg');

    console.log(`Black 1024x1024 JPEG: ${blackJpeg.length} bytes`);
    console.log(`White 1024x1024 JPEG: ${whiteJpeg.length} bytes`);
    console.log(`Non-food JPEG: ${nonFoodJpeg.length} bytes`);
    console.log(`Real meal JPEG: ${mealJpeg.length} bytes`);

    const cases = [
      {
        name: '1024x1024 Solid Black Image',
        buffer: blackJpeg,
        isFood: false
      },
      {
        name: '1024x1024 Solid White Image',
        buffer: whiteJpeg,
        isFood: false
      },
      {
        name: 'Non-Food Photo (Office Desk / Laptop)',
        buffer: nonFoodJpeg,
        isFood: false
      },
      {
        name: 'Real Indian Meal Photo (Thali with Dal, Paneer, Rice, Roti)',
        buffer: mealJpeg,
        isFood: true
      }
    ];

    for (const testCase of cases) {
      console.log(`\n-------------------------------------------------------------`);
      console.log(`CALIBRATION CASE: ${testCase.name}`);
      console.log(`-------------------------------------------------------------`);

      for (let run = 1; run <= 3; run++) {
        let attempts = 0;
        let success = false;

        while (!success && attempts < 3) {
          attempts++;
          process.stdout.write(`  Run ${run}/3 (attempt ${attempts}): `);

          try {
            const res = await axios.post(`${BASE_URL}/ai/analyze-photo`, {
              imageBase64: testCase.buffer.toString('base64')
            }, { headers });

            const logLine = getLatestPhotoAnalyzeLogLine();
            const { provider, model } = parseProviderAndModelFromLog(logLine);

            const data = res.data;
            const foods = data.foods || [];
            const overallConfidence = data.overallConfidence ?? 0;

            console.log(`HTTP ${res.status} | provider=${provider} | model=${model} | overallConfidence=${overallConfidence} | foodsCount=${foods.length}`);
            if (foods.length > 0) {
              console.log(`    Foods detected:`, JSON.stringify(foods.map(f => ({ name: f.name, confidence: f.confidence, calories: f.calories }))));
            }

            // Verify provider is strictly gemini
            if (provider !== 'gemini') {
              console.error(`    [FAIL] Request served by '${provider}', expected 'gemini'`);
              allPassed = false;
            }

            if (!testCase.isFood) {
              // PASS criteria: NO item with confidence >= 0.5, and empty or no-food result
              const highConfidenceItems = foods.filter(f => (f.confidence ?? 0) >= 0.5);
              if (highConfidenceItems.length > 0) {
                console.error(`    [HALLUCINATION] Non-food image produced items with confidence >= 0.5:`, highConfidenceItems);
                allPassed = false;
              } else {
                console.log(`    [PASS] No food items with confidence >= 0.5`);
              }
            } else {
              // PASS criteria: Real Indian meal photo has at least one plausible item
              if (foods.length === 0) {
                console.error(`    [FAIL] Real meal produced 0 items`);
                allPassed = false;
              } else {
                console.log(`    [PASS] Identified ${foods.length} items: ${foods.map(f => f.name).join(', ')}`);
              }
            }

            success = true;
          } catch (err) {
            if (err.response && err.response.status === 503) {
              const retryAfter = parseInt(err.response.headers['retry-after'] || '15', 10);
              console.warn(`\n    [BUSY] HTTP 503 AiUnavailable received. Waiting ${retryAfter}s (Retry-After) before retry...`);
              await sleep((retryAfter + 1) * 1000);
            } else {
              console.error(`\n    [ERROR] HTTP ${err.response?.status || err.message}:`, JSON.stringify(err.response?.data || err.message));
              allPassed = false;
              break;
            }
          }
        }

        // Pacing delay between calls: >= 13 seconds
        if (run < 3 || testCase !== cases[cases.length - 1]) {
          console.log('    [Pacing delay 13s]...');
          await sleep(13000);
        }
      }
    }
  } else {
    console.log('\n[INFO] Skipping live calibration tests. Run with RUN_LIVE_TESTS=1 to execute live LLM tests.');
  }

  console.log('\n===============================================================');
  console.log(` SUITE RESULT: ${allPassed ? 'ALL TESTS PASSED' : 'TEST FAILURES ENCOUNTERED'}`);
  console.log('===============================================================');

  if (!allPassed) {
    process.exit(1);
  }
}

main().catch(err => {
  console.error('Fatal test error:', err);
  process.exit(1);
});
