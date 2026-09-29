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

if (process.env.RUN_LIVE_TESTS !== '1') {
  console.log('RUN_LIVE_TESTS is not set to 1. Calibration script must be gated on RUN_LIVE_TESTS=1.');
  process.exit(1);
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function getFirebaseAuthEmulatorToken() {
  const url = `http://${AUTH_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:signUp?key=fake-api-key`;
  const res = await axios.post(url, { returnSecureToken: true });
  if (!res.data || !res.data.idToken) {
    throw new Error('Failed to acquire genuine Firebase ID token from Auth emulator');
  }
  return res.data.idToken;
}

function getOrGenerateSolidColorJpeg(colorName, outputPath) {
  const ps = `Add-Type -AssemblyName System.Drawing; $b = New-Object System.Drawing.Bitmap 1024, 1024; $g = [System.Drawing.Graphics]::FromImage($b); $g.Clear([System.Drawing.Color]::${colorName}); $b.Save('${outputPath.replace(/\\/g, '/')}', [System.Drawing.Imaging.ImageFormat]::Jpeg); $b.Dispose()`;
  execSync(`powershell -NoProfile -Command "${ps}"`);
  return fs.readFileSync(outputPath);
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
  } catch (e) { }
  return null;
}

function parseProviderAndModel(logLine) {
  if (!logLine) return { provider: 'unknown', model: 'unknown' };
  const providerMatch = logLine.match(/provider=([^\s]+)/);
  const modelMatch = logLine.match(/model=([^\s]+)/);
  return {
    provider: providerMatch ? providerMatch[1] : 'unknown',
    model: modelMatch ? modelMatch[1] : 'unknown'
  };
}

