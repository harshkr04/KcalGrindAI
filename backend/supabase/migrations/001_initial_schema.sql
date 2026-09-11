-- =============================================================
-- Lumina Nutrition — Supabase Schema (mirrors Room/SQLite)
-- Phase 10: Cloud Sync
-- =============================================================
-- Run this migration via: supabase db push
-- Or paste into the Supabase SQL Editor (Dashboard → SQL)
-- =============================================================

-- 1. user_profiles — keyed by Firebase UID, not auto-increment
CREATE TABLE IF NOT EXISTS public.user_profiles (
  firebase_uid  text PRIMARY KEY,
  email         text,
  goal          text NOT NULL,
  units         text NOT NULL DEFAULT 'metric',
  age           smallint NOT NULL,
  height_cm     real NOT NULL,
  weight_kg     real NOT NULL,
  goal_weight_kg real NOT NULL,
  activity_level text NOT NULL,
  diet_tags     text[] NOT NULL DEFAULT '{}',
  allergies     text[] NOT NULL DEFAULT '{}',
  targets_source text NOT NULL DEFAULT 'recommended',
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now()
);

-- 2. nutrition_goals — 1:1 with user_profiles
CREATE TABLE IF NOT EXISTS public.nutrition_goals (
  firebase_uid  text PRIMARY KEY REFERENCES public.user_profiles(firebase_uid) ON DELETE CASCADE,
  calories      smallint NOT NULL,
  protein_g     real NOT NULL,
  carbs_g       real NOT NULL,
  fat_g         real NOT NULL,
  water_liters  real NOT NULL DEFAULT 2.0,
  water_glasses smallint NOT NULL DEFAULT 8,
  bmr           real,
  tdee          real,
  is_custom     boolean NOT NULL DEFAULT false,
  updated_at    timestamptz NOT NULL DEFAULT now()
);

-- 3. meal_logs — identity PK, FK to user_profiles
CREATE TABLE IF NOT EXISTS public.meal_logs (
  id              bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  firebase_uid    text NOT NULL REFERENCES public.user_profiles(firebase_uid) ON DELETE CASCADE,
  local_id        bigint,          -- Room auto-increment id for mapping
  log_date        date NOT NULL,
  meal_type       text NOT NULL,   -- breakfast|lunch|dinner|snack
  total_calories  real NOT NULL DEFAULT 0,
  logged_at       timestamptz NOT NULL DEFAULT now(),
  source          text NOT NULL DEFAULT 'manual',
  synced          boolean NOT NULL DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_meal_logs_uid_date
  ON public.meal_logs (firebase_uid, log_date);

-- 4. food_log_items — FK to meal_logs
CREATE TABLE IF NOT EXISTS public.food_log_items (
  id                  bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  meal_log_id         bigint NOT NULL REFERENCES public.meal_logs(id) ON DELETE CASCADE,
  local_id            bigint,      -- Room auto-increment id for mapping
  local_meal_log_id   bigint,      -- Room meal_log id for mapping
  food_id             bigint,      -- nullable (AI ad-hoc items)
  name                text NOT NULL,
  brand               text,
  serving_description text NOT NULL,
  serving_grams       real NOT NULL,
  calories            real NOT NULL,
  protein_g           real NOT NULL DEFAULT 0,
  carbs_g             real NOT NULL DEFAULT 0,
  fat_g               real NOT NULL DEFAULT 0,
  fiber_g             real NOT NULL DEFAULT 0,
  source              text NOT NULL DEFAULT 'manual',
  confidence          real,        -- 0.0–1.0
  confirmed           boolean NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_food_log_items_meal
  ON public.food_log_items (meal_log_id);

-- 5. weight_entries — FK to user_profiles
CREATE TABLE IF NOT EXISTS public.weight_entries (
  id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  firebase_uid  text NOT NULL REFERENCES public.user_profiles(firebase_uid) ON DELETE CASCADE,
  local_id      bigint,
  weight_kg     real NOT NULL,
  log_date      date NOT NULL,
  note          text,
  logged_at     timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_weight_entries_uid
  ON public.weight_entries (firebase_uid);

-- 6. water_logs — FK to user_profiles
CREATE TABLE IF NOT EXISTS public.water_logs (
  id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  firebase_uid  text NOT NULL REFERENCES public.user_profiles(firebase_uid) ON DELETE CASCADE,
  local_id      bigint,
  log_date      date NOT NULL,
  amount_ml     smallint NOT NULL,
  logged_at     timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_water_logs_uid_date
  ON public.water_logs (firebase_uid, log_date);

-- 7. ai_analysis_log — audit trail, NOT the live data path
CREATE TABLE IF NOT EXISTS public.ai_analysis_log (
  id                  bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  firebase_uid        text NOT NULL REFERENCES public.user_profiles(firebase_uid) ON DELETE CASCADE,
  local_id            bigint,
  input_type          text NOT NULL,       -- photo|voice|text
  raw_input_ref       text,                -- image path or transcript
  result_json         jsonb NOT NULL,      -- structured JSON, not text
  overall_confidence  real,
  created_at          timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_ai_analysis_uid
  ON public.ai_analysis_log (firebase_uid);

CREATE INDEX IF NOT EXISTS idx_ai_analysis_created
  ON public.ai_analysis_log (created_at);

-- 8. conversations — FK to user_profiles
CREATE TABLE IF NOT EXISTS public.conversations (
  id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  firebase_uid  text NOT NULL REFERENCES public.user_profiles(firebase_uid) ON DELETE CASCADE,
  local_id      bigint,
  started_at    timestamptz NOT NULL DEFAULT now(),
  last_message_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_conversations_uid
  ON public.conversations (firebase_uid);

-- 9. messages — FK to conversations
CREATE TABLE IF NOT EXISTS public.messages (
  id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  conversation_id   bigint NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
  local_id          bigint,
  local_conversation_id bigint,
  role              text NOT NULL,         -- user|assistant
  text_content      text NOT NULL,
  structured_data   jsonb,                 -- suggested meal card, etc.
  created_at        timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation
  ON public.messages (conversation_id);

-- =============================================================
-- Row Level Security (RLS) — enable on all user-data tables
-- Service-role key bypasses RLS, but enabling it is defense-in-depth
-- =============================================================
ALTER TABLE public.user_profiles    ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.nutrition_goals  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.meal_logs        ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.food_log_items   ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.weight_entries   ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.water_logs       ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_analysis_log  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversations    ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages         ENABLE ROW LEVEL SECURITY;

-- Policy: service-role can do everything (our backend uses service-role key)
-- No anon/authenticated policies needed since the Android app never talks
-- to Supabase directly — all access goes through our backend.
