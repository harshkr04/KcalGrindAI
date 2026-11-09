process.env.TEST = 'true';
if (!process.env.FIREBASE_PROJECT_ID) {
  process.env.FIREBASE_PROJECT_ID = 'test-kcalgrind-ci';
}
if (!process.env.GEMINI_API_KEY) {
  process.env.GEMINI_API_KEY = 'mock-ci-gemini-key';
}
import assert from 'node:assert';

// Mock Supabase with support for RPC functions, ai_coach_daily_usage and user_profiles tables
class MockSupabaseSharedStorage {
  constructor() {
    this.tables = {
      ai_coach_daily_usage: [],
      user_profiles: []
    };
  }

  from(tableName) {
    const table = this.tables[tableName] || [];
    return {
      select: (fields) => {
        let current = [...table];
        const query = {
          eq: (col, val) => {
            current = current.filter(r => r[col] === val);
            return query;
          },
          gte: (col, val) => {
            current = current.filter(r => r[col] >= val);
            return query;
          },
          maybeSingle: async () => ({ data: current[0] ? { ...current[0] } : null, error: null }),
          single: async () => ({ data: current[0] ? { ...current[0] } : null, error: null })
        };
        return query;
      },
      insert: async (rowOrRows) => {
        const rows = Array.isArray(rowOrRows) ? rowOrRows : [rowOrRows];
        for (const r of rows) {
          table.push({ ...r });
        }
        return { data: rows, error: null };
      },
      update: (updates) => {
        return {
          eq: (col1, val1) => {
            const query = {
              eq: async (col2, val2) => {
                let count = 0;
                for (let i = 0; i < table.length; i++) {
                  if (table[i][col1] === val1 && table[i][col2] === val2) {
                    Object.assign(table[i], updates);
                    count++;
                  }
                }
                return { data: table, error: null, count };
              },
              then: (resolve) => {
                let count = 0;
                for (let i = 0; i < table.length; i++) {
                  if (table[i][col1] === val1) {
                    Object.assign(table[i], updates);
                    count++;
                  }
                }
                resolve({ data: table, error: null, count });
              }
            };
            return query;
          }
        };
      },
      delete: () => {
        return {
          eq: (col1, val1) => {
            const chain = {
              eq: async (col2, val2) => {
                const initLen = table.length;
                this.tables[tableName] = table.filter(r => !(r[col1] === val1 && r[col2] === val2));
                return { error: null, count: initLen - this.tables[tableName].length };
              },
              then: (resolve) => {
                const initLen = table.length;
                this.tables[tableName] = table.filter(r => r[col1] !== val1);
                resolve({ error: null, count: initLen - this.tables[tableName].length });
              }
            };
            return chain;
          }
        };
      }
    };
  }

