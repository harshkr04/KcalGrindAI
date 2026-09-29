/**
 * Phase 10 — Supabase Cloud Sync Integration Tests
 *
 * Tests the /sync/push and /sync/pull routes end-to-end.
 * Auth uses a genuine Firebase token from the Auth emulator.
 * There is NO NODE_ENV=test / test-token bypass in src/server.js by design.
 *
 * Run: firebase emulators:start --only auth & BASE_URL=http://localhost:8000 node test-sync-routes.js
 */

import { readFileSync, readdirSync, existsSync } from 'fs';
import { join, resolve } from 'path';

const BASE_URL = process.env.BASE_URL || 'http://localhost:8000';
let AUTH_HEADER = {};
let TEST_UID = '';

async function setupAuth() {
  const emulatorHost = process.env.FIREBASE_AUTH_EMULATOR_HOST || '127.0.0.1:9099';
  const res = await fetch(`http://${emulatorHost}/identitytoolkit.googleapis.com/v1/accounts:signUp?key=anything`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ returnSecureToken: true })
  });
  const data = await res.json();
  TEST_UID = data.localId;
  AUTH_HEADER = { 'Authorization': `Bearer ${data.idToken}` };
  console.log(`  ✓ Acquired genuine Firebase token from emulator (UID: ${TEST_UID})`);
}

let passed = 0;
let failed = 0;

function assert(condition, message) {
  if (condition) {
    console.log(`  ✓ ${message}`);
    passed++;
  } else {
    console.error(`  ✗ FAIL: ${message}`);
    failed++;
  }
}

async function fetchJson(path, options = {}) {
  const url = `${BASE_URL}${path}`;
  const response = await fetch(url, {
    headers: { ...AUTH_HEADER, 'Content-Type': 'application/json', ...options.headers },
    ...options
  });
  const body = await response.json();
  return { status: response.status, body };
}

// =============================================================
// Test 1: Health check
// =============================================================
async function testHealthCheck() {
  console.log('\n--- Test 1: Health Check ---');
  const { status, body } = await fetchJson('/health', {
    headers: {} // health doesn't need auth
  });
  assert(status === 200, `Health returns 200 (got ${status})`);
  assert(body.status === 'ok', `Status is ok`);
}

