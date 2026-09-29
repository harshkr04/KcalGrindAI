# Kcal Grind AI — Development Workflow

## 1. Phase gating

Work through the phases in `00_MASTER_PLAN.md` §2 in order. Don't start Phase N+1 until Phase N produces a working, demoable build — partial, disconnected screens across many phases at once is how the original prototype ended up with mocked AI everywhere and no real data flow. One phase, fully wired, then the next.

## 2. Branching

- `main` — always buildable, matches what's on the internal testing track.
- `feature/<phase>-<short-name>` branches per unit of work (e.g. `feature/phase2-room-schema`).
- PR into `main` requires: build passes, tests pass, and — critically for this project — a check against the relevant doc in this set (does the DB schema PR match `03_DATABASE.md`? does the new screen match its row in the screen inventory?).

## 3. Agent/session handoff (if using a multi-agent or multi-session process)

Each session/agent should:
1. Read the relevant doc(s) from this set before starting (not just the immediate task ask).
2. Produce or update an artifact the next session can pick up from — code + a short status note, not just chat history.
3. Flag any deviation from the docs explicitly rather than silently improvising — e.g. "screen X in the prototype has no empty state, I designed one, here's what I did."

## 4. Definition of done, per phase

- Code builds and runs on a real device/emulator.
- No mocked data paths remain for anything that phase claims to deliver (the #1 lesson from the current prototype).
- Screen(s) match their row in the screen inventory (`05_UI_DESIGN_SYSTEM.md` §3/§6).
- Data flows through Room as single source of truth — no screen reads directly from a network response.

## 5. Decision log

Keep a running `DECISIONS.md` (separate from these spec docs) recording choices made along the way that weren't pre-specified here — e.g. "chose Nutritionix over Open Food Facts on [date] because barcode hit-rate was too low in testing." This prevents re-litigating settled decisions every time a new session picks up the project.

## 6. When the docs and reality disagree

These docs are a plan, not scripture. If Phase 2 reveals the DB schema needs a field nobody anticipated, update `03_DATABASE.md` in the same PR — don't let the docs go stale. A doc that no longer matches the code is worse than no doc.
