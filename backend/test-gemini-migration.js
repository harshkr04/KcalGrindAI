import 'dotenv/config';
import fs from 'fs';
import path from 'path';
import axios from 'axios';
import { spawn } from 'child_process';

const PORT = 8009;
const BASE_URL = `http://127.0.0.1:${PORT}`;
const AUTH_HEADER = { Authorization: 'Bearer test-token-migrator-user' };

console.log('--- Starting Gemini Migration Test Suite ---');

let serverProcess = null;
const serverLogs = [];

async function startTestServer() {
  return new Promise((resolve, reject) => {
    serverProcess = spawn(process.execPath || 'node', ['src/server.js'], {
      cwd: process.cwd(),
      env: {
        ...process.env,
        PORT: PORT.toString(),
        HOST: '127.0.0.1',
        NODE_ENV: 'test'
      },
      stdio: ['pipe', 'pipe', 'pipe']
    });

    let started = false;

    serverProcess.stdout.on('data', data => {
      const out = data.toString();
      serverLogs.push(out);
      process.stdout.write('[Server]: ' + out);
      if (out.includes(`running on http://`)) {
        started = true;
        resolve();
      }
    });

    serverProcess.stderr.on('data', data => {
      const err = data.toString();
      process.stderr.write('[Server ERR]: ' + err);
    });

    serverProcess.on('error', err => {
      if (!started) reject(err);
    });

    setTimeout(() => {
      if (!started) reject(new Error('Server failed to start within timeout'));
    }, 10000);
  });
}

