-- =============================================================
-- Migration 003: AI Coach Shared Quota Storage & Atomic Reservation
-- Supports Cloud Run multi-instance scaling & ephemeral containers
-- =============================================================

-- 1. Ensure timezone tracking columns exist on user_profiles
ALTER TABLE public.user_profiles
ADD COLUMN IF NOT EXISTS timezone TEXT NOT NULL DEFAULT 'UTC';

ALTER TABLE public.user_profiles
ADD COLUMN IF NOT EXISTS timezone_changed_at TIMESTAMPTZ DEFAULT now();

-- 2. Create ai_coach_daily_usage table
CREATE TABLE IF NOT EXISTS public.ai_coach_daily_usage (
    firebase_uid        TEXT NOT NULL,
    local_date          TEXT NOT NULL,           -- Format: 'YYYY-MM-DD' in user's validated timezone
    used_count          INTEGER NOT NULL DEFAULT 0,
    in_flight           INTEGER NOT NULL DEFAULT 0,
    last_reservation_at TIMESTAMPTZ,             -- Timestamp of latest in-flight reservation
    timezone            TEXT NOT NULL DEFAULT 'UTC',
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (firebase_uid, local_date)
);

-- Index for rapid account deletion and audit lookups
CREATE INDEX IF NOT EXISTS idx_ai_coach_usage_uid
    ON public.ai_coach_daily_usage (firebase_uid);

CREATE INDEX IF NOT EXISTS idx_ai_coach_usage_updated
    ON public.ai_coach_daily_usage (firebase_uid, updated_at);

-- 3. Enable Row Level Security (RLS) with no public access
ALTER TABLE public.ai_coach_daily_usage ENABLE ROW LEVEL SECURITY;

-- Note: No policies are created for anon or authenticated roles.
-- All access is mediated through the backend API proxy using the Supabase service_role key.