async function runCalibration() {
  const dateStr = new Date().toISOString().split('T')[0];
  const outFilePath = path.join(__dirname, `calibration-${dateStr}.txt`);
  const logStream = fs.createWriteStream(outFilePath, { flags: 'w' });

  function log(msg) {
    console.log(msg);
    logStream.write(msg + '\n');
  }

  log(`================================================================`);
  log(` CALIBRATION RUN - ${new Date().toISOString()}`);
  log(` Target: ${BASE_URL} | Auth Emulator: ${AUTH_EMULATOR_HOST}`);
  log(`================================================================`);

  // 1. Generate 1024x1024 solid black and white JPEGs and assert DIFFERENT bytes
  const testAssetsDir = path.join(__dirname, 'test-assets');
  if (!fs.existsSync(testAssetsDir)) fs.mkdirSync(testAssetsDir, { recursive: true });

  const blackPath = path.join(testAssetsDir, 'solid_black_1024.jpg');
  const whitePath = path.join(testAssetsDir, 'solid_white_1024.jpg');

  const blackBuffer = getOrGenerateSolidColorJpeg('Black', blackPath);
  const whiteBuffer = getOrGenerateSolidColorJpeg('White', whitePath);

  if (!blackBuffer || blackBuffer.length === 0) throw new Error('Solid black JPEG generation failed (0 bytes)');
  if (!whiteBuffer || whiteBuffer.length === 0) throw new Error('Solid white JPEG generation failed (0 bytes)');
  if (blackBuffer.equals(whiteBuffer)) {
    throw new Error('ASSERTION FAILED: Black and White JPEG buffers must have DIFFERENT bytes');
  }
  log(`[ASSERTION PASSED] 1024x1024 Black (${blackBuffer.length} B) and White (${whiteBuffer.length} B) have DIFFERENT bytes.`);

  // 2. Load private test assets, THROW if missing (no fallback)
  const indianMealPath = path.join(testAssetsDir, 'private', 'indian_meal.jpg');
  if (!fs.existsSync(indianMealPath)) {
    throw new Error(`CRITICAL: Missing test asset: ${indianMealPath}. No fallback allowed.`);
  }
  const indianMealBuffer = fs.readFileSync(indianMealPath);
  log(`[ASSET LOADED] indian_meal.jpg: ${indianMealBuffer.length} bytes`);

  const nonFoodPath = path.join(testAssetsDir, 'private', 'non_food.jpg');
  if (!fs.existsSync(nonFoodPath)) {
    throw new Error(`CRITICAL: Missing test asset: ${nonFoodPath}. No fallback allowed.`);
  }
  const nonFoodBuffer = fs.readFileSync(nonFoodPath);
  log(`[ASSET LOADED] non_food.jpg: ${nonFoodBuffer.length} bytes`);

  // 3. Obtain genuine Firebase Auth emulator token
  const idToken = await getFirebaseAuthEmulatorToken();
  log('[AUTH] Firebase emulator token acquired');

  const testCases = [
    { name: 'Solid Black (1024x1024)', buffer: blackBuffer, expectNoFood: true },
    { name: 'Solid White (1024x1024)', buffer: whiteBuffer, expectNoFood: true },
    { name: 'Non-Food (Desk)', buffer: nonFoodBuffer, expectNoFood: true },
    { name: 'Real Food (Indian Meal / Thali)', buffer: indianMealBuffer, expectNoFood: false }
  ];

  const results = [];

  for (const tc of testCases) {
    log(`\n>>> Testing: ${tc.name} (3 runs)`);
    for (let run = 1; run <= 3; run++) {
      const timestamp = new Date().toISOString();
      const startTime = Date.now();

      const payload = {
        imageBase64: tc.buffer.toString('base64'),
        dietTags: [],
        allergies: []
      };

      let responseData = null;
      let statusCode = null;

      try {
        const res = await axios.post(`${BASE_URL}/ai/analyze-photo`, payload, {
          headers: {
            'Authorization': `Bearer ${idToken}`,
            'Content-Type': 'application/json'
          },
          timeout: 90000
        });
        statusCode = res.status;
        responseData = res.data;
      } catch (err) {
        statusCode = err.response ? err.response.status : 'TIMEOUT/ERROR';
        responseData = err.response ? err.response.data : { error: err.message };
      }

      const durationMs = Date.now() - startTime;
      const logLine = getLatestPhotoAnalyzeLogLine();
      const { provider, model } = parseProviderAndModel(logLine);

      const foods = Array.isArray(responseData?.foods) ? responseData.foods : [];
      const overallConfidence = responseData?.overallConfidence ?? 0.0;
      const totalCalories = foods.reduce((sum, f) => sum + (f.calories || 0), 0);

      const highConfItems = foods.filter(f => (f.confidence || 0) >= 0.5);

      let runPassed = false;
      if (tc.expectNoFood) {
        // PASS: black/white/non-food => foods array EMPTY (no exclusion of 0-kcal items)
        runPassed = (statusCode === 200 && foods.length === 0);
      } else {
        // PASS: thali => >= 4 items with confidence >= 0.5
        runPassed = (statusCode === 200 && highConfItems.length >= 4);
      }

      const runSummary = {
        test: tc.name,
        run,
        timestamp,
        durationMs,
        statusCode,
        provider,
        model,
        overallConfidence,
        foodsCount: foods.length,
        totalCalories,
        highConfItemsCount: highConfItems.length,
        itemsSummary: foods.map(f => `${f.name || 'Unknown'}(${f.calories || 0}kcal, ${f.estimatedGrams || 0}g, conf=${f.confidence || 0})`).join(', ') || 'None',
        passed: runPassed
      };

      results.push(runSummary);

      log(`  [Run ${run}/3] Status: ${statusCode} | Latency: ${durationMs}ms | Provider: ${provider} | Model: ${model}`);
      log(`               Conf: ${overallConfidence} | Items: ${foods.length} | High-Conf (>=0.5): ${highConfItems.length} | Calories: ${totalCalories} kcal`);
      log(`               Items: ${runSummary.itemsSummary}`);
      log(`               Result: ${runPassed ? 'PASS' : 'FAIL'}`);

      if (!runPassed) {
        log(`  [FAIL RAW JSON] ${JSON.stringify(responseData, null, 2)}`);
      }

      // Sleep >= 15s after each response to respect free-tier rate limits
      log(`  Waiting 15.5s before next call...`);
      await sleep(15500);
    }
  }

  log(`\n================================================================`);
  log(` CALIBRATION SUMMARY TABLE`);
  log(`================================================================`);
  log(`Test Target | Run | Latency | Provider | Model | Conf | Items | Calories | Status`);
  for (const r of results) {
    log(`${r.test} | Run ${r.run} | ${r.durationMs}ms | ${r.provider} | ${r.model} | ${r.overallConfidence} | ${r.foodsCount} | ${r.totalCalories} kcal | ${r.passed ? 'PASS' : 'FAIL'}`);
  }

  const allPassed = results.every(r => r.passed);
  log(`\nFINAL OUTCOME: ${allPassed ? 'ALL 12 RUNS PASSED' : 'SOME RUNS FAILED'}`);
  logStream.end();

  return allPassed;
}

runCalibration().then(success => {
  if (!success) process.exit(1);
}).catch(err => {
  console.error('Calibration script fatal error:', err);
  process.exit(1);
});
