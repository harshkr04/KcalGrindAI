/* ==========================================================================
   VF.store — onboarding persistence + step routing.
   Single localStorage record, schema-versioned, written on every change so
   closing the app mid-flow never loses an answer.
   ========================================================================== */
(function (global) {
  'use strict';

  var KEY = 'vitality.onboarding.v1';
  var SCHEMA = 1;

  /* Ordered flow. `counted` steps are the ones the progress bar reports;
     welcome/customize/ready-side-trips stay out of the count so the bar never
     appears to go backwards. */
  var STEPS = [
    { id: 'welcome',     file: '01_welcome.html',     counted: false, optional: false },
    { id: 'goal',        file: '02_goal.html',        counted: true,  optional: false },
    { id: 'details',     file: '03_details.html',     counted: true,  optional: false },
    { id: 'activity',    file: '04_activity.html',    counted: true,  optional: false },
    { id: 'diet',        file: '05_diet.html',        counted: true,  optional: true  },
    { id: 'allergies',   file: '06_allergies.html',   counted: true,  optional: true  },
    { id: 'target',      file: '07_target.html',      counted: true,  optional: false },
    { id: 'customize',   file: '08_customize.html',   counted: false, optional: true  },
    { id: 'permissions', file: '09_permissions.html', counted: true,  optional: true  },
    { id: 'ready',       file: '10_ready.html',       counted: true,  optional: false }
  ];
  var HOME = '11_home.html';

  function defaults() {
    return {
      schemaVersion: SCHEMA,
      startedAt: null,
      updatedAt: null,
      currentStep: 'welcome',
      furthestStep: 'welcome',
      completed: false,
      goal: null,                 // lose | maintain | gain | muscle | healthier | nutrition
      units: 'metric',            // metric | imperial
      age: null,                  // years
      heightCm: null,
      weightKg: null,
      goalWeightKg: null,
      activity: null,             // sedentary | light | moderate | very | extreme
      diet: [],                   // string[]
      dietSkipped: false,
      allergies: [],              // string[]
      allergiesNone: false,
      allergiesSkipped: false,
      recommended: null,          // {calories, protein, carbs, fat, waterL, glasses, bmr, tdee, basis}
      custom: null,               // same shape when the user edits targets
      targetsSource: null,        // 'recommended' | 'custom' | 'fallback'
      permissions: {              // not_requested | granted | denied | skipped | unavailable | requesting
        camera: 'not_requested',
        microphone: 'not_requested',
        notifications: 'not_requested',
        health: 'not_requested'
      },
      permissionsSkipped: false
    };
  }

  var state = null;

  function read() {
    if (state) return state;
    var raw = null;
    try { raw = global.localStorage.getItem(KEY); } catch (e) { raw = null; }
    if (!raw) { state = defaults(); return state; }
    var parsed;
    try { parsed = JSON.parse(raw); } catch (e) { parsed = null; }
    if (!parsed || typeof parsed !== 'object') { state = defaults(); return state; }
    // forward-compatible merge: unknown/missing keys fall back to defaults
    var base = defaults();
    Object.keys(base).forEach(function (k) {
      if (parsed[k] !== undefined && parsed[k] !== null) base[k] = parsed[k];
    });
    base.permissions = Object.assign(defaults().permissions, parsed.permissions || {});
    base.schemaVersion = SCHEMA;
    state = base;
    return state;
  }

  function persist() {
    state.updatedAt = new Date().toISOString();
    if (!state.startedAt) state.startedAt = state.updatedAt;
    try {
      global.localStorage.setItem(KEY, JSON.stringify(state));
    } catch (e) {
      /* Storage blocked (private mode / file:// restrictions). The in-memory
         copy still drives this session; warn once so the user is not silently
         surprised by a lost resume point. */
      if (!persist._warned) {
        persist._warned = true;
        if (global.VF && VF.ui && VF.ui.snackbar) {
          VF.ui.snackbar('Progress can’t be saved on this device, but you can finish setup now.');
        }
      }
    }
    return state;
  }

  function patch(changes) {
    read();
    Object.keys(changes || {}).forEach(function (k) { state[k] = changes[k]; });
    return persist();
  }

  function stepIndex(id) {
    for (var i = 0; i < STEPS.length; i++) if (STEPS[i].id === id) return i;
    return -1;
  }

  function countedSteps() { return STEPS.filter(function (s) { return s.counted; }); }

  function progressFor(id) {
    var counted = countedSteps();
    var pos = -1;
    for (var i = 0; i < counted.length; i++) if (counted[i].id === id) pos = i;
    if (pos < 0) return null;
    return { step: pos + 1, total: counted.length, percent: Math.round(((pos + 1) / counted.length) * 100) };
  }

  /* Marks a step as reached and remembers the furthest point, so "resume"
     lands on the last incomplete step rather than the last visited one. */
  function enter(id) {
    read();
    state.currentStep = id;
    if (stepIndex(id) > stepIndex(state.furthestStep)) state.furthestStep = id;
    return persist();
  }

  function next(id) {
    var i = stepIndex(id);
    return STEPS[Math.min(i + 1, STEPS.length - 1)];
  }
  function prev(id) {
    var i = stepIndex(id);
    return STEPS[Math.max(i - 1, 0)];
  }

  /* The step the user should land on when reopening the app. */
  function resumeTarget() {
    var s = read();
    if (s.completed) return HOME;
    if (!s.goal) return '02_goal.html';
    if (s.age === null || s.heightCm === null || s.weightKg === null || s.goalWeightKg === null) return '03_details.html';
    if (!s.activity) return '04_activity.html';
    if (!s.diet.length && !s.dietSkipped) return '05_diet.html';
    if (!s.allergies.length && !s.allergiesNone && !s.allergiesSkipped) return '06_allergies.html';
    if (!targets()) return '07_target.html';
    if (!s.permissionsSkipped && everyPermissionUntouched()) return '09_permissions.html';
    return '10_ready.html';
    function everyPermissionUntouched() {
      return Object.keys(s.permissions).every(function (k) { return s.permissions[k] === 'not_requested'; });
    }
  }

  function hasProgress() {
    var s = read();
    return !!(s.goal || s.age !== null || s.activity || s.diet.length || s.allergies.length);
  }

  function targets() {
    var s = read();
    return s.custom || s.recommended || null;
  }

  function reset() {
    state = defaults();
    try { global.localStorage.removeItem(KEY); } catch (e) {}
    return state;
  }

  global.VF = global.VF || {};
  global.VF.store = {
    KEY: KEY, STEPS: STEPS, HOME: HOME,
    get: read, patch: patch, enter: enter, reset: reset,
    next: next, prev: prev, stepIndex: stepIndex,
    progressFor: progressFor, resumeTarget: resumeTarget,
    hasProgress: hasProgress, targets: targets
  };
})(window);