  async rpc(fnName, params) {
    const now = Date.now();
    if (fnName === 'reserve_ai_coach_quota') {
      const { p_uid, p_date, p_limit, p_timezone } = params;
      const table = this.tables.ai_coach_daily_usage;

      // Enforce rolling 24-hour cap backstop (2x daily limit)
      // Allows legitimate users to use 5 messages late at night + 5 messages after midnight (total 10 in 24h)
      // while preventing malicious timezone hopping from exceeding 2x daily allowance.
      const rolling24hUsed = table
        .filter(r => r.firebase_uid === p_uid && (now - new Date(r.updated_at).getTime()) <= 24 * 60 * 60 * 1000)
        .reduce((sum, r) => sum + (r.used_count || 0), 0);

      const rollingCap = p_limit * 2;
      if (rolling24hUsed >= rollingCap) {
        return {
          data: {
            allowed: false,
            usedToday: rolling24hUsed,
            inFlight: 0,
            dailyLimit: p_limit,
            remaining: 0,
            isLimitReached: true,
            rolling24hCapReached: true,
            localDay: p_date
          },
          error: null
        };
      }

      const row = table.find(r => r.firebase_uid === p_uid && r.local_date === p_date);

      if (!row) {
        if (p_limit > 0) {
          table.push({
            firebase_uid: p_uid,
            local_date: p_date,
            used_count: 0,
            in_flight: 1,
            last_reservation_at: new Date(now).toISOString(),
            timezone: p_timezone || 'UTC',
            updated_at: new Date(now).toISOString()
          });
          return {
            data: {
              allowed: true,
              usedToday: 0,
              inFlight: 1,
              dailyLimit: p_limit,
              remaining: p_limit - 1,
              isLimitReached: false,
              localDay: p_date
            },
            error: null
          };
        }
        return {
          data: {
            allowed: false,
            usedToday: 0,
            inFlight: 0,
            dailyLimit: p_limit,
            remaining: 0,
            isLimitReached: true,
            localDay: p_date
          },
          error: null
        };
      }

      const used = row.used_count || 0;
      const isStale = row.last_reservation_at && (now - new Date(row.last_reservation_at).getTime() > 2 * 60 * 1000);
      const effectiveInFlight = isStale ? 0 : (row.in_flight || 0);

      if (used + effectiveInFlight < p_limit) {
        const nextInFlight = effectiveInFlight + 1;
        row.in_flight = nextInFlight;
        row.last_reservation_at = new Date(now).toISOString();
        row.updated_at = new Date(now).toISOString();
        return {
          data: {
            allowed: true,
            usedToday: used,
            inFlight: nextInFlight,
            dailyLimit: p_limit,
            remaining: Math.max(0, p_limit - (used + nextInFlight)),
            isLimitReached: false,
            localDay: p_date
          },
          error: null
        };
      }

      return {
        data: {
          allowed: false,
          usedToday: used,
          inFlight: effectiveInFlight,
          dailyLimit: p_limit,
          remaining: 0,
          isLimitReached: true,
          localDay: p_date
        },
        error: null
      };
    } else if (fnName === 'release_ai_coach_reservation') {
      const { p_uid, p_date } = params;
      const row = this.tables.ai_coach_daily_usage.find(r => r.firebase_uid === p_uid && r.local_date === p_date);
      if (row) {
        row.in_flight = Math.max(0, (row.in_flight || 1) - 1);
        row.updated_at = new Date().toISOString();
      }
      return { data: null, error: null };
    } else if (fnName === 'commit_ai_coach_quota') {
      const { p_uid, p_date, p_timezone } = params;
      let row = this.tables.ai_coach_daily_usage.find(r => r.firebase_uid === p_uid && r.local_date === p_date);
      if (row) {
        row.used_count = (row.used_count || 0) + 1;
        row.in_flight = Math.max(0, (row.in_flight || 1) - 1);
        row.updated_at = new Date().toISOString();
        return { data: row.used_count, error: null };
      }
      this.tables.ai_coach_daily_usage.push({
        firebase_uid: p_uid,
        local_date: p_date,
        used_count: 1,
        in_flight: 0,
        timezone: p_timezone || 'UTC',
        updated_at: new Date().toISOString()
      });
      return { data: 1, error: null };
    }
    return { data: null, error: new Error(`Unknown RPC ${fnName}`) };
  }
}

const {
  setSupabaseClient,
  getAiCoachUsageAsync,
  tryReserveAiCoachQuotaAsync,
  commitAiCoachQuotaAsync,
  releaseAiCoachReservationAsync,
  resetAiCoachUsageForTesting,
  resetAiCoachUsageForTestingAsync,
  resolveUserTimezoneAsync,
  getUserLocalDate
} = await import('./src/server.js');

