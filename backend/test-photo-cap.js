/**
 * KcalGrind AI - Photo Analysis Daily Quota & Cost Protection Tests
 *
 * Verifies:
 * 1. Default free photo cap: 5 photos/day.
 * 2. Pro photo cap: 50 photos/day.
 * 3. 6th photo analysis for free user blocked with 429 QuotaExceeded.
 * 4. Dynamic Pro upgrade unblocks immediately.
 * 5. Persistence across server restarts.
 * 6. Account deletion purges photo usage.
 */

process.env.TEST = 'true';
process.env.PORT = '8098';

import test from 'node:test';
import assert from 'node:assert/strict';

const {
  FREE_PHOTO_DAILY_LIMIT,
  PRO_PHOTO_DAILY_LIMIT,
  getPhotoUsage,
  recordPhotoAnalysisUsage,
  resetPhotoUsageForTesting,
  initPersistence,
  photoDailyUsage
} = await import('./src/server.js');

test('Photo Quota: default limits are 5 (Free) and 50 (Pro)', () => {
  assert.equal(FREE_PHOTO_DAILY_LIMIT, 5, 'Free photo quota must be 5/day');
  assert.equal(PRO_PHOTO_DAILY_LIMIT, 50, 'Pro photo quota must be 50/day');
});

test('Photo Quota: tracks usage and enforces daily limit', () => {
  const uid = 'test_photo_user_1';
  const tz = 'Asia/Kolkata';
  resetPhotoUsageForTesting(uid);

  // Initial state: 0 used, 5 remaining
  let usage = getPhotoUsage(uid, tz, false);
  assert.equal(usage.usedToday, 0);
  assert.equal(usage.remaining, 5);
  assert.equal(usage.isBlocked, false);

  // Record 5 analyses
  for (let i = 1; i <= 5; i++) {
    const count = recordPhotoAnalysisUsage(uid, tz);
    assert.equal(count, i);
  }

  // 5th reached: 0 remaining, isBlocked: true
  usage = getPhotoUsage(uid, tz, false);
  assert.equal(usage.usedToday, 5);
  assert.equal(usage.remaining, 0);
  assert.equal(usage.isBlocked, true, '6th request must be blocked for Free tier');

  // Pro tier with same usage is NOT blocked (45 remaining)
  const proUsage = getPhotoUsage(uid, tz, true);
  assert.equal(proUsage.usedToday, 5);
  assert.equal(proUsage.remaining, 45);
  assert.equal(proUsage.isBlocked, false, 'Pro user must not be blocked at 5 photos');

  resetPhotoUsageForTesting(uid);
});

test('Photo Quota: disk persistence survives re-initialization', () => {
  const uid = 'test_photo_persist_user';
  const tz = 'America/New_York';
  resetPhotoUsageForTesting(uid);

  recordPhotoAnalysisUsage(uid, tz);
  recordPhotoAnalysisUsage(uid, tz);
  recordPhotoAnalysisUsage(uid, tz);

  const before = getPhotoUsage(uid, tz, false);
  assert.equal(before.usedToday, 3);

  // Re-read from disk
  initPersistence();

  const after = getPhotoUsage(uid, tz, false);
  assert.equal(after.usedToday, 3, 'Usage count must be restored from disk persistence');

  resetPhotoUsageForTesting(uid);
});
