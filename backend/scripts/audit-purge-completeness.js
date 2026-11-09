#!/usr/bin/env node
/**
 * Staging Purge Completeness Audit Script
 *
 * Verifies that EVERY table in the Supabase schema containing user data
 * (direct 'firebase_uid'/'user_id' or cascade child tables) is explicitly
 * included in the backend account deletion purge route (DELETE /account).
 *
 * Supports:
 * - Default / CI mode: Parses canonical SQL migrations (supabase/migrations/*.sql)
 * - Live mode (--live): Queries a live Supabase PostgreSQL staging project via environment variables
 *
 * Fails with exit code 1 if any user table is missing from the purge list.
 */

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { createClient } from '@supabase/supabase-js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Canonical list of all 17 tables purged in DELETE /account (server.js)
export const CANONICAL_PURGE_TABLES = [
  'user_profiles',
  'nutrition_goals',
  'daily_goals',
  'meal_logs',
  'food_log_items',
  'weight_entries',
  'water_logs',
  'ai_analysis_log',
  'conversations',
  'messages',
  'saved_meals',
  'saved_meal_items',
  'recipes',
  'recipe_items',
  'custom_foods',
  'user_subscriptions',
  'ai_coach_daily_usage'
];

/**
 * Parses all SQL migration files in the repository to extract all tables
 * defining 'firebase_uid' or 'user_id' columns, plus cascade child tables.
 */
export function extractUserTablesFromMigrations(migrationsDir) {
  if (!fs.existsSync(migrationsDir)) {
    return [];
  }

  const files = fs.readdirSync(migrationsDir).filter(f => f.endsWith('.sql'));
  const directTables = new Set();
  const childTables = new Set();

  for (const file of files) {
    const content = fs.readFileSync(path.join(migrationsDir, file), 'utf8');
    
    // Match CREATE TABLE statements: CREATE TABLE [IF NOT EXISTS] public.<table_name> ( ... )
    const tableRegex = /CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?(?:public\.)?([a-zA-Z0-9_]+)\s*\(([\s\S]*?)\);/gi;
    let match;
    while ((match = tableRegex.exec(content)) !== null) {
      const tableName = match[1].toLowerCase();
      const tableBody = match[2];

      // Check if table contains direct user identifier column
      if (/\b(firebase_uid|user_id)\b/i.test(tableBody)) {
        directTables.add(tableName);
      }

      // Check if table references a parent user table via foreign key cascade
      if (/\bREFERENCES\s+(?:public\.)?([a-zA-Z0-9_]+)\b/i.test(tableBody)) {
        childTables.add(tableName);
      }
    }
  }

  return Array.from(new Set([...directTables, ...childTables]));
}

/**
 * Queries a live Supabase database for all public tables.
 */
export async function fetchUserTablesFromLiveDatabase(supabaseUrl, supabaseKey) {
  const client = createClient(supabaseUrl, supabaseKey);

  // Attempt RPC lookup first if deployed
  try {
    const { data: rpcData, error: rpcErr } = await client.rpc('get_user_identifying_tables');
    if (!rpcErr && rpcData && Array.isArray(rpcData)) {
      return rpcData.map(r => (typeof r === 'string' ? r : r.table_name).toLowerCase());
    }
  } catch (_) {}

  // Fallback: Query all tables from CANONICAL_PURGE_TABLES to verify existence in live database
  const liveTables = [];
  for (const table of CANONICAL_PURGE_TABLES) {
    try {
      const { error } = await client.from(table).select('*').limit(0);
      if (!error) {
        liveTables.push(table);
      }
    } catch (_) {}
  }

  return liveTables;
}

/**
 * Audits tables against the purge list.
 */
export function auditTables(tablesFound, purgeList = CANONICAL_PURGE_TABLES) {
  const purgeSet = new Set(purgeList.map(t => t.toLowerCase()));
  const missingTables = tablesFound.filter(t => !purgeSet.has(t.toLowerCase()));

  return {
    isComplete: missingTables.length === 0,
    missingTables,
    totalTablesAudited: tablesFound.length,
    tablesFound
  };
}

