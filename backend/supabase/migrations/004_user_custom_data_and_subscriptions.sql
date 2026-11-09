-- =============================================================
-- Migration 004: User Custom Foods, Meals, Recipes & Subscriptions Schema
-- Completes Supabase relational tables mirroring client Room database
-- =============================================================

-- 1. daily_goals
CREATE TABLE IF NOT EXISTS public.daily_goals (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    firebase_uid  TEXT NOT NULL REFERENCES public.user_profiles(firebase_uid) ON DELETE CASCADE,
    calories      REAL,
    water_ml      SMALLINT,
    goal_date     DATE NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_daily_goals_uid ON public.daily_goals(firebase_uid);
ALTER TABLE public.daily_goals ENABLE ROW LEVEL SECURITY;

-- 2. custom_foods
CREATE TABLE IF NOT EXISTS public.custom_foods (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    firebase_uid  TEXT NOT NULL REFERENCES public.user_profiles(firebase_uid) ON DELETE CASCADE,
    name          TEXT NOT NULL,
    brand         TEXT,
    serving_desc  TEXT NOT NULL DEFAULT '1 serving',
    serving_grams REAL NOT NULL DEFAULT 100,
    calories      REAL NOT NULL DEFAULT 0,
    protein_g     REAL NOT NULL DEFAULT 0,
    carbs_g       REAL NOT NULL DEFAULT 0,
    fat_g         REAL NOT NULL DEFAULT 0,
    fiber_g       REAL NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_custom_foods_uid ON public.custom_foods(firebase_uid);
ALTER TABLE public.custom_foods ENABLE ROW LEVEL SECURITY;

-- 3. saved_meals
CREATE TABLE IF NOT EXISTS public.saved_meals (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    firebase_uid      TEXT NOT NULL REFERENCES public.user_profiles(firebase_uid) ON DELETE CASCADE,
    name              TEXT NOT NULL,
    default_meal_type TEXT NOT NULL DEFAULT 'LUNCH',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_saved_meals_uid ON public.saved_meals(firebase_uid);
ALTER TABLE public.saved_meals ENABLE ROW LEVEL SECURITY;

-- 4. saved_meal_items (child table to saved_meals)
CREATE TABLE IF NOT EXISTS public.saved_meal_items (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    saved_meal_id BIGINT NOT NULL REFERENCES public.saved_meals(id) ON DELETE CASCADE,
    name          TEXT NOT NULL,
    brand         TEXT,
    serving_desc  TEXT NOT NULL DEFAULT '1 serving',
    serving_grams REAL NOT NULL DEFAULT 100,
    calories      REAL NOT NULL DEFAULT 0,
    protein_g     REAL NOT NULL DEFAULT 0,
    carbs_g       REAL NOT NULL DEFAULT 0,
    fat_g         REAL NOT NULL DEFAULT 0,
    fiber_g       REAL NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_saved_meal_items_meal ON public.saved_meal_items(saved_meal_id);
ALTER TABLE public.saved_meal_items ENABLE ROW LEVEL SECURITY;

-- 5. recipes
CREATE TABLE IF NOT EXISTS public.recipes (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    firebase_uid      TEXT NOT NULL REFERENCES public.user_profiles(firebase_uid) ON DELETE CASCADE,
    name              TEXT NOT NULL,
    description       TEXT,
    meal_type         TEXT NOT NULL DEFAULT 'LUNCH',
    total_calories    REAL NOT NULL DEFAULT 0,
    protein_g         REAL NOT NULL DEFAULT 0,
    carbs_g           REAL NOT NULL DEFAULT 0,
    fat_g             REAL NOT NULL DEFAULT 0,
    fiber_g           REAL NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_recipes_uid ON public.recipes(firebase_uid);
ALTER TABLE public.recipes ENABLE ROW LEVEL SECURITY;

-- 6. recipe_items (child table to recipes)
CREATE TABLE IF NOT EXISTS public.recipe_items (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recipe_id     BIGINT NOT NULL REFERENCES public.recipes(id) ON DELETE CASCADE,
    name          TEXT NOT NULL,
    amount        TEXT,
    calories      REAL NOT NULL DEFAULT 0,
    protein_g     REAL NOT NULL DEFAULT 0,
    carbs_g       REAL NOT NULL DEFAULT 0,
    fat_g         REAL NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_recipe_items_recipe ON public.recipe_items(recipe_id);
ALTER TABLE public.recipe_items ENABLE ROW LEVEL SECURITY;

-- 7. user_subscriptions
CREATE TABLE IF NOT EXISTS public.user_subscriptions (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       TEXT NOT NULL REFERENCES public.user_profiles(firebase_uid) ON DELETE CASCADE,
    status        TEXT NOT NULL DEFAULT 'active',
    plan          TEXT NOT NULL DEFAULT 'free',
    expires_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_user_subscriptions_uid ON public.user_subscriptions(user_id);
ALTER TABLE public.user_subscriptions ENABLE ROW LEVEL SECURITY;
