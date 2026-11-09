# Supabase Migrations

Canonical migrations for the KcalGrind project are maintained in the root Supabase directory:
`supabase/migrations/`

This includes:
- `001_initial_schema.sql`: Core user profiles, logs, and AI analysis tables
- `002_ai_cleanup_cron.sql`: Scheduled cleanup definitions
- `003_ai_coach_daily_usage.sql`: AI Coach shared quota storage, atomic reservation RPC with 2-minute stale recovery, and timezone locking
- `004_user_custom_data_and_subscriptions.sql`: Custom foods, saved meals, recipes, and user subscriptions with RLS enabled