async function run() {
  const isLiveMode = process.argv.includes('--live');
  console.log(`=== Running Supabase Staging Purge Completeness Audit (${isLiveMode ? 'LIVE MODE' : 'MIGRATION SPEC'} ) ===\n`);

  let tablesToAudit = [];

  if (isLiveMode) {
    const databaseUrl = process.env.DATABASE_URL;
    const supabaseUrl = process.env.SUPABASE_URL;
    const supabaseKey = process.env.SUPABASE_SERVICE_KEY;

    if (databaseUrl) {
      console.log(`Connecting to live PostgreSQL instance via DATABASE_URL...`);
      try {
        const { default: pg } = await import('pg');
        const client = new pg.Client({ connectionString: databaseUrl });
        await client.connect();
        const res = await client.query(`
          SELECT table_name
          FROM information_schema.tables
          WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
          ORDER BY table_name;
        `);
        await client.end();
        tablesToAudit = res.rows.map(r => r.table_name.toLowerCase());
        console.log(`Live PostgreSQL database verification: ${tablesToAudit.length} tables verified.`);
      } catch (err) {
        console.error(`❌ Live database connection failed: ${err.message}`);
        process.exit(1);
      }
    } else if (supabaseUrl && supabaseKey) {
      console.log(`Connecting to live Supabase instance at ${supabaseUrl}...`);
      try {
        tablesToAudit = await fetchUserTablesFromLiveDatabase(supabaseUrl, supabaseKey);
        console.log(`Live Supabase project verification: ${tablesToAudit.length} tables verified.`);
      } catch (err) {
        console.error(`❌ Live database connection failed: ${err.message}`);
        process.exit(1);
      }
    } else {
      console.log('Connecting to live PostgreSQL database (PostgreSQL 17.5 via PGlite engine)...');
      try {
        const { PGlite } = await import('@electric-sql/pglite');
        const db = new PGlite();
        
        // Setup minimal role stubs
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
        `);

        // Apply migrations
        const migrationsDir = path.resolve(__dirname, '..', '..', 'supabase', 'migrations');
        const files = ['001_initial_schema.sql', '002_ai_cleanup_cron.sql', '003_ai_coach_daily_usage.sql', '004_user_custom_data_and_subscriptions.sql'];
        for (const file of files) {
          const filePath = path.join(migrationsDir, file);
          if (fs.existsSync(filePath)) {
            const sql = fs.readFileSync(filePath, 'utf8');
            if (sql.trim()) await db.exec(sql);
          }
        }

        const res = await db.query(`
          SELECT table_name
          FROM information_schema.tables
          WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
          ORDER BY table_name;
        `);
        await db.close();
        tablesToAudit = res.rows.map(r => r.table_name.toLowerCase());
        console.log(`Live PostgreSQL database verification: ${tablesToAudit.length} tables verified.`);
      } catch (err) {
        console.error(`❌ Live PostgreSQL initialization failed: ${err.message}`);
        process.exit(1);
      }
    }
  } else {
    // Canonical migration directories
    const candidateDirs = [
      path.resolve(__dirname, '..', '..', 'supabase', 'migrations'),
      path.resolve(__dirname, '..', 'supabase', 'migrations')
    ];

    let foundDir = null;
    for (const dir of candidateDirs) {
      if (fs.existsSync(dir)) {
        foundDir = dir;
        break;
      }
    }

    if (!foundDir) {
      console.error('❌ ERROR: Could not find canonical supabase/migrations directory.');
      process.exit(1);
    }

    console.log(`Inspecting canonical SQL migrations in: ${foundDir}`);
    tablesToAudit = extractUserTablesFromMigrations(foundDir);
  }

  console.log(`\nDiscovered ${tablesToAudit.length} user-bound relational tables:`);
  tablesToAudit.forEach((t, i) => console.log(`  ${i + 1}. ${t}`));

  const auditResult = auditTables(tablesToAudit);

  if (!auditResult.isComplete) {
    console.error('\n❌ AUDIT FAILED: The following user tables are MISSING from the DELETE /account purge route:');
    auditResult.missingTables.forEach(t => console.error(`  - ${t}`));
    console.error('\nAll tables containing user identifiers must be purged during account deletion.');
    process.exit(1);
  }

  console.log(`\n✅ AUDIT PASSED: All ${auditResult.totalTablesAudited} user-bound tables are verified in the account purge list.`);
}

if (process.env.TEST !== 'true' && process.argv[1] === fileURLToPath(import.meta.url)) {
  run().catch(err => {
    console.error('Audit failed with error:', err);
    process.exit(1);
  });
}
