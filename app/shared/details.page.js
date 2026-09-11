/* ==========================================================================
   details.page.js — Personal Details (Screen 3) page logic.
   Phase 2B: full state management with empty / focused / valid / invalid /
   boundary states, trailing status icons, error shake, scroll-to-first-error,
   metric ↔ imperial lossless conversion, persistent canonical metric storage.
   ========================================================================== */
document.addEventListener('DOMContentLoaded', function () {
  'use strict';

  var state = VF.ui.initScreen('details', { title: 'Tell us about yourself' });
  if (!state) return;
  if (VF.ui.qp('resumed') === '1') VF.ui.snackbar('Picked up where you left off.', 'restore');

  var N = VF.nutrition;
  var units = state.units || 'metric';
  var touched = {}; // tracks which fields the user has interacted with

  /* --- DOM refs ---------------------------------------------------------- */
  var ageEl       = document.getElementById('age');
  var heightEl    = document.getElementById('height');
  var heightFtEl  = document.getElementById('height-ft');
  var heightInEl  = document.getElementById('height-in');
  var weightEl    = document.getElementById('weight');
  var goalWtEl    = document.getElementById('goalweight');
  var cta         = document.getElementById('continue');
  var metricBlk   = document.querySelector('[data-metric]');
  var imperialBlk = document.querySelector('[data-imperial]');
  var unitBtns    = document.querySelectorAll('.unit-seg');
  var fAge        = document.getElementById('f-age');
  var fHeight     = document.getElementById('f-height');
  var fHeightFt   = document.getElementById('f-height-ft');
  var fHeightIn   = document.getElementById('f-height-in');
  var fWeight     = document.getElementById('f-weight');
  var fGoalWt     = document.getElementById('f-goalweight');
  var ageMsg      = document.getElementById('age-msg');
  var heightMsg   = document.getElementById('height-msg');
  var weightMsg   = document.getElementById('weight-msg');
  var goalWtMsg   = document.getElementById('goalweight-msg');
  var ageIcon     = document.getElementById('age-icon');
  var heightIcon  = document.getElementById('height-icon');
  var heightFtIcon = document.getElementById('height-ft-icon');
  var heightInIcon = document.getElementById('height-in-icon');
  var weightIcon  = document.getElementById('weight-icon');
  var goalWtIcon  = document.getElementById('goalweight-icon');

  /* --- Trailing icon helper ---------------------------------------------- */
  function trailIcon(el, result) {
    if (!el) return;
    if (result.state === 'valid') {
      el.innerHTML = '<span class="material-symbols-outlined text-secondary text-[20px]" style="font-variation-settings:\'FILL\' 1;">check_circle</span>';
    } else if (result.state === 'invalid') {
      el.innerHTML = '<span class="material-symbols-outlined text-error text-[20px]" style="font-variation-settings:\'FILL\' 1;">error</span>';
    } else {
      el.innerHTML = '';
    }
  }

  /* --- Error shake ------------------------------------------------------- */
  function shake(field) {
    if (!field) return;
    field.classList.add('vf-shake');
    field.addEventListener('animationend', function h() {
      field.classList.remove('vf-shake');
      field.removeEventListener('animationend', h);
    });
  }

  /* --- Unit toggle ------------------------------------------------------- */
  function applyUnits(u) {
    units = u;
    VF.store.patch({ units: u });
    unitBtns.forEach(function (btn) {
      var on = btn.dataset.units === u;
      btn.classList.toggle('bg-primary', on);
      btn.classList.toggle('text-on-primary', on);
      btn.classList.toggle('shadow-sm', on);
      btn.classList.toggle('text-on-surface-variant', !on);
      btn.setAttribute('aria-checked', String(on));
    });
    var isMetric = u === 'metric';
    metricBlk.classList.toggle('hidden', !isMetric);
    imperialBlk.classList.toggle('hidden', isMetric);

    /* Sync height display values from canonical cm */
    var s = VF.store.get();
    if (s.heightCm !== null) {
      if (isMetric) {
        heightEl.value = Math.round(s.heightCm);
      } else {
        var fi = N.cmToFtIn(s.heightCm);
        heightFtEl.value = fi.ft;
        heightInEl.value = fi.inch;
      }
    }
    /* Sync weight labels */
    var wLabelEl = weightEl.parentElement.querySelector('.m3-label');
    var gwLabelEl = goalWtEl.parentElement.querySelector('.m3-label');
    if (wLabelEl) wLabelEl.textContent = isMetric ? 'Current weight (kg)' : 'Current weight (lb)';
    if (gwLabelEl) gwLabelEl.textContent = isMetric ? 'Goal weight (kg)' : 'Goal weight (lb)';

    /* Sync weight display values from canonical kg */
    if (s.weightKg !== null) {
      weightEl.value = isMetric ? N.round1(s.weightKg) : N.kgToLb(s.weightKg);
    }
    if (s.goalWeightKg !== null) {
      goalWtEl.value = isMetric ? N.round1(s.goalWeightKg) : N.kgToLb(s.goalWeightKg);
    }
  }

  unitBtns.forEach(function (btn) {
    btn.addEventListener('click', function () {
      VF.ui.haptics.select();
      applyUnits(btn.dataset.units);
      validate(false);
    });
  });

  /* --- Populate from state ----------------------------------------------- */
  if (state.age !== null) { ageEl.value = state.age; touched.age = true; }
  if (state.heightCm !== null) {
    heightEl.value = Math.round(state.heightCm);
    var fi = N.cmToFtIn(state.heightCm);
    heightFtEl.value = fi.ft;
    heightInEl.value = fi.inch;
    touched.height = true;
  }
  if (state.weightKg !== null) {
    weightEl.value = units === 'metric' ? N.round1(state.weightKg) : N.kgToLb(state.weightKg);
    touched.weight = true;
  }
  if (state.goalWeightKg !== null) {
    goalWtEl.value = units === 'metric' ? N.round1(state.goalWeightKg) : N.kgToLb(state.goalWeightKg);
    touched.goalweight = true;
  }
  applyUnits(units);

  /* --- Validation -------------------------------------------------------- */
  function showMsg(el, msg, cls) {
    if (!el) return;
    el.textContent = msg;
    el.className = cls + ' mt-1 px-1';
  }

  function fieldState(field, result) {
    if (!field) return;
    field.classList.remove('is-invalid', 'is-valid');
    if (result.state === 'invalid') field.classList.add('is-invalid');
    else if (result.state === 'valid') field.classList.add('is-valid');
  }

  function heightFieldState(result) {
    fieldState(fHeight, result);
    fieldState(fHeightFt, result);
    fieldState(fHeightIn, result);
  }

  function heightTrailIcon(result) {
    trailIcon(heightIcon, result);
    trailIcon(heightFtIcon, result);
    trailIcon(heightInIcon, result);
  }

  function readHeightCm() {
    if (units === 'metric') return heightEl.value;
    if (!heightFtEl.value && !heightInEl.value) return '';
    return N.ftInToCm(heightFtEl.value, heightInEl.value) || '';
  }

  function readWeightKg(el) {
    var v = el.value;
    if (v === '' || v === undefined) return '';
    return units === 'imperial' ? N.lbToKg(v) : Number(v);
  }

  /**
   * @param {boolean} showAll - when true, validates and shows errors on ALL
   *   fields (used on submit). When false, only shows errors on touched fields.
   * @returns {boolean} true if the form is valid
   */
  function validate(showAll) {
    var a = N.validateAge(ageEl.value);
    var h = N.validateHeightCm(readHeightCm(), units);
    var w = N.validateWeightKg(readWeightKg(weightEl), units, 'Weight');

    /* Apply states to touched or all fields */
    if (showAll || touched.age) {
      fieldState(fAge, a);
      trailIcon(ageIcon, a);
      if (a.state !== 'empty') showMsg(ageMsg, a.message || 'Looks good.', a.state === 'invalid' ? 'vf-error' : 'vf-help');
      else showMsg(ageMsg, 'In whole years.', 'vf-help');
    }

    if (showAll || touched.height) {
      heightFieldState(h);
      heightTrailIcon(h);
      if (h.state !== 'empty') showMsg(heightMsg, h.message || 'Looks good.', h.state === 'invalid' ? 'vf-error' : 'vf-help');
      else showMsg(heightMsg, 'Height helps size your calorie target.', 'vf-help');
    }

    if (showAll || touched.weight) {
      fieldState(fWeight, w);
      trailIcon(weightIcon, w);
      if (w.state !== 'empty') showMsg(weightMsg, w.message || 'Looks good.', w.state === 'invalid' ? 'vf-error' : 'vf-help');
      else showMsg(weightMsg, 'Your starting point.', 'vf-help');
    }

    /* Goal weight is required in Phase 2B. */
    var gw = readWeightKg(goalWtEl);
    var gwR = N.validateWeightKg(gw, units, 'Goal weight');
    if (showAll || touched.goalweight) {
      fieldState(fGoalWt, gwR);
      trailIcon(goalWtIcon, gwR);
      if (gwR.state !== 'empty') showMsg(goalWtMsg, gwR.message || 'Looks good.', gwR.state === 'invalid' ? 'vf-error' : 'vf-help');
      else showMsg(goalWtMsg, "Where you'd like to be.", 'vf-help');
    }

    /* CTA gating — require age, height, current weight, and goal weight valid */
    var ok = a.state === 'valid' && h.state === 'valid' && w.state === 'valid' && gwR.state === 'valid';
    VF.ui.gate(cta, ok, ok ? '' : 'Fill in your details to continue');

    /* Persist valid values immediately */
    var patch = {};
    if (a.state === 'valid') patch.age = a.value;
    if (h.state === 'valid') patch.heightCm = h.value;
    if (w.state === 'valid') patch.weightKg = w.value;
    if (gwR.state === 'valid') patch.goalWeightKg = gwR.value;
    if (Object.keys(patch).length) VF.store.patch(patch);

    /* Goal weight note — gentle, non-blocking guidance */
    if (touched.goalweight && goalWtEl.value && gwR.state === 'valid' && w.state === 'valid') {
      var note = N.goalWeightNote(state.goal, patch.weightKg || state.weightKg, patch.goalWeightKg || state.goalWeightKg);
      if (note) showMsg(goalWtMsg, note, 'vf-help');
    }

    return ok;
  }

  /* --- Field event wiring ------------------------------------------------ */
  var fieldMap = [
    { el: ageEl,      key: 'age' },
    { el: heightEl,   key: 'height' },
    { el: heightFtEl, key: 'height' },
    { el: heightInEl, key: 'height' },
    { el: weightEl,   key: 'weight' },
    { el: goalWtEl,   key: 'goalweight' }
  ];

  fieldMap.forEach(function (f) {
    if (!f.el) return;
    f.el.addEventListener('input', function () {
      touched[f.key] = true;
      validate(false);
    });
    f.el.addEventListener('blur', function () {
      touched[f.key] = true;
      validate(false);
    });
    f.el.addEventListener('focus', function () {
      /* On focus, scroll the field into view so it's never hidden behind the
         footer — especially important when the soft keyboard is open. */
      setTimeout(function () {
        f.el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }, 250);
    });
  });

  /* Initial validation — only show state on pre-populated fields */
  validate(false);

  /* --- Continue / submit ------------------------------------------------- */
  cta.addEventListener('click', function () {
    /* Mark all required fields as touched and re-validate */
    touched.age = true;
    touched.height = true;
    touched.weight = true;
    touched.goalweight = true;
    var ok = validate(true);

    if (!ok) {
      /* Shake the first invalid field and scroll to it */
      VF.ui.haptics.warn();
      var firstBad = null;
      var a = N.validateAge(ageEl.value);
      var h = N.validateHeightCm(readHeightCm(), units);
      var w = N.validateWeightKg(readWeightKg(weightEl), units, 'Weight');

      if (a.state !== 'valid') { firstBad = fAge; shake(fAge); }
      else if (h.state !== 'valid') {
        firstBad = units === 'metric' ? fHeight : fHeightFt;
        shake(units === 'metric' ? fHeight : fHeightFt);
        if (units === 'imperial') shake(fHeightIn);
      }
      else if (w.state !== 'valid') { firstBad = fWeight; shake(fWeight); }
      else {
        var gw = readWeightKg(goalWtEl);
        var gwR = N.validateWeightKg(gw, units, 'Goal weight');
        if (gwR.state !== 'valid') { firstBad = fGoalWt; shake(fGoalWt); }
      }
      if (firstBad) {
        firstBad.scrollIntoView({ behavior: 'smooth', block: 'center' });
        var inp = firstBad.querySelector('.m3-input');
        if (inp) setTimeout(function () { inp.focus(); }, 350);
      }
      return;
    }

    VF.ui.haptics.confirm();
    VF.ui.advance('details');
  });

  /* Prevent actual form submission */
  var form = document.getElementById('form');
  if (form) {
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      cta.click();
    });
  }
});