// =============================================================
// Test 2: Push sync — full data set
// =============================================================
async function testSyncPush() {
  console.log('\n--- Test 2: Sync Push ---');

  const now = Date.now();
  const pushPayload = {
    user_profile: {
      email: 'test@kcalgrindai.test',
      goal: 'lose_weight',
      units: 'metric',
      age: 28,
      heightCm: 175.0,
      weightKg: 80.0,
      goalWeightKg: 72.0,
      activityLevel: 'moderate',
      dietTags: ['vegetarian', 'low_carb'],
      allergies: ['peanuts'],
      targetsSource: 'recommended',
      createdAt: now - 86400000,
      updatedAt: now
    },
    nutrition_goal: {
      calories: 1800,
      proteinG: 120.0,
      carbsG: 150.0,
      fatG: 60.0,
      waterLiters: 2.5,
      waterGlasses: 10,
      bmr: 1750.0,
      tdee: 2200.0,
      isCustom: false,
      updatedAt: now
    },
    meal_logs: [
      {
        id: 1001,
        date: '2026-09-10',
        mealType: 'breakfast',
        totalCalories: 450.0,
        loggedAt: now - 3600000,
        source: 'manual',
        synced: false
      },
      {
        id: 1002,
        date: '2026-09-10',
        mealType: 'lunch',
        totalCalories: 650.0,
        loggedAt: now,
        source: 'ai_photo',
        synced: false
      }
    ],
    food_log_items: [
      {
        id: 2001,
        mealLogId: 1001,
        foodId: null,
        name: 'Oatmeal with Berries',
        brand: null,
        servingDescription: '1 bowl',
        servingGrams: 250.0,
        calories: 300.0,
        proteinG: 10.0,
        carbsG: 50.0,
        fatG: 8.0,
        fiberG: 5.0,
        source: 'manual',
        confidence: null,
        confirmed: true
      },
      {
        id: 2002,
        mealLogId: 1001,
        foodId: null,
        name: 'Black Coffee',
        brand: null,
        servingDescription: '1 cup',
        servingGrams: 240.0,
        calories: 5.0,
        proteinG: 0.3,
        carbsG: 0.0,
        fatG: 0.0,
        fiberG: 0.0,
        source: 'manual',
        confidence: null,
        confirmed: true
      }
    ],
    weight_entries: [
      {
        id: 3001,
        weightKg: 80.2,
        date: '2026-09-10',
        note: 'Morning weigh-in',
        loggedAt: now
      }
    ],
    water_logs: [
      {
        id: 4001,
        date: '2026-09-10',
        amountMl: 250,
        loggedAt: now
      }
    ],
    ai_analyses: [
      {
        id: 5001,
        inputType: 'photo',
        rawInputRef: '/storage/photos/meal_001.jpg',
        resultJson: '{"foods":[{"name":"Rice","calories":200}],"overallConfidence":0.85}',
        overallConfidence: 0.85,
        createdAt: now
      }
    ],
    conversations: [
      {
        id: 6001,
        startedAt: now - 7200000,
        lastMessageAt: now
      }
    ],
    messages: [
      {
        id: 7001,
        conversationId: 6001,
        role: 'user',
        text: 'How many calories are in a banana?',
        structuredDataJson: null,
        createdAt: now - 3600000
      },
      {
        id: 7002,
        conversationId: 6001,
        role: 'assistant',
        text: 'A medium banana has about 105 calories.',
        structuredDataJson: null,
        createdAt: now - 3500000
      }
    ]
  };

  const { status, body } = await fetchJson('/sync/push', {
    method: 'POST',
    body: JSON.stringify(pushPayload)
  });

  assert(status === 200, `Push returns 200 (got ${status})`);
  assert(body.success === true, `Push reports success`);
  assert(body.summary?.pushed?.user_profile === 1, `User profile pushed`);
  assert(body.summary?.pushed?.nutrition_goal === 1, `Nutrition goal pushed`);
  assert(body.summary?.pushed?.meal_logs >= 1, `Meal logs pushed (${body.summary?.pushed?.meal_logs})`);
  assert(body.summary?.pushed?.food_log_items >= 1, `Food log items pushed (${body.summary?.pushed?.food_log_items})`);
  assert(body.summary?.pushed?.weight_entries === 1, `Weight entry pushed`);
  assert(body.summary?.pushed?.water_logs === 1, `Water log pushed`);
  assert(body.summary?.pushed?.ai_analyses === 1, `AI analysis pushed`);
  console.log('  Push summary:', JSON.stringify(body.summary, null, 2));
}

// =============================================================
// Test 3: Pull sync — retrieve pushed data
// =============================================================
async function testSyncPull() {
  console.log('\n--- Test 3: Sync Pull ---');

  const { status, body } = await fetchJson('/sync/pull?since=1970-01-01T00:00:00Z');

  assert(status === 200, `Pull returns 200 (got ${status})`);
  assert(body.user_profile !== null, `User profile returned`);
  assert(body.user_profile?.goal === 'lose_weight', `Goal matches pushed data`);
  assert(body.nutrition_goal !== null, `Nutrition goal returned`);
  assert(body.nutrition_goal?.calories === 1800, `Calories match pushed data`);
  assert(Array.isArray(body.meal_logs) && body.meal_logs.length >= 1, `Meal logs returned (${body.meal_logs?.length})`);
  assert(Array.isArray(body.food_log_items) && body.food_log_items.length >= 1, `Food log items returned (${body.food_log_items?.length})`);
  assert(Array.isArray(body.weight_entries) && body.weight_entries.length >= 1, `Weight entries returned`);
  assert(Array.isArray(body.water_logs) && body.water_logs.length >= 1, `Water logs returned`);
  assert(body.synced_at != null, `synced_at timestamp returned`);
}

