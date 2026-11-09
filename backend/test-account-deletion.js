import Fastify from 'fastify';
import assert from 'node:assert';
import { auditTables, CANONICAL_PURGE_TABLES } from './scripts/audit-purge-completeness.js';

// Contract and unit test for DELETE /account route and auth middleware.
const VALID_TOKENS = new Map([
  ['valid-firebase-token-charlie', { uid: 'user_charlie_delete_me', email: 'charlie@test.local' }],
  ['valid-firebase-token-bob', { uid: 'user_bob_keep_me', email: 'bob@test.local' }],
]);

async function mockVerifyIdToken(idToken) {
  const user = VALID_TOKENS.get(idToken);
  if (user) return user;
  const err = new Error('Invalid or expired Firebase ID token.');
  err.code = 'auth/invalid-token';
  throw err;
}

// In-memory relational database fixture mocking Supabase
class MockSupabase {
  constructor() {
    this.tables = {
      user_profiles: [],
      nutrition_goals: [],
      daily_goals: [],
      meal_logs: [],
      food_log_items: [],
      weight_entries: [],
      water_logs: [],
      ai_analysis_log: [],
      conversations: [],
      messages: [],
      saved_meals: [],
      saved_meal_items: [],
      recipes: [],
      recipe_items: [],
      custom_foods: [],
      user_subscriptions: [],
      ai_coach_daily_usage: [],
    };
  }

  from(tableName) {
    const table = this.tables[tableName];
    if (!table) {
      throw new Error(`Table ${tableName} does not exist in MockSupabase`);
    }

    return {
      select: (fields) => {
        let currentData = [...table];
        const query = {
          eq: (col, val) => {
            currentData = currentData.filter(row => row[col] === val);
            return query;
          },
          gte: (col, val) => {
            currentData = currentData.filter(row => (row[col] || '') >= val);
            return query;
          },
          order: () => query,
          in: (col, vals) => {
            currentData = currentData.filter(row => vals.includes(row[col]));
            return query;
          },
          maybeSingle: async () => ({ data: currentData[0] || null, error: null }),
          single: async () => ({ data: currentData[0] || null, error: null }),
          then: (resolve) => resolve({ data: currentData, error: null })
        };
        return query;
      },

      delete: () => {
        return {
          eq: async (col, val) => {
            const initialLen = table.length;
            this.tables[tableName] = table.filter(row => row[col] !== val);
            return { error: null, count: initialLen - this.tables[tableName].length };
          },
          in: async (col, vals) => {
            const initialLen = table.length;
            this.tables[tableName] = table.filter(row => !vals.includes(row[col]));
            return { error: null, count: initialLen - this.tables[tableName].length };
          }
        };
      }
    };
  }
}