-- 4. Atomic Quota Reservation Function (RPC)
-- Row-level locking via SELECT ... FOR UPDATE guarantees no race conditions across Cloud Run instances.
-- Fixes first-insert race with INSERT ... ON CONFLICT DO NOTHING before FOR UPDATE.
-- Automatically recovers stale in_flight reservations older than 2 minutes.
CREATE OR REPLACE FUNCTION public.reserve_ai_coach_quota(
    p_uid TEXT,
    p_date TEXT,
    p_limit INT,
    p_timezone TEXT DEFAULT 'UTC'
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_used INT := 0;
    v_inflight INT := 0;
    v_last_res TIMESTAMPTZ;
    v_effective_inflight INT := 0;
    v_allowed BOOLEAN := false;
    v_remaining INT := 0;
    v_now TIMESTAMPTZ := clock_timestamp();
    v_rolling_24h_used INT := 0;
BEGIN
    -- Backstop: Enforce rolling 24-hour reservation cap across all date boundaries.
    -- Set to 2x daily limit so legitimate users consuming quota across local midnight
    -- (e.g. 5 messages at 23:30 and 5 messages at 00:30) are never blocked, while capping
    -- timezone manipulation at 2x daily allowance (10 for Free, 100 for Pro) within any 24h window.
    SELECT COALESCE(SUM(used_count), 0)
    INTO v_rolling_24h_used
    FROM public.ai_coach_daily_usage
    WHERE firebase_uid = p_uid
      AND updated_at >= (v_now - INTERVAL '24 hours');

    IF v_rolling_24h_used >= (p_limit * 2) THEN
        RETURN jsonb_build_object(
            'allowed', false,
            'usedToday', v_rolling_24h_used,
            'inFlight', 0,
            'dailyLimit', p_limit,
            'remaining', 0,
            'isLimitReached', true,
            'rolling24hCapReached', true,
            'localDay', p_date
        );
    END IF;

    -- Fix first-insert race: ensure row exists atomically before locking
    INSERT INTO public.ai_coach_daily_usage (
        firebase_uid, local_date, used_count, in_flight, last_reservation_at, timezone, updated_at
    ) VALUES (
        p_uid, p_date, 0, 0, NULL, p_timezone, v_now
    )
    ON CONFLICT (firebase_uid, local_date) DO NOTHING;

    -- Strict row-level lock on guaranteed existing row
    SELECT used_count, in_flight, last_reservation_at
    INTO v_used, v_inflight, v_last_res
    FROM public.ai_coach_daily_usage
    WHERE firebase_uid = p_uid AND local_date = p_date
    FOR UPDATE;

    -- Stale reservation recovery: if reservation is older than 2 minutes, ignore stale in_flight count
    IF v_last_res IS NOT NULL AND v_last_res < (v_now - INTERVAL '2 minutes') THEN
        v_effective_inflight := 0;
    ELSE
        v_effective_inflight := v_inflight;
    END IF;

    -- Quota evaluation
    IF (v_used + v_effective_inflight) < p_limit THEN
        v_allowed := true;
        v_inflight := v_effective_inflight + 1;
        v_remaining := GREATEST(0, p_limit - (v_used + v_inflight));

        UPDATE public.ai_coach_daily_usage
        SET in_flight = v_inflight,
            last_reservation_at = v_now,
            timezone = p_timezone,
            updated_at = v_now
        WHERE firebase_uid = p_uid AND local_date = p_date;

        RETURN jsonb_build_object(
            'allowed', true,
            'usedToday', v_used,
            'inFlight', v_inflight,
            'dailyLimit', p_limit,
            'remaining', v_remaining,
            'isLimitReached', false,
            'rolling24hCapReached', false,
            'localDay', p_date
        );
    ELSE
        RETURN jsonb_build_object(
            'allowed', false,
            'usedToday', v_used,
            'inFlight', v_effective_inflight,
            'dailyLimit', p_limit,
            'remaining', 0,
            'isLimitReached', true,
            'rolling24hCapReached', false,
            'localDay', p_date
        );
    END IF;
END;
$$;

-- 5. Atomic Reservation Release Function (RPC)
CREATE OR REPLACE FUNCTION public.release_ai_coach_reservation(
    p_uid TEXT,
    p_date TEXT
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    UPDATE public.ai_coach_daily_usage
    SET in_flight = GREATEST(0, in_flight - 1),
        updated_at = clock_timestamp()
    WHERE firebase_uid = p_uid AND local_date = p_date;
END;
$$;

-- 6. Atomic Quota Commit Function (RPC)
CREATE OR REPLACE FUNCTION public.commit_ai_coach_quota(
    p_uid TEXT,
    p_date TEXT,
    p_timezone TEXT DEFAULT 'UTC'
)
RETURNS INT
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_new_used INT;
BEGIN
    INSERT INTO public.ai_coach_daily_usage (
        firebase_uid, local_date, used_count, in_flight, timezone, updated_at
    ) VALUES (
        p_uid, p_date, 1, 0, p_timezone, clock_timestamp()
    )
    ON CONFLICT (firebase_uid, local_date) DO UPDATE
    SET used_count = public.ai_coach_daily_usage.used_count + 1,
        in_flight = GREATEST(0, public.ai_coach_daily_usage.in_flight - 1),
        timezone = p_timezone,
        updated_at = clock_timestamp()
    RETURNING used_count INTO v_new_used;

    RETURN v_new_used;
END;
$$;

-- 7. Hardened RPC Permissions
-- Explicitly revoke from PUBLIC, anon, and authenticated roles to guarantee no unauthorized direct client invocation
REVOKE EXECUTE ON FUNCTION public.reserve_ai_coach_quota(TEXT, TEXT, INT, TEXT) FROM PUBLIC, anon, authenticated;
REVOKE EXECUTE ON FUNCTION public.release_ai_coach_reservation(TEXT, TEXT) FROM PUBLIC, anon, authenticated;
REVOKE EXECUTE ON FUNCTION public.commit_ai_coach_quota(TEXT, TEXT, TEXT) FROM PUBLIC, anon, authenticated;

-- Grant execution strictly to service_role (used by backend API proxy)
GRANT EXECUTE ON FUNCTION public.reserve_ai_coach_quota(TEXT, TEXT, INT, TEXT) TO service_role;
GRANT EXECUTE ON FUNCTION public.release_ai_coach_reservation(TEXT, TEXT) TO service_role;
GRANT EXECUTE ON FUNCTION public.commit_ai_coach_quota(TEXT, TEXT, TEXT) TO service_role;
