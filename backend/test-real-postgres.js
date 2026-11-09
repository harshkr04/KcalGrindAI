import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import assert from 'node:assert';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Minimal unified Postgres adapter supporting both native TCP pg.Pool and in-process PGlite WASM
class PostgresTestClient {
  constructor() {
    this.pgPool = null;
    this.pglite = null;
    this.isNative = false;
  }

  async init() {
    if (process.env.DATABASE_URL) {
      console.log(`Connecting to native PostgreSQL via DATABASE_URL (${process.env.DATABASE_URL.replace(/:[^:@]+@/, ':****@')})...`);
      const { default: pg } = await import('pg');
      this.pgPool = new pg.Pool({
        connectionString: process.env.DATABASE_URL,
        max: 25 // support 20 concurrent connections
      });
      const res = await this.pgPool.query('SELECT version();');
      console.log('✓ Connected to PostgreSQL:', res.rows[0].version.split(',')[0]);
      this.isNative = true;
    } else {
      console.log('DATABASE_URL not set — Initializing real PostgreSQL 17 in-process via PGlite WebAssembly...');
      const { PGlite } = await import('@electric-sql/pglite');
      this.pglite = new PGlite();
      const res = await this.pglite.query('SELECT version();');
      console.log('✓ In-process PostgreSQL 17 WASM engine ready:', res.rows[0].version.split(',')[0]);
      this.isNative = false;
    }
  }

  async query(sql, params = []) {
    if (this.isNative) {
      return this.pgPool.query(sql, params);
    }
    return this.pglite.query(sql, params);
  }

  async exec(sql) {
    if (this.isNative) {
      return this.pgPool.query(sql);
    }
    return this.pglite.exec(sql);
  }

  async close() {
    if (this.pgPool) {
      await this.pgPool.end();
    }
    if (this.pglite) {
      await this.pglite.close();
    }
  }
}