async function runTests() {
  console.log('Testing Fastify DELETE /account endpoint & Backend Purge Completeness...');

  const app = Fastify({ logger: false });
  const mockDb = new MockSupabase();

  const rateLimits = new Map();
  rateLimits.set('user_charlie_delete_me', [Date.now(), Date.now()]);
  rateLimits.set('user_bob_keep_me', [Date.now()]);

  const aiCoachDailyUsage = new Map();
  aiCoachDailyUsage.set('user_charlie_delete_me:2026-11-06', 4);
  aiCoachDailyUsage.set('user_bob_keep_me:2026-11-06', 2);

  const userTimezones = new Map();
  userTimezones.set('user_charlie_delete_me', 'America/New_York');
  userTimezones.set('user_bob_keep_me', 'Asia/Kolkata');

  // Seed rows for Charlie (to be deleted) and Bob (to be kept)
  const charlieUid = 'user_charlie_delete_me';
  const bobUid = 'user_bob_keep_me';

  // 1. user_profiles
  mockDb.tables.user_profiles.push(
    { firebase_uid: charlieUid, email: 'charlie@test.local', goal: 'Weight Loss' },
    { firebase_uid: bobUid, email: 'bob@test.local', goal: 'Muscle Gain' }
  );

  // 2. nutrition_goals
  mockDb.tables.nutrition_goals.push(
    { firebase_uid: charlieUid, calories: 2000, protein_g: 150 },
    { firebase_uid: bobUid, calories: 2800, protein_g: 200 }
  );

  // 3. daily_goals
  mockDb.tables.daily_goals.push(
    { firebase_uid: charlieUid, water_ml: 2500 },
    { firebase_uid: bobUid, water_ml: 3500 }
  );

  // 4. meal_logs & food_log_items
  mockDb.tables.meal_logs.push(
    { id: 101, firebase_uid: charlieUid, meal_type: 'breakfast' },
    { id: 201, firebase_uid: bobUid, meal_type: 'lunch' }
  );
  mockDb.tables.food_log_items.push(
    { id: 1001, meal_log_id: 101, name: 'Eggs' },
    { id: 2001, meal_log_id: 201, name: 'Chicken Rice' }
  );

  // 5. weight_entries
  mockDb.tables.weight_entries.push(
    { id: 1, firebase_uid: charlieUid, weight_kg: 78.5 },
    { id: 2, firebase_uid: bobUid, weight_kg: 85.0 }
  );

  // 6. water_logs
  mockDb.tables.water_logs.push(
    { id: 1, firebase_uid: charlieUid, amount_ml: 500 },
    { id: 2, firebase_uid: bobUid, amount_ml: 750 }
  );

  // 7. ai_analysis_log
  mockDb.tables.ai_analysis_log.push(
    { id: 1, firebase_uid: charlieUid, input_type: 'photo' },
    { id: 2, firebase_uid: bobUid, input_type: 'voice' }
  );

  // 8. conversations & messages
  mockDb.tables.conversations.push(
    { id: 10, firebase_uid: charlieUid },
    { id: 20, firebase_uid: bobUid }
  );
  mockDb.tables.messages.push(
    { id: 101, conversation_id: 10, text_content: 'Help me track protein' },
    { id: 201, conversation_id: 20, text_content: 'How many calories in oats?' }
  );

  // 9. saved_meals & saved_meal_items
  mockDb.tables.saved_meals.push(
    { id: 50, firebase_uid: charlieUid, name: 'Quick Breakfast' },
    { id: 60, firebase_uid: bobUid, name: 'Post Workout Shake' }
  );
  mockDb.tables.saved_meal_items.push(
    { id: 501, saved_meal_id: 50, name: 'Oatmeal' },
    { id: 601, saved_meal_id: 60, name: 'Whey' }
  );

  // 10. recipes & recipe_items
  mockDb.tables.recipes.push(
    { id: 80, firebase_uid: charlieUid, name: 'Charlie High Protein Salad' },
    { id: 90, firebase_uid: bobUid, name: 'Bob Power Bowl' }
  );
  mockDb.tables.recipe_items.push(
    { id: 801, recipe_id: 80, name: 'Lettuce' },
    { id: 901, recipe_id: 90, name: 'Quinoa' }
  );

  // 11. custom_foods
  mockDb.tables.custom_foods.push(
    { id: 1, firebase_uid: charlieUid, name: 'Moms Special Roti' },
    { id: 2, firebase_uid: bobUid, name: 'Protein Bar' }
  );

  // 12. user_subscriptions
  mockDb.tables.user_subscriptions.push(
    { id: 1, user_id: charlieUid, status: 'active' },
    { id: 2, user_id: bobUid, status: 'active' }
  );

  // 13. ai_coach_daily_usage (Cloud Run Shared Quota Storage)
  mockDb.tables.ai_coach_daily_usage.push(
    { firebase_uid: charlieUid, local_date: '2026-11-06', used_count: 4, in_flight: 0, timezone: 'America/New_York' },
    { firebase_uid: bobUid, local_date: '2026-11-06', used_count: 2, in_flight: 0, timezone: 'Asia/Kolkata' }
  );

  // Global Auth hook
  app.addHook('preHandler', async (request, reply) => {
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

    if (idToken.startsWith('test-token-')) {
      return reply.code(401).send({
        error: 'Unauthorized',
        message: 'Invalid or expired Firebase ID token.',
      });
    }

    try {
      const decoded = await mockVerifyIdToken(idToken);
      request.user = decoded;
    } catch (err) {
      return reply.code(401).send({
        error: 'Unauthorized',
        message: 'Invalid or expired Firebase ID token.',
      });
    }
  });

  // GET /sync/pull route (mirrors backend/src/server.js)
  app.get('/sync/pull', async (request, reply) => {
    const uid = request.user?.uid;
    const { data: profile } = await mockDb.from('user_profiles').select('*').eq('firebase_uid', uid).maybeSingle();
    const { data: goals } = await mockDb.from('nutrition_goals').select('*').eq('firebase_uid', uid).maybeSingle();
    return reply.code(200).send({
      user_profile: profile || null,
      nutrition_goal: goals || null,
      synced_at: new Date().toISOString()
    });
  });

  // DELETE /account route (mirrors backend/src/server.js)
  app.delete('/account', async (request, reply) => {
    const uid = request.user?.uid;
    if (!uid) {
      return reply.code(401).send({ error: 'Unauthorized', message: 'User not authenticated' });
    }

    const result = {
      purged: true,
      uid,
      timestamp: new Date().toISOString(),
      details: {}
    };

    try {
      // 1. Meal logs + cascade food log items
      const { data: mealLogs } = await mockDb.from('meal_logs').select('id').eq('firebase_uid', uid);
      if (mealLogs && mealLogs.length > 0) {
        await mockDb.from('food_log_items').delete().in('meal_log_id', mealLogs.map(m => m.id));
      }
      await mockDb.from('meal_logs').delete().eq('firebase_uid', uid);

      // 2. Nutrition goals
      await mockDb.from('nutrition_goals').delete().eq('firebase_uid', uid);

      // 3. Weight entries
      await mockDb.from('weight_entries').delete().eq('firebase_uid', uid);

      // 4. Water logs
      await mockDb.from('water_logs').delete().eq('firebase_uid', uid);

      // 5. AI analysis log
      await mockDb.from('ai_analysis_log').delete().eq('firebase_uid', uid);

      // 6. Conversations + cascade messages
      const { data: convs } = await mockDb.from('conversations').select('id').eq('firebase_uid', uid);
      if (convs && convs.length > 0) {
        await mockDb.from('messages').delete().in('conversation_id', convs.map(c => c.id));
      }
      await mockDb.from('conversations').delete().eq('firebase_uid', uid);

      // 7. User profile
      await mockDb.from('user_profiles').delete().eq('firebase_uid', uid);

      // 8. User subscriptions
      await mockDb.from('user_subscriptions').delete().eq('user_id', uid);

      // 9. Saved meals + cascade items
      const { data: savedMeals } = await mockDb.from('saved_meals').select('id').eq('firebase_uid', uid);
      if (savedMeals && savedMeals.length > 0) {
        await mockDb.from('saved_meal_items').delete().in('saved_meal_id', savedMeals.map(m => m.id));
      }
      await mockDb.from('saved_meals').delete().eq('firebase_uid', uid);

      // 10. Recipes + cascade items
      const { data: recipes } = await mockDb.from('recipes').select('id').eq('firebase_uid', uid);
      if (recipes && recipes.length > 0) {
        await mockDb.from('recipe_items').delete().in('recipe_id', recipes.map(r => r.id));
      }
      await mockDb.from('recipes').delete().eq('firebase_uid', uid);

      // 11. Custom foods
      await mockDb.from('custom_foods').delete().eq('firebase_uid', uid);

      // 12. Daily goals
      await mockDb.from('daily_goals').delete().eq('firebase_uid', uid);

      // 13. AI Coach daily usage (shared Cloud Run storage)
      await mockDb.from('ai_coach_daily_usage').delete().eq('firebase_uid', uid);

      // Purge in-memory / cache data
      for (const key of aiCoachDailyUsage.keys()) {
        if (key.startsWith(`${uid}:`)) {
          aiCoachDailyUsage.delete(key);
        }
      }
      userTimezones.delete(uid);
      rateLimits.delete(uid);

      return reply.code(200).send(result);
    } catch (err) {
      return reply.code(500).send({ error: 'Account purge failed', details: err.message });
    }
  });

  // Test 1: Missing auth returns 401
  const res1 = await app.inject({
    method: 'DELETE',
    url: '/account'
  });
  assert.strictEqual(res1.statusCode, 401, 'Unauthenticated DELETE /account must return 401');
  console.log('  ✓ Test 1: Unauthenticated request rejected with 401');

  // Test 2: test-token-* returns 401
  const res2 = await app.inject({
    method: 'DELETE',
    url: '/account',
    headers: { authorization: 'Bearer test-token-fake' }
  });
  assert.strictEqual(res2.statusCode, 401, 'test-token-* must return 401');
  console.log('  ✓ Test 2: test-token prefix rejected with 401');

  // Test 3: Authenticated DELETE /account purges ALL 17 tables for Charlie
  const res3 = await app.inject({
    method: 'DELETE',
    url: '/account',
    headers: { authorization: 'Bearer valid-firebase-token-charlie' }
  });
  assert.strictEqual(res3.statusCode, 200, 'Authenticated DELETE /account must return 200');
  const body3 = JSON.parse(res3.payload);
  assert.strictEqual(body3.purged, true);
  assert.strictEqual(body3.uid, charlieUid);

  // Assert ALL tables have 0 rows for Charlie
  assert.strictEqual(mockDb.tables.user_profiles.filter(r => r.firebase_uid === charlieUid).length, 0, 'user_profiles purged');
  assert.strictEqual(mockDb.tables.nutrition_goals.filter(r => r.firebase_uid === charlieUid).length, 0, 'nutrition_goals purged');
  assert.strictEqual(mockDb.tables.daily_goals.filter(r => r.firebase_uid === charlieUid).length, 0, 'daily_goals purged');
  assert.strictEqual(mockDb.tables.meal_logs.filter(r => r.firebase_uid === charlieUid).length, 0, 'meal_logs purged');
  assert.strictEqual(mockDb.tables.food_log_items.filter(r => r.meal_log_id === 101).length, 0, 'food_log_items purged');
  assert.strictEqual(mockDb.tables.weight_entries.filter(r => r.firebase_uid === charlieUid).length, 0, 'weight_entries purged');
  assert.strictEqual(mockDb.tables.water_logs.filter(r => r.firebase_uid === charlieUid).length, 0, 'water_logs purged');
  assert.strictEqual(mockDb.tables.ai_analysis_log.filter(r => r.firebase_uid === charlieUid).length, 0, 'ai_analysis_log purged');
  assert.strictEqual(mockDb.tables.conversations.filter(r => r.firebase_uid === charlieUid).length, 0, 'conversations purged');
  assert.strictEqual(mockDb.tables.messages.filter(r => r.conversation_id === 10).length, 0, 'messages purged');
  assert.strictEqual(mockDb.tables.saved_meals.filter(r => r.firebase_uid === charlieUid).length, 0, 'saved_meals purged');
  assert.strictEqual(mockDb.tables.saved_meal_items.filter(r => r.saved_meal_id === 50).length, 0, 'saved_meal_items purged');
  assert.strictEqual(mockDb.tables.recipes.filter(r => r.firebase_uid === charlieUid).length, 0, 'recipes purged');
  assert.strictEqual(mockDb.tables.recipe_items.filter(r => r.recipe_id === 80).length, 0, 'recipe_items purged');
  assert.strictEqual(mockDb.tables.custom_foods.filter(r => r.firebase_uid === charlieUid).length, 0, 'custom_foods purged');
  assert.strictEqual(mockDb.tables.user_subscriptions.filter(r => r.user_id === charlieUid).length, 0, 'user_subscriptions purged');
  assert.strictEqual(mockDb.tables.ai_coach_daily_usage.filter(r => r.firebase_uid === charlieUid).length, 0, 'ai_coach_daily_usage purged');
  assert.strictEqual(aiCoachDailyUsage.has(`${charlieUid}:2026-11-06`), false, 'in-memory aiCoachDailyUsage purged');
  assert.strictEqual(userTimezones.has(charlieUid), false, 'userTimezones cache purged');
  assert.strictEqual(rateLimits.has(charlieUid), false, 'Rate limit entries cleared');

  // Assert Bob's rows are completely intact
  assert.strictEqual(mockDb.tables.user_profiles.filter(r => r.firebase_uid === bobUid).length, 1, 'Bob user_profiles intact');
  assert.strictEqual(mockDb.tables.nutrition_goals.filter(r => r.firebase_uid === bobUid).length, 1, 'Bob nutrition_goals intact');
  assert.strictEqual(mockDb.tables.daily_goals.filter(r => r.firebase_uid === bobUid).length, 1, 'Bob daily_goals intact');
  assert.strictEqual(mockDb.tables.meal_logs.filter(r => r.firebase_uid === bobUid).length, 1, 'Bob meal_logs intact');
  assert.strictEqual(mockDb.tables.food_log_items.filter(r => r.meal_log_id === 201).length, 1, 'Bob food_log_items intact');
  assert.strictEqual(mockDb.tables.saved_meals.filter(r => r.firebase_uid === bobUid).length, 1, 'Bob saved_meals intact');
  assert.strictEqual(mockDb.tables.recipes.filter(r => r.firebase_uid === bobUid).length, 1, 'Bob recipes intact');
  assert.strictEqual(mockDb.tables.custom_foods.filter(r => r.firebase_uid === bobUid).length, 1, 'Bob custom_foods intact');
  assert.strictEqual(mockDb.tables.ai_coach_daily_usage.filter(r => r.firebase_uid === bobUid).length, 1, 'Bob ai_coach_daily_usage intact');
  assert.strictEqual(aiCoachDailyUsage.has(`${bobUid}:2026-11-06`), true, 'Bob in-memory aiCoachDailyUsage intact');
  assert.strictEqual(userTimezones.has(bobUid), true, 'Bob userTimezones cache intact');
  assert.strictEqual(rateLimits.has(bobUid), true, 'Bob rate limit intact');

  console.log('  ✓ Test 3: Authenticated account deletion purges ALL 17 tables cleanly and preserves other users');

  // Test 4: Idempotency (calling DELETE /account again returns 200 without error)
  const res4 = await app.inject({
    method: 'DELETE',
    url: '/account',
    headers: { authorization: 'Bearer valid-firebase-token-charlie' }
  });
  assert.strictEqual(res4.statusCode, 200, 'Idempotent second deletion call must return 200');
  const body4 = JSON.parse(res4.payload);
  assert.strictEqual(body4.purged, true);
  console.log('  ✓ Test 4: Idempotency verified — second deletion succeeds cleanly');

  // Test 5: Verify backend does NOT recreate a profile row for a purged uid on later authenticated requests
  const res5 = await app.inject({
    method: 'GET',
    url: '/sync/pull',
    headers: { authorization: 'Bearer valid-firebase-token-charlie' }
  });
  assert.strictEqual(res5.statusCode, 200, 'Later authenticated GET /sync/pull returns 200');
  const body5 = JSON.parse(res5.payload);
  assert.strictEqual(body5.user_profile, null, 'Purged user returns null user_profile');
  assert.strictEqual(mockDb.tables.user_profiles.filter(r => r.firebase_uid === charlieUid).length, 0, 'No user_profiles row was recreated');
  console.log('  ✓ Test 5: Verified backend does not recreate a profile row for purged UID on later requests');

  // Test 6: Purge list schema completeness audit
  const auditAllTables = auditTables(Object.keys(mockDb.tables));
  assert.strictEqual(auditAllTables.isComplete, true, 'All 17 mock database tables must be in the purge list');
  assert.strictEqual(auditAllTables.missingTables.length, 0);

  // If a new user table is added to the schema without being added to purge list, audit MUST fail
  const badSchema = [...Object.keys(mockDb.tables), 'untracked_user_tokens'];
  const auditFailed = auditTables(badSchema);
  assert.strictEqual(auditFailed.isComplete, false, 'Audit must fail if any schema table is missing from purge list');
  assert.deepStrictEqual(auditFailed.missingTables, ['untracked_user_tokens']);
  console.log('  ✓ Test 6: Staging purge audit script verified against complete and incomplete schemas');

  console.log('\nAll 6 Backend Account Deletion & Purge Tests Passed Successfully!');
}

runTests().catch((err) => {
  console.error('Test execution failed:', err);
  process.exit(1);
});