async function runSharedQuotaTests() {
  console.log('=== Running AI Coach Supabase Shared Quota & Cloud Run Scaling Tests ===\n');

  const mockDb = new MockSupabaseSharedStorage();
  setSupabaseClient(mockDb);

  const tz = 'America/New_York';
  const today = getUserLocalDate(tz);

  // Test 1: Clean initialization and empty usage query
  console.log('Test 1: Clean initialization and empty usage query');
  const userAlice = `user_alice_shared_${Date.now()}`;
  await resetAiCoachUsageForTestingAsync(userAlice);

  let usage = await getAiCoachUsageAsync(userAlice, tz, false);
  assert.strictEqual(usage.usedToday, 0, 'Alice usedToday must start at 0');
  assert.strictEqual(usage.inFlight, 0, 'Alice inFlight must start at 0');
  assert.strictEqual(usage.dailyLimit, 5, 'Alice dailyLimit must be 5');
  assert.strictEqual(usage.remaining, 5, 'Alice remaining must be 5');
  assert.strictEqual(usage.isLimitReached, false, 'Alice limit must not be reached');
  console.log('✓ Initial usage query returns 0 used, 5 remaining\n');

  // Test 2: Atomic Reservation in Supabase Shared Table
  console.log('Test 2: Atomic reservation in Supabase shared table via RPC / atomic query');
  const res1 = await tryReserveAiCoachQuotaAsync(userAlice, tz, false);
  assert.strictEqual(res1.allowed, true, 'Reservation 1 must be allowed');
  assert.strictEqual(res1.inFlight, 1, 'inFlight must be 1');
  assert.strictEqual(res1.remaining, 4, 'Remaining must be 4');

  // Verify row was inserted into Supabase table
  const row1 = mockDb.tables.ai_coach_daily_usage.find(r => r.firebase_uid === userAlice && r.local_date === today);
  assert.ok(row1, 'Supabase table row must exist');
  assert.strictEqual(row1.in_flight, 1, 'Supabase row in_flight must be 1');
  assert.strictEqual(row1.used_count, 0, 'Supabase row used_count must be 0');
  assert.ok(row1.last_reservation_at, 'last_reservation_at timestamp must be recorded');
  console.log('✓ Supabase row created with in_flight: 1 and last_reservation_at timestamp\n');

  // Test 3: Commit updates used_count and decrements in_flight in Supabase
  console.log('Test 3: Commit quota updates used_count and decrements in_flight');
  const newUsed = await commitAiCoachQuotaAsync(userAlice, tz);
  assert.strictEqual(newUsed, 1, 'Committed used count must be 1');

  const rowAfterCommit = mockDb.tables.ai_coach_daily_usage.find(r => r.firebase_uid === userAlice && r.local_date === today);
  assert.strictEqual(rowAfterCommit.used_count, 1, 'Supabase row used_count must be 1');
  assert.strictEqual(rowAfterCommit.in_flight, 0, 'Supabase row in_flight must be 0');
  console.log('✓ Supabase row updated: used_count=1, in_flight=0\n');

  // Test 4: Failed request releases reservation without consuming quota
  console.log('Test 4: Failed request releases reservation without consuming quota');
  const res2 = await tryReserveAiCoachQuotaAsync(userAlice, tz, false);
  assert.strictEqual(res2.allowed, true);
  assert.strictEqual(res2.inFlight, 1);

  await releaseAiCoachReservationAsync(userAlice, tz);

  const rowAfterRelease = mockDb.tables.ai_coach_daily_usage.find(r => r.firebase_uid === userAlice && r.local_date === today);
  assert.strictEqual(rowAfterRelease.used_count, 1, 'used_count must remain 1');
  assert.strictEqual(rowAfterRelease.in_flight, 0, 'in_flight must return to 0');

  const usageAfterRelease = await getAiCoachUsageAsync(userAlice, tz, false);
  assert.strictEqual(usageAfterRelease.usedToday, 1);
  assert.strictEqual(usageAfterRelease.remaining, 4);
  console.log('✓ Reservation released cleanly in Supabase, quota preserved\n');

  // Test 5: Reaching Free limit (5 messages) blocks 6th reservation in shared storage
  console.log('Test 5: Reaching Free limit (5 messages) blocks 6th reservation');
  for (let i = 2; i <= 5; i++) {
    const reserve = await tryReserveAiCoachQuotaAsync(userAlice, tz, false);
    assert.strictEqual(reserve.allowed, true, `Reservation ${i} must succeed`);
    await commitAiCoachQuotaAsync(userAlice, tz);
  }

  const limitCheck = await getAiCoachUsageAsync(userAlice, tz, false);
  assert.strictEqual(limitCheck.usedToday, 5);
  assert.strictEqual(limitCheck.remaining, 0);
  assert.strictEqual(limitCheck.isLimitReached, true);

  const blockedReservation = await tryReserveAiCoachQuotaAsync(userAlice, tz, false);
  assert.strictEqual(blockedReservation.allowed, false, '6th message reservation must be blocked');
  assert.strictEqual(blockedReservation.isLimitReached, true);
  console.log('✓ 6th message blocked at limit 5 across shared storage\n');

  // Test 6: Multi-Instance Cloud Run simulation
  console.log('Test 6: Multi-instance Cloud Run simulation');
  const userMulti = `user_bob_cloudrun_multi_${Date.now()}`;
  await resetAiCoachUsageForTestingAsync(userMulti);

  // Instance A receives message 1
  const instA_res1 = await tryReserveAiCoachQuotaAsync(userMulti, tz, false);
  assert.strictEqual(instA_res1.allowed, true);
  await commitAiCoachQuotaAsync(userMulti, tz);

  // Instance B receives message 2 (queries shared storage)
  const instB_usage = await getAiCoachUsageAsync(userMulti, tz, false);
  assert.strictEqual(instB_usage.usedToday, 1, 'Instance B must see Instance A commit');

  // Instance B uses up remaining 4 slots
  for (let i = 2; i <= 5; i++) {
    const r = await tryReserveAiCoachQuotaAsync(userMulti, tz, false);
    assert.strictEqual(r.allowed, true);
    await commitAiCoachQuotaAsync(userMulti, tz);
  }

  // Instance A receives message 6 -> MUST be blocked because Instance B used remaining quota
  const instA_blocked = await tryReserveAiCoachQuotaAsync(userMulti, tz, false);
  assert.strictEqual(instA_blocked.allowed, false, 'Instance A must block request based on Instance B shared usage');
  console.log('✓ Cross-instance quota enforcement verified\n');

  // Test 7: Stale Reservation Recovery (Older than 2 minutes)
  console.log('Test 7: Stale in-flight reservation recovery (> 2 minutes)');
  const userStale = `user_stale_recovery_${Date.now()}`;
  await resetAiCoachUsageForTestingAsync(userStale);

  // Seed a simulated hung instance: user has 1 completed message, and 4 in_flight reservations
  // with last_reservation_at timestamp 3 minutes ago (> 2 minute threshold)
  const threeMinutesAgo = new Date(Date.now() - 3 * 60 * 1000).toISOString();
  mockDb.tables.ai_coach_daily_usage.push({
    firebase_uid: userStale,
    local_date: today,
    used_count: 1,
    in_flight: 4, // 1 + 4 = 5 (would normally block if not stale)
    last_reservation_at: threeMinutesAgo,
    timezone: tz,
    updated_at: threeMinutesAgo
  });

  // Since in_flight reservations are older than 2 minutes, the atomic reservation
  // must discard the stale in_flight count and allow the new reservation!
  const staleRecoveryRes = await tryReserveAiCoachQuotaAsync(userStale, tz, false);
  assert.strictEqual(staleRecoveryRes.allowed, true, 'Stale reservations must be discarded, allowing new reservation');
  assert.strictEqual(staleRecoveryRes.inFlight, 1, 'inFlight must be reset to 1');
  assert.strictEqual(staleRecoveryRes.usedToday, 1, 'usedToday must remain 1');
  console.log('✓ Stale reservation (> 2 min) recovered cleanly, in_flight reset\n');

  // Test 8: Timezone Persistence in user_profiles
  console.log('Test 8: Timezone persistence in user_profiles table');
  const userTz = `user_tz_persist_${Date.now()}`;
  mockDb.tables.user_profiles.push({
    firebase_uid: userTz,
    email: 'tzuser@test.local',
    goal: 'Maintain',
    timezone: 'UTC'
  });

  // Client requests with validated IANA timezone
  const resolvedTz1 = await resolveUserTimezoneAsync(userTz, 'America/Chicago');
  assert.strictEqual(resolvedTz1, 'America/Chicago', 'Must resolve to validated IANA timezone');

  // Verify it was persisted to user_profiles table in Supabase
  const profileRow = mockDb.tables.user_profiles.find(p => p.firebase_uid === userTz);
  assert.strictEqual(profileRow.timezone, 'America/Chicago', 'Timezone must be persisted to user_profiles');

  // Second call without header reads directly from database
  const resolvedTz2 = await resolveUserTimezoneAsync(userTz, null);
  assert.strictEqual(resolvedTz2, 'America/Chicago', 'Must load persisted timezone from database');
  console.log('✓ Timezone successfully persisted to and read from user_profiles\n');

  // Test 9: Fallback to local memory when Supabase client is disconnected in test mode
  console.log('Test 9: Graceful fallback when Supabase is disconnected in non-production mode');
  setSupabaseClient(null);

  const userFallback = `user_fallback_local_${Date.now()}`;
  resetAiCoachUsageForTesting(userFallback);
  const localRes = await tryReserveAiCoachQuotaAsync(userFallback, tz, false);
  assert.strictEqual(localRes.allowed, true, 'Local fallback must succeed when Supabase is null in test mode');
  await commitAiCoachQuotaAsync(userFallback, tz);

  const localUsage = await getAiCoachUsageAsync(userFallback, tz, false);
  assert.strictEqual(localUsage.usedToday, 1, 'Local fallback usage must be 1');
  console.log('✓ Graceful fallback to local in-memory/disk verified in test mode\n');

  // Restore mockDb for cleanup
  setSupabaseClient(mockDb);

  // Test 10: Account deletion purge of shared quota table
  console.log('Test 10: Account deletion purges shared quota table');
  await resetAiCoachUsageForTestingAsync(userAlice);
  const remainingAliceRows = mockDb.tables.ai_coach_daily_usage.filter(r => r.firebase_uid === userAlice);
  assert.strictEqual(remainingAliceRows.length, 0, 'Alice rows must be 0 after reset/purge');
  console.log('✓ Shared quota table records successfully purged on account reset\n');

  // Test 11: Timezone Locking & 7-Day Cooldown
  console.log('Test 11: Timezone locking enforces 7-day cooldown on changes');
  const userLock = `user_tz_lock_${Date.now()}`;
  mockDb.tables.user_profiles.push({
    firebase_uid: userLock,
    email: 'tzlock@test.local',
    goal: 'Maintain',
    timezone: 'UTC',
    timezone_changed_at: null
  });

  // First change: user sets Asia/Tokyo
  const initialLockTz = await resolveUserTimezoneAsync(userLock, 'Asia/Tokyo');
  assert.strictEqual(initialLockTz, 'Asia/Tokyo', 'Initial timezone must be set to Asia/Tokyo');
  const lockProfile = mockDb.tables.user_profiles.find(p => p.firebase_uid === userLock);
  assert.strictEqual(lockProfile.timezone, 'Asia/Tokyo');
  assert.ok(lockProfile.timezone_changed_at, 'timezone_changed_at must be recorded on first set');

  // Attempt change after 2 days (within 7-day cooldown): MUST BE REJECTED
  lockProfile.timezone_changed_at = new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString();
  const rejectedTzChange = await resolveUserTimezoneAsync(userLock, 'Europe/Paris');
  assert.strictEqual(rejectedTzChange, 'Asia/Tokyo', 'Timezone change within 7 days must be rejected and kept as Asia/Tokyo');
  assert.strictEqual(lockProfile.timezone, 'Asia/Tokyo', 'Database profile timezone must remain locked');

  // Attempt change after 8 days (> 7-day cooldown): MUST BE ALLOWED
  lockProfile.timezone_changed_at = new Date(Date.now() - 8 * 24 * 60 * 60 * 1000).toISOString();
  const allowedTzChange = await resolveUserTimezoneAsync(userLock, 'Europe/Paris');
  assert.strictEqual(allowedTzChange, 'Europe/Paris', 'Timezone change after 7 days must be permitted');
  assert.strictEqual(lockProfile.timezone, 'Europe/Paris', 'Database profile timezone must be updated to Europe/Paris');
  console.log('✓ Timezone 7-day locking and cooldown logic verified\n');

  // Test 12: Rolling 24-Hour Reservation Cap Backstop Across Date Boundaries (2x Daily Limit)
  console.log('Test 12: Rolling 24-hour reservation cap backstop across date boundaries (2x limit)');
  const userRolling = `user_rolling_24h_${Date.now()}`;
  await resetAiCoachUsageForTestingAsync(userRolling);

  // Step 1: User consumes 5 messages late at night on Day 1 (e.g. 23:30, 2 hours ago)
  const twoHoursAgo = new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString();
  mockDb.tables.ai_coach_daily_usage.push({
    firebase_uid: userRolling,
    local_date: '2026-11-05',
    used_count: 5,
    in_flight: 0,
    timezone: 'UTC',
    updated_at: twoHoursAgo
  });

  // Step 2: After local midnight on Day 2 (00:30), user makes 5 more messages on new calendar day
  // Since rolling cap is 2x daily limit (2 * 5 = 10), legitimate midnight usage MUST SUCCEED!
  for (let i = 1; i <= 5; i++) {
    const midnightRes = await tryReserveAiCoachQuotaAsync(userRolling, 'UTC', false);
    assert.strictEqual(midnightRes.allowed, true, `Midnight request ${i} on new calendar day must succeed`);
    await commitAiCoachQuotaAsync(userRolling, 'UTC');
  }

  // Verify Day 2 row has used_count: 5
  const day2Row = mockDb.tables.ai_coach_daily_usage.find(r => r.firebase_uid === userRolling && r.local_date === today);
  assert.strictEqual(day2Row.used_count, 5, 'Day 2 used_count must be 5');
  console.log('✓ 5 messages late at night + 5 messages after local midnight succeed cleanly for Free user');

  // Step 3: Malicious timezone change / extra request attempting 11th message in 24h window
  // Rolling 24h usage is now 5 + 5 = 10, which hits the 2x backstop (10)
  // Even if user attempts an artificial 3rd day via timezone change, rolling 24h cap MUST BLOCK it!
  const abuseAttempt = await tryReserveAiCoachQuotaAsync(userRolling, 'UTC', false);
  assert.strictEqual(abuseAttempt.allowed, false, '11th message within 24 hours must be blocked by rolling backstop');
  assert.strictEqual(abuseAttempt.rolling24hCapReached, true, 'rolling24hCapReached must be true');
  console.log('✓ Timezone abuse / >10 requests in 24h successfully blocked by rolling 24h backstop\n');

  // Test 13: Isolated 2x Rolling Backstop with Timezone Jump to Fresh Local Day
  console.log('Test 13: Isolated 2x rolling backstop with timezone jump to fresh local day');
  const userTzJump = `user_tz_jump_${Date.now()}`;
  await resetAiCoachUsageForTestingAsync(userTzJump);

  // User consumes 10 messages spread across two dates within the last 24 hours:
  // Date A (18 hours ago): 5 messages
  const eighteenHoursAgo = new Date(Date.now() - 18 * 60 * 60 * 1000).toISOString();
  mockDb.tables.ai_coach_daily_usage.push({
    firebase_uid: userTzJump,
    local_date: '2026-11-05',
    used_count: 5,
    in_flight: 0,
    timezone: 'America/New_York',
    updated_at: eighteenHoursAgo
  });

  // Date B (6 hours ago): 5 messages
  const sixHoursAgo = new Date(Date.now() - 6 * 60 * 60 * 1000).toISOString();
  mockDb.tables.ai_coach_daily_usage.push({
    firebase_uid: userTzJump,
    local_date: '2026-11-06',
    used_count: 5,
    in_flight: 0,
    timezone: 'America/New_York',
    updated_at: sixHoursAgo
  });

  // Total consumed in last 24h: 5 + 5 = 10 messages (= 2x daily limit of 5).
  // Now user changes timezone to Pacific/Auckland, where local date is Date C ('2026-11-07').
  // On Date C, usedToday in database is 0! The daily limit check (0 < 5) would ordinarily allow it.
  // But rolling 24h usage is 10 >= limit * 2 (10).
  // The 2x rolling backstop MUST strictly block the reservation!
  const jumpRes = await tryReserveAiCoachQuotaAsync(userTzJump, 'Pacific/Auckland', false);
  assert.strictEqual(jumpRes.allowed, false, 'Reservation on fresh day must be blocked by rolling backstop');
  assert.strictEqual(jumpRes.rolling24hCapReached, true, 'rolling24hCapReached must be true');
  assert.strictEqual(jumpRes.usedToday, 10, 'usedToday in payload must reflect 10 rolling messages');
  console.log('✓ Isolated 2x rolling backstop verified: fresh day blocked by rolling cap, not daily limit\n');

  console.log('=== All 13 AI Coach Shared Storage, Atomicity, Timezone Locking & Rolling 24h Tests PASSED! ===');
}

runSharedQuotaTests().catch((err) => {
  console.error('Shared quota tests failed:', err);
  process.exit(1);
});