async function runRealPostgresIntegrationSuite() {
  console.log('================================================================');
  console.log('   KcalGrind Real PostgreSQL Schema, Security & Atomic RPC Test');
  console.log('================================================================\n');

  const db = new PostgresTestClient();
  await db.init();

  try {
    // ----------------------------------------------------------------
    // Setup: Create minimal Supabase role stubs (anon, authenticated, service_role)
    // ----------------------------------------------------------------
    console.log('\n--- Setting up Supabase Role Stubs ---');
    await db.exec(`
      DO $$
      BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'anon') THEN
          CREATE ROLE anon NOLOGIN;
        END IF;
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'authenticated') THEN
          CREATE ROLE authenticated NOLOGIN;
        END IF;
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'service_role') THEN
          CREATE ROLE service_role NOLOGIN;
        END IF;
      END $$;
      GRANT USAGE ON SCHEMA public TO anon, authenticated, service_role;
    `);
    console.log('✓ Roles anon, authenticated, and service_role created and granted schema usage\n');

    // ----------------------------------------------------------------
    // Requirement 3 (a): Migration Idempotency (Apply All Migrations TWICE)
    // ----------------------------------------------------------------
    console.log('--- Test (a): Migration Application Idempotency (Running Twice) ---');
    const migrationsDir = path.resolve(__dirname, '..', 'supabase', 'migrations');
    const migrationFiles = [
      '001_initial_schema.sql',
      '002_ai_cleanup_cron.sql',
      '003_ai_coach_daily_usage.sql',
      '004_user_custom_data_and_subscriptions.sql',
      '005_ai_photo_daily_usage.sql'
    ];

    for (let pass = 1; pass <= 2; pass++) {
      console.log(`Running Migration Pass ${pass}/2...`);
      for (const file of migrationFiles) {
        const filePath = path.join(migrationsDir, file);
        const sql = fs.readFileSync(filePath, 'utf8');
        if (sql.trim()) {
          await db.exec(sql);
          console.log(`  ✓ Pass ${pass}: Applied ${file}`);
        }
      }
    }
    console.log('✓ Test (a) PASSED: Migrations are completely idempotent (zero errors on re-run)\n');

    // ----------------------------------------------------------------
    // Requirement 3 (b): Concurrency Test with 20 Parallel Callers & Limit 5
    // ----------------------------------------------------------------
    console.log('--- Test (b): Concurrency Test (20 Parallel Callers, Limit 5) ---');
    const testUid = `user_concurrent_pg_${Date.now()}`;
    const testDate = '2026-11-06';
    const limit = 5;

    let results;
    if (db.isNative) {
      console.log('Acquiring 20 distinct client connections from pg.Pool for true parallel execution...');
      const clients = await Promise.all(
        Array.from({ length: 20 }, () => db.pgPool.connect())
      );
      try {
        console.log(`Firing 20 concurrent invocations across 20 separate connections for ${testUid}...`);
        const promises = clients.map(client =>
          client.query(
            `SELECT public.reserve_ai_coach_quota($1, $2, $3, 'UTC') AS result;`,
            [testUid, testDate, limit]
          )
        );
        results = await Promise.all(promises);
      } finally {
        clients.forEach(client => client.release());
      }
    } else {
      console.log(`Firing 20 concurrent invocations for ${testUid}...`);
      const promises = [];
      for (let i = 0; i < 20; i++) {
        promises.push(
          db.query(
            `SELECT public.reserve_ai_coach_quota($1, $2, $3, 'UTC') AS result;`,
            [testUid, testDate, limit]
          )
        );
      }
      results = await Promise.all(promises);
    }

    let allowedCount = 0;
    let blockedCount = 0;

    for (const r of results) {
      const resData = r.rows[0].result;
      if (resData.allowed === true) {
        allowedCount++;
      } else if (resData.allowed === false) {
        blockedCount++;
      }
    }

    console.log(`Results: ${allowedCount} ALLOWED (expected 5), ${blockedCount} BLOCKED (expected 15)`);
    assert.strictEqual(allowedCount, 5, 'Exactly 5 concurrent requests must be allowed');
    assert.strictEqual(blockedCount, 15, 'Exactly 15 concurrent requests must be blocked');

    // Verify row state in PostgreSQL table
    const rowCheck = await db.query(
      `SELECT in_flight, used_count FROM public.ai_coach_daily_usage WHERE firebase_uid = $1 AND local_date = $2;`,
      [testUid, testDate]
    );
    assert.strictEqual(rowCheck.rows.length, 1, 'Usage row must exist');
    assert.strictEqual(rowCheck.rows[0].in_flight, 5, 'in_flight count in database must be exactly 5');
    assert.strictEqual(rowCheck.rows[0].used_count, 0, 'used_count must be 0');
    console.log('✓ Test (b) PASSED: Row locking (SELECT ... FOR UPDATE) strictly prevented over-allocation\n');

    // ----------------------------------------------------------------
    // Requirement 3 (c): Anon Role Cannot EXECUTE the RPC Function
    // ----------------------------------------------------------------
    console.log('--- Test (c): Security Audit — Anon Role Execution Rejection ---');
    let anonRejected = false;
    try {
      await db.exec('SET ROLE anon;');
      await db.query(`SELECT public.reserve_ai_coach_quota('malicious_actor', '2026-11-06', 5, 'UTC');`);
    } catch (err) {
      anonRejected = true;
      console.log(`Expected rejection caught: [${err.code || 'ERR'}] ${err.message}`);
      assert.match(err.message, /permission denied/i, 'Error message must specify permission denied');
    } finally {
      await db.exec('RESET ROLE;');
    }
    assert.strictEqual(anonRejected, true, 'Calling reserve_ai_coach_quota as anon role MUST fail');
    console.log('✓ Test (c) PASSED: Function execution strictly revoked from anon role\n');

    // ----------------------------------------------------------------
    // Requirement 3 (d): Stale Reservation Recovery (> 2 Minutes)
    // ----------------------------------------------------------------
    console.log('--- Test (d): Stale Reservation Recovery (> 2 Minutes) ---');
    const staleUid = `user_stale_pg_${Date.now()}`;
    
    // Seed row with 1 completed message, and 4 in-flight reservations created 3 minutes ago
    await db.query(`
      INSERT INTO public.ai_coach_daily_usage (
        firebase_uid, local_date, used_count, in_flight, last_reservation_at, timezone, updated_at
      ) VALUES (
        $1, $2, 1, 4, clock_timestamp() - INTERVAL '3 minutes', 'UTC', clock_timestamp() - INTERVAL '3 minutes'
      );
    `, [staleUid, testDate]);

    // Calling reserve_ai_coach_quota must detect that in_flight is older than 2 minutes,
    // reset effective in_flight to 0, and allow reservation (1 used + 1 new in_flight = 2 < 5)
    const staleResQuery = await db.query(
      `SELECT public.reserve_ai_coach_quota($1, $2, 5, 'UTC') AS result;`,
      [staleUid, testDate]
    );
    const staleRes = staleResQuery.rows[0].result;
    assert.strictEqual(staleRes.allowed, true, 'Reservation must succeed after recovering stale in-flight reservations');
    assert.strictEqual(staleRes.inFlight, 1, 'in_flight count must be reset to 1');
    assert.strictEqual(staleRes.usedToday, 1, 'usedToday must remain 1');
    console.log('✓ Test (d) PASSED: Stale in-flight reservations automatically recovered\n');

    // ----------------------------------------------------------------
    // Requirement 3 (e): in_flight Never Goes Below Zero
    // ----------------------------------------------------------------
    console.log('--- Test (e): in_flight Floor Protection (Never < 0) ---');
    const floorUid = `user_floor_pg_${Date.now()}`;
    await db.query(`
      INSERT INTO public.ai_coach_daily_usage (
        firebase_uid, local_date, used_count, in_flight, timezone, updated_at
      ) VALUES (
        $1, $2, 0, 0, 'UTC', clock_timestamp()
      );
    `, [floorUid, testDate]);

    // Call release_ai_coach_reservation multiple times when in_flight is already 0
    for (let i = 0; i < 5; i++) {
      await db.query(`SELECT public.release_ai_coach_reservation($1, $2);`, [floorUid, testDate]);
    }

    const floorCheck = await db.query(
      `SELECT in_flight FROM public.ai_coach_daily_usage WHERE firebase_uid = $1 AND local_date = $2;`,
      [floorUid, testDate]
    );
    assert.strictEqual(floorCheck.rows[0].in_flight, 0, 'in_flight must remain 0 and never drop below zero');
    console.log('✓ Test (e) PASSED: in_flight floor protection enforced via GREATEST(0, in_flight - 1)\n');

    // ----------------------------------------------------------------
    // Requirement 2: Rolling 24-Hour Cap (2x Daily Limit Backstop)
    // ----------------------------------------------------------------
    console.log('--- Test (f): Rolling 24h Cap (2x Daily Limit Across Midnight) ---');
    const midnightUid = `user_midnight_pg_${Date.now()}`;
    const day1Date = '2026-11-05';
    const day2Date = '2026-11-06';

    // User consumes 5 messages on Day 1 late at night (2 hours ago)
    await db.query(`
      INSERT INTO public.ai_coach_daily_usage (
        firebase_uid, local_date, used_count, in_flight, timezone, updated_at
      ) VALUES (
        $1, $2, 5, 0, 'UTC', clock_timestamp() - INTERVAL '2 hours'
      );
    `, [midnightUid, day1Date]);

    // After midnight on Day 2, user makes 5 more messages on new calendar day:
    // With 2x limit (10 in 24h), all 5 Day 2 messages MUST SUCCEED
    for (let i = 1; i <= 5; i++) {
      const day2ResQuery = await db.query(
        `SELECT public.reserve_ai_coach_quota($1, $2, 5, 'UTC') AS result;`,
        [midnightUid, day2Date]
      );
      assert.strictEqual(day2ResQuery.rows[0].result.allowed, true, `Day 2 message ${i} must succeed`);
      await db.query(`SELECT public.commit_ai_coach_quota($1, $2, 'UTC');`, [midnightUid, day2Date]);
    }

    // Now Day 1 (5) + Day 2 (5) = 10 used in the last 24 hours.
    // An 11th request within 24h (e.g. attempting timezone hopping) MUST BE BLOCKED by rolling 24h backstop!
    const capResQuery = await db.query(
      `SELECT public.reserve_ai_coach_quota($1, $2, 5, 'UTC') AS result;`,
      [midnightUid, day2Date]
    );
    const capRes = capResQuery.rows[0].result;
    assert.strictEqual(capRes.allowed, false, '11th message in 24h must be blocked by rolling backstop');
    assert.strictEqual(capRes.rolling24hCapReached, true, 'rolling24hCapReached must be true');
    console.log('✓ Test (f) PASSED: 5 late at night + 5 after midnight succeed; 11th request blocked by 2x backstop\n');

    // ----------------------------------------------------------------
    // Requirement 2: Isolated 2x Rolling Backstop Test (Timezone Change to Fresh Day)
    // ----------------------------------------------------------------
    console.log('--- Test (g): Isolated 2x Rolling Backstop with Timezone Jump to Fresh Day ---');
    const userTzJump = `user_tz_jump_${Date.now()}`;
    const dateA = '2026-11-05';
    const dateB = '2026-11-06';
    const dateC = '2026-11-07'; // Fresh local day resulting from user changing timezone to Auckland / Tokyo

    // Step 1: User consumes 5 messages on Date A (18 hours ago, within 24h)
    await db.query(`
      INSERT INTO public.ai_coach_daily_usage (
        firebase_uid, local_date, used_count, in_flight, timezone, updated_at
      ) VALUES (
        $1, $2, 5, 0, 'America/New_York', clock_timestamp() - INTERVAL '18 hours'
      );
    `, [userTzJump, dateA]);

    // Step 2: User consumes 5 messages on Date B (6 hours ago, within 24h)
    await db.query(`
      INSERT INTO public.ai_coach_daily_usage (
        firebase_uid, local_date, used_count, in_flight, timezone, updated_at
      ) VALUES (
        $1, $2, 5, 0, 'America/New_York', clock_timestamp() - INTERVAL '6 hours'
      );
    `, [userTzJump, dateB]);

    // Total consumed in last 24h: 5 + 5 = 10 messages (= 2x daily limit of 5).
    // Now, user switches timezone to 'Pacific/Auckland', landing on a fresh local day Date C ('2026-11-07').
    // On Date C, used_count = 0 in database. If only daily limit was checked, 0 < 5 would ALLOW!
    // But the 2x rolling backstop (10 in 24h) MUST strictly BLOCK the request!
    const freshDayResQuery = await db.query(
      `SELECT public.reserve_ai_coach_quota($1, $2, 5, 'Pacific/Auckland') AS result;`,
      [userTzJump, dateC]
    );
    const freshDayRes = freshDayResQuery.rows[0].result;

    assert.strictEqual(freshDayRes.allowed, false, 'Fresh day reservation must be blocked by 2x rolling backstop');
    assert.strictEqual(freshDayRes.rolling24hCapReached, true, 'rolling24hCapReached must be true');
    assert.strictEqual(freshDayRes.usedToday, 10, 'usedToday must reflect the 10 messages consumed in rolling 24h window');
    assert.strictEqual(freshDayRes.dailyLimit, 5, 'dailyLimit must remain 5');
    console.log('✓ Test (g) PASSED: User with 10 msgs in 24h hopping to a fresh local day is blocked by rolling cap (not daily limit)\n');

    // ----------------------------------------------------------------
    // Requirement 1 (h): Photo Quota Concurrency Test (20 Parallel Callers, Limit 5)
    // ----------------------------------------------------------------
    console.log('--- Test (h): Photo Quota Concurrency Test (20 Parallel Callers, Limit 5) ---');
    const photoUid = `user_photo_concurrent_${Date.now()}`;
    const photoDate = '2026-11-06';
    const photoLimit = 5;

    let photoResults;
    if (db.isNative) {
      const clients = await Promise.all(
        Array.from({ length: 20 }, () => db.pgPool.connect())
      );
      try {
        const promises = clients.map(client =>
          client.query(
            `SELECT public.reserve_photo_quota($1, $2, $3, 'UTC') AS result;`,
            [photoUid, photoDate, photoLimit]
          )
        );
        photoResults = await Promise.all(promises);
      } finally {
        clients.forEach(client => client.release());
      }
    } else {
      const promises = [];
      for (let i = 0; i < 20; i++) {
        promises.push(
          db.query(
            `SELECT public.reserve_photo_quota($1, $2, $3, 'UTC') AS result;`,
            [photoUid, photoDate, photoLimit]
          )
        );
      }
      photoResults = await Promise.all(promises);
    }

    let photoAllowedCount = 0;
    let photoBlockedCount = 0;

    for (const r of photoResults) {
      const resData = r.rows[0].result;
      if (resData.allowed === true) photoAllowedCount++;
      else if (resData.allowed === false) photoBlockedCount++;
    }

    console.log(`Photo Results: ${photoAllowedCount} ALLOWED (expected 5), ${photoBlockedCount} BLOCKED (expected 15)`);
    assert.strictEqual(photoAllowedCount, 5, 'Exactly 5 concurrent photo requests must be allowed');
    assert.strictEqual(photoBlockedCount, 15, 'Exactly 15 concurrent photo requests must be blocked');

    const photoRowCheck = await db.query(
      `SELECT in_flight, used_count FROM public.ai_photo_daily_usage WHERE firebase_uid = $1 AND local_date = $2;`,
      [photoUid, photoDate]
    );
    assert.strictEqual(photoRowCheck.rows.length, 1, 'Photo usage row must exist');
    assert.strictEqual(photoRowCheck.rows[0].in_flight, 5, 'in_flight count must be exactly 5');
    assert.strictEqual(photoRowCheck.rows[0].used_count, 0, 'used_count must be 0');
    console.log('✓ Test (h) PASSED: Row locking (SELECT ... FOR UPDATE) strictly prevented photo over-allocation\n');

    // ----------------------------------------------------------------
    // Requirement 1 (i): Photo Quota Anon Role Execution Rejection
    // ----------------------------------------------------------------
    console.log('--- Test (i): Photo Quota Security Audit — Anon Role Execution Rejection ---');
    let photoAnonRejected = false;
    try {
      await db.exec('SET ROLE anon;');
      await db.query(`SELECT public.reserve_photo_quota('malicious_actor', '2026-11-06', 5, 'UTC');`);
    } catch (err) {
      photoAnonRejected = true;
      console.log(`Expected rejection caught: [${err.code || 'ERR'}] ${err.message}`);
      assert.match(err.message, /permission denied/i, 'Error message must specify permission denied');
    } finally {
      await db.exec('RESET ROLE;');
    }
    assert.strictEqual(photoAnonRejected, true, 'Calling reserve_photo_quota as anon role MUST fail');
    console.log('✓ Test (i) PASSED: Photo function execution strictly revoked from anon role\n');

    // ----------------------------------------------------------------
    // Requirement 1 (j): Photo Quota Commit & Release Functions
    // ----------------------------------------------------------------
    console.log('--- Test (j): Photo Quota Commit & Release Functionality ---');
    const photoOpsUid = `user_photo_ops_${Date.now()}`;
    await db.query(`
      INSERT INTO public.ai_photo_daily_usage (
        firebase_uid, local_date, used_count, in_flight, timezone, updated_at
      ) VALUES (
        $1, $2, 0, 2, 'UTC', clock_timestamp()
      );
    `, [photoOpsUid, photoDate]);

    // Commit 1 quota
    const commitRes = await db.query(
      `SELECT public.commit_photo_quota($1, $2, 'UTC') AS new_used;`,
      [photoOpsUid, photoDate]
    );
    assert.strictEqual(commitRes.rows[0].new_used, 1, 'Commit must return new used count 1');

    // Release 1 in-flight
    await db.query(
      `SELECT public.release_photo_reservation($1, $2);`,
      [photoOpsUid, photoDate]
    );

    const opsCheck = await db.query(
      `SELECT in_flight, used_count FROM public.ai_photo_daily_usage WHERE firebase_uid = $1 AND local_date = $2;`,
      [photoOpsUid, photoDate]
    );
    assert.strictEqual(opsCheck.rows[0].used_count, 1, 'used_count must be 1');
    assert.strictEqual(opsCheck.rows[0].in_flight, 0, 'in_flight must be 0 after commit and release');
    console.log('✓ Test (j) PASSED: commit_photo_quota and release_photo_reservation operate accurately\n');

    console.log('================================================================');
    console.log('   ALL REAL POSTGRESQL INTEGRATION & SECURITY TESTS PASSED!');
    console.log('================================================================\n');
  } finally {
    await db.close();
  }
}

runRealPostgresIntegrationSuite().catch((err) => {
  console.error('\n❌ Real Postgres Test Suite FAILED:', err);
  process.exit(1);
});