// =============================================================
// Test 4: Conflict resolution — last-write-wins
// =============================================================
async function testConflictResolution() {
  console.log('\n--- Test 4: Conflict Resolution (Last-Write-Wins) ---');

  const now = Date.now();

  // Push a meal log with a recent timestamp
  const newerPayload = {
    meal_logs: [{
      id: 1001,
      date: '2026-09-10',
      mealType: 'breakfast',
      totalCalories: 500.0, // updated value
      loggedAt: now + 10000, // newer than first push
      source: 'manual',
      synced: false
    }],
    food_log_items: []
  };

  const { status: newerStatus, body: newerBody } = await fetchJson('/sync/push', {
    method: 'POST',
    body: JSON.stringify(newerPayload)
  });
  assert(newerStatus === 200, `Newer push succeeds`);
  assert(newerBody.summary?.pushed?.meal_logs === 1, `Newer meal log overwrites (pushed=1)`);

  // Now push an OLDER timestamp for the same meal — should be skipped
  const olderPayload = {
    meal_logs: [{
      id: 1001,
      date: '2026-09-10',
      mealType: 'breakfast',
      totalCalories: 300.0, // older value
      loggedAt: now - 86400000, // much older
      source: 'manual',
      synced: false
    }],
    food_log_items: []
  };

  const { status: olderStatus, body: olderBody } = await fetchJson('/sync/push', {
    method: 'POST',
    body: JSON.stringify(olderPayload)
  });
  assert(olderStatus === 200, `Older push returns 200`);
  assert(olderBody.summary?.skipped?.meal_logs === 1, `Older meal log skipped (skipped=1)`);

  // Verify the value in Supabase is the newer one (500 kcal, not 300)
  const { body: pullBody } = await fetchJson('/sync/pull?since=1970-01-01T00:00:00Z');
  const meal = pullBody.meal_logs?.find(m => m.local_id === 1001);
  assert(meal != null, `Meal log 1001 found in pull`);
  assert(meal?.total_calories === 500.0, `Total calories = 500 (newer wins, got ${meal?.total_calories})`);
}

// =============================================================
// Test 5: Service-role key NOT in Android source
// =============================================================
async function testServiceKeyAbsentFromAndroid() {
  console.log('\n--- Test 5: Service-Role Key Absent From Android ---');

  const androidRoot = resolve(process.cwd(), '..', 'app', 'src');
  if (!existsSync(androidRoot)) {
    console.log('  ⚠ Android src not found at expected path, skipping file scan');
    return;
  }

  function scanDir(dir) {
    let found = false;
    try {
      const entries = readdirSync(dir, { withFileTypes: true });
      for (const entry of entries) {
        const fullPath = join(dir, entry.name);
        if (entry.isDirectory()) {
          if (scanDir(fullPath)) found = true;
        } else if (entry.name.endsWith('.kt') || entry.name.endsWith('.java') || entry.name.endsWith('.xml')) {
          const content = readFileSync(fullPath, 'utf-8');
          if (content.includes('SUPABASE_SERVICE_KEY') || content.includes('supabase_service_key')) {
            console.error(`    Found SUPABASE_SERVICE_KEY reference in: ${fullPath}`);
            found = true;
          }
        }
      }
    } catch { /* permission errors etc */ }
    return found;
  }

  const found = scanDir(androidRoot);
  assert(!found, 'No SUPABASE_SERVICE_KEY references found in Android source');
}

// =============================================================
// Run all tests
// =============================================================
async function main() {
  console.log('=== Phase 10 — Supabase Cloud Sync Tests ===');
  console.log(`Backend: ${BASE_URL}`);
  console.log('');

  try {
    await setupAuth();
    await testHealthCheck();
    await testSyncPush();
    await testSyncPull();
    await testConflictResolution();
    await testServiceKeyAbsentFromAndroid();
  } catch (err) {
    console.error('\n💥 Unexpected error:', err);
    failed++;
  }

  console.log(`\n${'='.repeat(50)}`);
  console.log(`Results: ${passed} passed, ${failed} failed, ${passed + failed} total`);
  console.log(`${'='.repeat(50)}`);
  process.exit(failed > 0 ? 1 : 0);
}

main();