async function runTests() {
  try {
    console.log(`Starting backend server on port ${PORT}...`);
    await startTestServer();
    console.log('Backend server started successfully.');

    // 1. Health Check
    console.log('\n[Test 1] Testing /health endpoint...');
    const healthRes = await axios.get(`${BASE_URL}/health`);
    console.log('Health Response:', JSON.stringify(healthRes.data, null, 2));
    if (healthRes.data.status !== 'ok') throw new Error('Health status is not ok');
    if (healthRes.data.provider !== 'gemini') {
      throw new Error(`Expected provider 'gemini', but got '${healthRes.data.provider}'!`);
    }
    if (healthRes.data.visionModel !== 'gemini-2.5-flash' || healthRes.data.chatModel !== 'gemini-2.5-flash') {
      throw new Error(`Expected gemini-2.5-flash models, got ${healthRes.data.visionModel} / ${healthRes.data.chatModel}`);
    }
    console.log('✓ Health check passed: Provider is GEMINI (gemini-2.5-flash)');

    // 2. Auth Protection on /ai/analyze-text
    console.log('\n[Test 2] Verifying 401 Unauthorized without token on /ai/analyze-text...');
    try {
      await axios.post(`${BASE_URL}/ai/analyze-text`, { text: 'Apple' });
      throw new Error('Expected 401 but request succeeded');
    } catch (err) {
      if (err.response && err.response.status === 401) {
        console.log('✓ Expected 401 received:', err.response.data);
      } else {
        throw err;
      }
    }

    // 3. Text Meal Analysis (Gemini 2.5 Flash)
    console.log('\n[Test 3] Testing /ai/analyze-text (Gemini 2.5 Flash structured text)...');
    const textRes = await axios.post(
      `${BASE_URL}/ai/analyze-text`,
      { text: '2 scrambled eggs and 1 slice whole wheat toast with butter' },
      { headers: AUTH_HEADER }
    );
    console.log('Text Analysis Result:');
    console.log(JSON.stringify(textRes.data, null, 2));
    if (!Array.isArray(textRes.data.foods) || textRes.data.foods.length === 0) {
      throw new Error('Foods array missing or empty');
    }
    if (typeof textRes.data.overallConfidence !== 'number') {
      throw new Error('overallConfidence missing or not a number');
    }
    const firstFood = textRes.data.foods[0];
    if (!firstFood.name || !firstFood.calories || !firstFood.macros) {
      throw new Error('Food item missing essential fields (name, calories, macros)');
    }
    console.log('✓ /ai/analyze-text passed Gemini structured output validation');

    // 4. Photo Meal Analysis (Gemini 2.5 Flash Multimodal Vision)
    console.log('\n[Test 4] Testing /ai/analyze-photo (Gemini 2.5 Flash Multimodal Vision)...');
    let sampleJpegBase64 = '/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA=';
    let mimeType = 'image/jpeg';
    const realImgPath = path.resolve(process.cwd(), '../food_detected/screen.png');
    if (fs.existsSync(realImgPath)) {
      sampleJpegBase64 = fs.readFileSync(realImgPath).toString('base64');
      mimeType = 'image/png';
    }
    const photoRes = await axios.post(
      `${BASE_URL}/ai/analyze-photo`,
      {
        imageBase64: `data:${mimeType};base64,${sampleJpegBase64}`,
        dietTags: ['high-protein'],
        allergies: []
      },
      { headers: AUTH_HEADER }
    );
    console.log('Photo Analysis Result:');
    console.log(JSON.stringify(photoRes.data, null, 2));
    if (!Array.isArray(photoRes.data.foods)) {
      throw new Error('Foods array missing in photo response');
    }
    console.log(`✓ /ai/analyze-photo passed Gemini multimodal vision validation (${photoRes.data.foods.length} items detected)`);

    // 5. AI Coach Chat: Water Logging Tool
    console.log('\n[Test 5] Testing /ai/chat tool calling (log_water via Gemini 2.5 Flash)...');
    const waterChatRes = await axios.post(
      `${BASE_URL}/ai/chat`,
      {
        messages: [{ role: 'user', content: 'Please log a glass of water' }],
        userContext: { goal: 'Lose weight', targetCalories: 2100, consumedCalories: 500 }
      },
      { headers: AUTH_HEADER }
    );
    console.log('Water Chat Reply:', waterChatRes.data.reply);
    console.log('Tool Invoked:', waterChatRes.data.toolInvoked);
    console.log('Suggested Action:', waterChatRes.data.suggestedAction);
    if (waterChatRes.data.toolInvoked !== 'log_water') {
      throw new Error(`Expected toolInvoked = 'log_water', got ${waterChatRes.data.toolInvoked}`);
    }
    if (!waterChatRes.data.suggestedAction || !waterChatRes.data.suggestedAction.includes('auto_logged_water')) {
      throw new Error('suggestedAction missing auto_logged_water');
    }
    console.log('✓ /ai/chat log_water tool passed');

    // 6. AI Coach Chat: Food Logging Tool
    console.log('\n[Test 6] Testing /ai/chat tool calling (create_food_log via Gemini 2.5 Flash)...');
    const foodChatRes = await axios.post(
      `${BASE_URL}/ai/chat`,
      {
        messages: [{ role: 'user', content: 'Log a banana for snack' }],
        userContext: { goal: 'Lose weight', targetCalories: 2100, consumedCalories: 500 }
      },
      { headers: AUTH_HEADER }
    );
    console.log('Food Chat Reply:', foodChatRes.data.reply);
    console.log('Tool Invoked:', foodChatRes.data.toolInvoked);
    console.log('Suggested Action:', foodChatRes.data.suggestedAction);
    if (foodChatRes.data.toolInvoked !== 'create_food_log') {
      throw new Error(`Expected toolInvoked = 'create_food_log', got ${foodChatRes.data.toolInvoked}`);
    }
    if (!foodChatRes.data.suggestedAction || !foodChatRes.data.suggestedAction.includes('confirm_food_log')) {
      throw new Error('suggestedAction missing confirm_food_log');
    }
    console.log('✓ /ai/chat create_food_log tool passed');

    console.log('\n======================================================');
    console.log('ALL GEMINI 2.5 FLASH MIGRATION TESTS PASSED (6/6)!');
    console.log('======================================================\n');
  } finally {
    if (serverProcess) {
      serverProcess.kill();
    }
  }
}

runTests().catch(err => {
  console.error('\nTest Suite Failed:', err.message);
  if (err.response) {
    console.error('Response Status:', err.response.status);
    console.error('Response Data:', err.response.data);
  }
  if (serverProcess) {
    serverProcess.kill();
  }
  process.exit(1);
});
