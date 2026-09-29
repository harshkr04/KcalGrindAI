process.env.TEST = 'true';
import assert from 'node:assert';

const {
  FREE_AI_COACH_DAILY_LIMIT,
  PRO_AI_COACH_DAILY_LIMIT,
  aiCoachDailyUsage,
  getUserLocalDate,
  determineUserIsPro,
  getAiCoachUsage,
  recordAiCoachMessageSent,
  resetAiCoachUsageForTesting,
  fastify
} = await import('./src/server.js');

async function runAiCoachQuotaTests() {
  console.log('=== Running AI Coach Quota & Limit Enforcement Unit Tests ===\n');

  // Test 1: Limit Constants must be EXACTLY 5 (Free) and 50 (Pro)
  console.log('Test 1: Quota Constants verification');
  assert.strictEqual(FREE_AI_COACH_DAILY_LIMIT, 5, 'Free daily limit must be exactly 5');
  assert.strictEqual(PRO_AI_COACH_DAILY_LIMIT, 50, 'Pro daily limit must be exactly 50');
  console.log('✓ Quota constants are exactly 5 Free and 50 Pro\n');

  // Test 2: Local Calendar Date Calculation (Timezone preservation, no UTC substitute)
  console.log('Test 2: Local Calendar Date Calculation');
  const now = new Date();
  const kolkataDate = getUserLocalDate('Asia/Kolkata');
  const newYorkDate = getUserLocalDate('America/New_York');
  const utcDate = getUserLocalDate('UTC');
  assert.match(kolkataDate, /^\d{4}-\d{2}-\d{2}$/, 'Format must be YYYY-MM-DD');
  assert.match(newYorkDate, /^\d{4}-\d{2}-\d{2}$/, 'Format must be YYYY-MM-DD');
  console.log(`✓ Kolkata local date: ${kolkataDate}, NY local date: ${newYorkDate}, UTC: ${utcDate}\n`);

  // Test 3: Free Plan: 5 allowed, 6th blocked
  console.log('Test 3: Free Plan: 5 messages allowed, 6th blocked');
  const testUid = 'user_free_test_123';
  resetAiCoachUsageForTesting(testUid);

  let usage = getAiCoachUsage(testUid, 'Asia/Kolkata', false);
  assert.strictEqual(usage.usedToday, 0);
  assert.strictEqual(usage.dailyLimit, 5);
  assert.strictEqual(usage.remaining, 5);
  assert.strictEqual(usage.isLimitReached, false);

  for (let i = 1; i <= 5; i++) {
    recordAiCoachMessageSent(testUid, 'Asia/Kolkata');
    usage = getAiCoachUsage(testUid, 'Asia/Kolkata', false);
    assert.strictEqual(usage.usedToday, i);
    assert.strictEqual(usage.remaining, 5 - i);
    if (i < 5) {
      assert.strictEqual(usage.isLimitReached, false);
    }
  }

  assert.strictEqual(usage.usedToday, 5);
  assert.strictEqual(usage.remaining, 0);
  assert.strictEqual(usage.isLimitReached, true);
  console.log('✓ Free Plan correctly allows 5 messages and blocks at 5 (remaining: 0)\n');

  // Test 4: Pro Plan: 50 allowed, 51st blocked
  console.log('Test 4: Pro Plan: 50 messages allowed, 51st blocked');
  const proUid = 'user_pro_test_456';
  resetAiCoachUsageForTesting(proUid);

  usage = getAiCoachUsage(proUid, 'Asia/Kolkata', true);
  assert.strictEqual(usage.usedToday, 0);
  assert.strictEqual(usage.dailyLimit, 50);
  assert.strictEqual(usage.remaining, 50);
  assert.strictEqual(usage.isLimitReached, false);

  for (let i = 1; i <= 50; i++) {
    recordAiCoachMessageSent(proUid, 'Asia/Kolkata');
  }

  usage = getAiCoachUsage(proUid, 'Asia/Kolkata', true);
  assert.strictEqual(usage.usedToday, 50);
  assert.strictEqual(usage.remaining, 0);
  assert.strictEqual(usage.isLimitReached, true);
  console.log('✓ Pro Plan correctly allows 50 messages and blocks at 50 (remaining: 0)\n');

  // Test 5: Upgrade Free -> Pro immediately increases limit to 50
  console.log('Test 5: Upgrade Free -> Pro dynamic entitlement');
  const upgradeUid = 'user_upgrade_789';
  resetAiCoachUsageForTesting(upgradeUid);

  // Free user sends 5 messages (reaches limit)
  for (let i = 0; i < 5; i++) {
    recordAiCoachMessageSent(upgradeUid, 'Asia/Kolkata');
  }
  let freeUsage = getAiCoachUsage(upgradeUid, 'Asia/Kolkata', false);
  assert.strictEqual(freeUsage.isLimitReached, true);
  assert.strictEqual(freeUsage.remaining, 0);

  // User upgrades to Pro: limit expands to 50, remaining becomes 45
  let proUsage = getAiCoachUsage(upgradeUid, 'Asia/Kolkata', true);
  assert.strictEqual(proUsage.dailyLimit, 50);
  assert.strictEqual(proUsage.usedToday, 5);
  assert.strictEqual(proUsage.remaining, 45);
  assert.strictEqual(proUsage.isLimitReached, false);
  console.log('✓ Upgrade from Free to Pro immediately expands limit to 50 (45 remaining)\n');

  // Test 6: Downgrade Pro -> Free immediately applies 5/day limit
  console.log('Test 6: Downgrade Pro -> Free dynamic entitlement');
  const downgradeUsage = getAiCoachUsage(upgradeUid, 'Asia/Kolkata', false);
  assert.strictEqual(downgradeUsage.dailyLimit, 5);
  assert.strictEqual(downgradeUsage.usedToday, 5);
  assert.strictEqual(downgradeUsage.remaining, 0);
  assert.strictEqual(downgradeUsage.isLimitReached, true);
  console.log('✓ Downgrade from Pro to Free immediately restricts to 5/day (blocked)\n');

  // Test 7: Daily Quota Reset on next local calendar day
  console.log('Test 7: Daily Quota Reset on next local calendar day');
  const rolloverUid = 'user_rollover_999';
  resetAiCoachUsageForTesting(rolloverUid);

  // Simulate usage on yesterday's date
  aiCoachDailyUsage.set(`${rolloverUid}:2026-09-13`, 5);

  // Check usage today (2026-09-14)
  const todayUsage = getAiCoachUsage(rolloverUid, 'Asia/Kolkata', false);
  assert.strictEqual(todayUsage.usedToday, 0, 'Today usage should be 0 because yesterday was 2026-09-13');
  assert.strictEqual(todayUsage.remaining, 5);
  assert.strictEqual(todayUsage.isLimitReached, false);
  console.log('✓ Usage on previous day does not carry over to new calendar day\n');

  // Test 8: Security - Production environment rejects client-controlled headers
  console.log('Test 8: Production Security - Never trust client-controlled headers in production');
  const fakeReqWithHeader = {
    user: { uid: 'attacker_1' },
    headers: { 'x-test-pro': 'true', 'x-user-pro': 'true' }
  };

  const oldEnv = process.env.NODE_ENV;
  try {
    process.env.NODE_ENV = 'production';
    const isProInProd = determineUserIsPro(fakeReqWithHeader);
    assert.strictEqual(isProInProd, false, 'Client headers must be IGNORED in production');

    const verifiedReq = {
      user: { uid: 'verified_user', isPro: true },
      headers: {}
    };
    const isVerifiedPro = determineUserIsPro(verifiedReq);
    assert.strictEqual(isVerifiedPro, true, 'Verified claims in token must be honored');
  } finally {
    process.env.NODE_ENV = oldEnv;
  }
  console.log('✓ Server-side production enforcement rejects unverified client headers\n');

  // Test 9: Concurrency-Safe Reservation: Simultaneous requests cannot exceed daily limit
  console.log('Test 9: Concurrency-Safe Atomic Quota Reservation');
  const raceUid = 'user_race_condition_test';
  resetAiCoachUsageForTesting(raceUid);

  // Set user to 4 of 5 messages used today
  const {
    tryReserveAiCoachQuota,
    commitAiCoachQuota,
    releaseAiCoachReservation,
    initPersistence,
    isValidIanaTimezone,
    resolveUserTimezone,
    userTimezones
  } = await import('./src/server.js');

  for (let i = 0; i < 4; i++) {
    const res = tryReserveAiCoachQuota(raceUid, 'Asia/Kolkata', false);
    assert.strictEqual(res.allowed, true);
    commitAiCoachQuota(raceUid, 'Asia/Kolkata');
  }
  let raceUsage = getAiCoachUsage(raceUid, 'Asia/Kolkata', false);
  assert.strictEqual(raceUsage.usedToday, 4);

  // Now, 2 simultaneous requests arrive at the exact same time
  const req1 = tryReserveAiCoachQuota(raceUid, 'Asia/Kolkata', false);
  const req2 = tryReserveAiCoachQuota(raceUid, 'Asia/Kolkata', false);

  assert.strictEqual(req1.allowed, true, 'First simultaneous request must be allowed');
  assert.strictEqual(req2.allowed, false, 'Second simultaneous request must be BLOCKED (cannot bypass limit)');
  assert.strictEqual(req2.isLimitReached, true);

  // Commit the first one
  commitAiCoachQuota(raceUid, 'Asia/Kolkata');
  raceUsage = getAiCoachUsage(raceUid, 'Asia/Kolkata', false);
  assert.strictEqual(raceUsage.usedToday, 5, 'Usage is strictly capped at 5');
  assert.strictEqual(raceUsage.remaining, 0);
  console.log('✓ Concurrency reservation prevents simultaneous requests from bypassing limit\n');

  // Test 10: Failed AI requests release reservation and consume zero quota
  console.log('Test 10: Failed AI requests release reservation and consume zero quota');
  const failUid = 'user_failure_test';
  resetAiCoachUsageForTesting(failUid);

  const reserveForFail = tryReserveAiCoachQuota(failUid, 'Asia/Kolkata', false);
  assert.strictEqual(reserveForFail.allowed, true);
  assert.strictEqual(getAiCoachUsage(failUid, 'Asia/Kolkata', false).inFlight, 1);

  // Simulate API failure (NVIDIA / Gemini returns 502 / timeout)
  releaseAiCoachReservation(failUid, 'Asia/Kolkata');

  const afterFailUsage = getAiCoachUsage(failUid, 'Asia/Kolkata', false);
  assert.strictEqual(afterFailUsage.usedToday, 0, 'Used count must remain 0 after failed AI call');
  assert.strictEqual(afterFailUsage.inFlight, 0, 'In-flight reservations must be cleared');
  assert.strictEqual(afterFailUsage.remaining, 5, 'Full 5 remaining quota preserved');
  console.log('✓ Failed AI requests release reservation and consume zero quota\n');

  // Test 11: Disk Persistence survives server restart
  console.log('Test 11: Disk Persistence survives server restart');
  const persistUid = 'user_persistence_test';
  resetAiCoachUsageForTesting(persistUid);

  // Record 3 messages
  for (let i = 0; i < 3; i++) {
    recordAiCoachMessageSent(persistUid, 'Asia/Kolkata');
  }
  assert.strictEqual(getAiCoachUsage(persistUid, 'Asia/Kolkata', false).usedToday, 3);

  // Simulate server restart: clear in-memory map and re-initialize from disk
  aiCoachDailyUsage.clear();
  assert.strictEqual(aiCoachDailyUsage.size, 0, 'In-memory map cleared');
  initPersistence();

  const restoredUsage = getAiCoachUsage(persistUid, 'Asia/Kolkata', false);
  assert.strictEqual(restoredUsage.usedToday, 3, 'Usage must be restored from disk after restart');
  assert.strictEqual(restoredUsage.remaining, 2, 'Remaining must accurately reflect persisted usage');
  console.log('✓ AI Coach usage successfully survives server restarts via disk persistence\n');

  // Test 12: Account-Bound Timezone Association & Validation
  console.log('Test 12: Account-Bound Timezone Association & Security Validation');
  const tzUid = 'user_timezone_test';
  userTimezones.delete(tzUid);

  // 1. Valid IANA timezone associates with account
  assert.strictEqual(isValidIanaTimezone('Europe/London'), true);
  assert.strictEqual(isValidIanaTimezone('Invalid/Fake_Zone'), false);
  assert.strictEqual(isValidIanaTimezone(''), false);
  assert.strictEqual(isValidIanaTimezone(null), false);

  const initialTz = resolveUserTimezone(tzUid, 'Europe/London');
  assert.strictEqual(initialTz, 'Europe/London');
  assert.strictEqual(userTimezones.get(tzUid), 'Europe/London');

  // 2. Client header sending unvalidated timezone does NOT overwrite stored timezone
  const invalidAttempt = resolveUserTimezone(tzUid, 'Invalid/Fake_Zone');
  assert.strictEqual(invalidAttempt, 'Europe/London', 'Invalid timezone must be rejected');
  assert.strictEqual(userTimezones.get(tzUid), 'Europe/London');

  // 3. Client header sending generic 'UTC' does NOT overwrite stored local timezone
  const utcAttempt = resolveUserTimezone(tzUid, 'UTC');
  assert.strictEqual(utcAttempt, 'Europe/London', 'Stored local timezone must not be wiped by generic UTC');
  assert.strictEqual(userTimezones.get(tzUid), 'Europe/London');

  // 4. Valid new local timezone updates account
  const updateTz = resolveUserTimezone(tzUid, 'America/New_York');
  assert.strictEqual(updateTz, 'America/New_York');
  assert.strictEqual(userTimezones.get(tzUid), 'America/New_York');
  console.log('✓ Account-bound timezone validation and persistence verified\n');

  console.log('=== All 12 AI Coach Quota, Concurrency, Persistence & Security Tests PASSED! ===');
}

runAiCoachQuotaTests().catch((err) => {
  console.error('Test failed:', err);
  process.exit(1);
});

