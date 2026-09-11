/* ==========================================================================
   VF.ui — shared onboarding chrome: progress header, navigation, haptics,
   snackbar, offline banner, soft-keyboard state, screen-reader announcements.
   Markup below reuses the Phase 1 onboarding header/footer patterns
   (goal_selection + diet_preferences) so every step looks hand-placed.
   ========================================================================== */
(function (global) {
  'use strict';

  var doc = global.document;

  function qp(name) {
    var m = new RegExp('[?&]' + name + '=([^&#]*)').exec(global.location.search);
    return m ? decodeURIComponent(m[1]) : null;
  }

  /* --- Haptics ------------------------------------------------------------ */
  var haptics = {
    enabled: function () { return typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function'; },
    select:  function () { if (haptics.enabled()) navigator.vibrate(10); },
    confirm: function () { if (haptics.enabled()) navigator.vibrate(20); },
    success: function () { if (haptics.enabled()) navigator.vibrate([10, 40, 16]); },
    warn:    function () { if (haptics.enabled()) navigator.vibrate([12, 60, 12]); }
  };

  /* --- Screen-reader live region ------------------------------------------ */
  function liveRegion() {
    var el = doc.getElementById('vf-live');
    if (!el) {
      el = doc.createElement('div');
      el.id = 'vf-live';
      el.setAttribute('aria-live', 'polite');
      el.setAttribute('role', 'status');
      el.className = 'sr-only';
      el.style.cssText = 'position:absolute;width:1px;height:1px;overflow:hidden;clip:rect(0 0 0 0);white-space:nowrap;';
      doc.body.appendChild(el);
    }
    return el;
  }
  function announce(msg) { liveRegion().textContent = msg; }

  /* --- Snackbar ----------------------------------------------------------- */
  var snackTimer = null;
  function snackbar(message, icon) {
    var el = doc.getElementById('vf-snackbar');
    if (!el) {
      el = doc.createElement('div');
      el.id = 'vf-snackbar';
      el.className = 'vf-snackbar';
      el.setAttribute('role', 'status');
      doc.body.appendChild(el);
    }
    el.innerHTML = '<span class="material-symbols-outlined" style="font-size:20px">' + (icon || 'info') + '</span><span></span>';
    el.lastChild.textContent = message;
    requestAnimationFrame(function () { el.classList.add('is-open'); });
    clearTimeout(snackTimer);
    snackTimer = setTimeout(function () { el.classList.remove('is-open'); }, 3600);
  }

  /* --- Navigation with a subtle exit transition --------------------------- */
  function go(file, opts) {
    opts = opts || {};
    var reduce = global.matchMedia && global.matchMedia('(prefers-reduced-motion: reduce)').matches;
    var url = file + (opts.query ? (file.indexOf('?') > -1 ? '&' : '?') + opts.query : '');
    if (opts.back) try { sessionStorage.setItem('vf.dir', 'back'); } catch (e) {}
    else try { sessionStorage.removeItem('vf.dir'); } catch (e) {}
    if (reduce) { global.location.href = url; return; }
    doc.body.classList.add('vf-leaving');
    setTimeout(function () { global.location.href = url; }, 170);
  }

  /* Continue to the next step — or straight back to Ready when the user came
     here from the Ready summary to edit an answer. */
  function advance(stepId) {
    var back = qp('return');
    if (back === 'ready') { go('10_ready.html'); return; }
    var n = VF.store.next(stepId);
    go(n.file);
  }

  function goBack(stepId) {
    var back = qp('return');
    if (back === 'ready') { go('10_ready.html', { back: true }); return; }
    var p = VF.store.prev(stepId);
    go(p.file, { back: true });
  }

  /* --- Progress header (Phase 1 goal_selection pattern) ------------------- */
  function headerHTML(stepId, opts) {
    opts = opts || {};
    var p = VF.store.progressFor(stepId);
    var editing = qp('return') === 'ready';
    var label = editing ? 'Editing your answer' : (p ? 'Step ' + p.step + ' of ' + p.total : '');
    var pct = editing ? 100 : (p ? p.percent : 0);
    return '' +
      '<header class="flex flex-col w-full px-container-padding pt-margin-mobile pb-stack-sm relative z-20">' +
        '<div class="flex items-center justify-between h-12 mb-stack-md">' +
          '<button id="vf-back" aria-label="Go back" class="flex items-center justify-center w-12 h-12 -ml-3 text-on-surface-variant hover:bg-surface-container-low rounded-full transition-colors active:scale-95">' +
            '<span class="material-symbols-outlined text-[24px]">arrow_back</span>' +
          '</button>' +
          '<span class="font-label-lg text-label-lg text-on-surface-variant tracking-wider uppercase">' + label + '</span>' +
          (opts.skip
            ? '<button id="vf-skip" class="h-12 px-3 -mr-3 rounded-full text-primary font-label-lg text-label-lg hover:bg-primary/5 active:scale-95 transition-colors">Skip</button>'
            : '<div class="w-12 h-12" aria-hidden="true"></div>') +
        '</div>' +
        '<div class="w-full h-1.5 bg-surface-container-high rounded-full overflow-hidden" role="progressbar" aria-valuemin="1" aria-valuemax="' + (p ? p.total : 1) + '" aria-valuenow="' + (p ? p.step : 1) + '" aria-label="Setup progress">' +
          '<div id="vf-progress" class="h-full bg-primary rounded-full transition-all duration-500 ease-out" style="width:0%"></div>' +
        '</div>' +
      '</header>';
  }

  /* --- Connectivity ------------------------------------------------------- */
  function offlineBanner() {
    var forced = qp('sim') === 'offline';
    var el = doc.getElementById('vf-offline');
    if (!el) {
      el = doc.createElement('div');
      el.id = 'vf-offline';
      el.className = 'fixed top-0 left-0 right-0 z-[60] bg-surface-container-highest/95 backdrop-blur-md border-b border-outline-variant/20 py-3 px-margin-mobile flex items-center gap-3';
      el.setAttribute('role', 'status');
      el.innerHTML = '<span class="material-symbols-outlined text-outline">cloud_off</span>' +
        '<p class="font-label-lg text-label-lg text-on-surface-variant">You’re offline. Setup keeps working — everything saves on this device.</p>';
      el.hidden = true;
      doc.body.appendChild(el);
    }
    function sync() {
      var off = forced || navigator.onLine === false;
      el.hidden = !off;
      doc.body.classList.toggle('vf-offline', off);
    }
    global.addEventListener('online', function () { sync(); snackbar('Back online.', 'cloud_done'); });
    global.addEventListener('offline', sync);
    sync();
  }
  /* --- Soft keyboard ------------------------------------------------------ */
  function keyboardWatch() {
    var vv = global.visualViewport;
    if (!vv) return;
    var base = vv.height;
    function check() {
      var shrunk = base - vv.height;
      var open = shrunk > 140;
      doc.body.classList.toggle('kb-open', open);
      if (!open) base = Math.max(base, vv.height);
    }
    vv.addEventListener('resize', check);
    vv.addEventListener('scroll', check);
    // Fallback for browsers without a useful visualViewport delta.
    doc.addEventListener('focusin', function (e) {
      if (e.target.matches('input,textarea,select')) setTimeout(check, 250);
    });
    doc.addEventListener('focusout', function () {
      setTimeout(function () { doc.body.classList.remove('kb-open'); }, 120);
    });
  }

  /* --- CTA gating --------------------------------------------------------- */
  function gate(btn, ok, reason) {
    if (!btn) return;
    btn.disabled = !ok;
    btn.setAttribute('aria-disabled', String(!ok));
    if (!ok && reason) btn.setAttribute('title', reason); else btn.removeAttribute('title');
  }

  /* --- Ring drawing (Phase 1 progress-ring component) --------------------- */
  function drawRing(circle, fraction) {
    if (!circle) return;
    var r = Number(circle.getAttribute('r'));
    var c = 2 * Math.PI * r;
    circle.setAttribute('stroke-dasharray', c.toFixed(1));
    circle.setAttribute('stroke-dashoffset', c.toFixed(1));
    requestAnimationFrame(function () {
      circle.setAttribute('stroke-dashoffset', (c * (1 - Math.max(0, Math.min(1, fraction)))).toFixed(1));
    });
  }

  function fmt(n) { return Number(n).toLocaleString('en-US'); }

  /* --- Screen bootstrap --------------------------------------------------- */
  function initScreen(stepId, opts) {
    opts = opts || {};
    if (qp('reset') === '1') VF.store.reset();
    var state = VF.store.get();

    // A finished user never sees onboarding again.
    if (state.completed && !opts.allowCompleted) { global.location.replace(VF.store.HOME); return null; }

    VF.store.enter(stepId);

    try { if (sessionStorage.getItem('vf.dir') === 'back') doc.documentElement.classList.add('vf-back'); } catch (e) {}

    var mount = doc.getElementById('vf-header');
    if (mount) {
      mount.outerHTML = headerHTML(stepId, opts);
      var p = VF.store.progressFor(stepId);
      var bar = doc.getElementById('vf-progress');
      if (bar) requestAnimationFrame(function () { bar.style.width = (qp('return') === 'ready' ? 100 : (p ? p.percent : 0)) + '%'; });
      var back = doc.getElementById('vf-back');
      if (back) back.addEventListener('click', function () { haptics.select(); goBack(stepId); });
      var skip = doc.getElementById('vf-skip');
      if (skip && opts.onSkip) skip.addEventListener('click', function () { haptics.select(); opts.onSkip(); });
    }

    // Hardware / browser back must behave like the header back button.
    global.addEventListener('popstate', function () { goBack(stepId); });

    offlineBanner();
    keyboardWatch();
    if (opts.title) announce(opts.title);
    return state;
  }

  global.VF = global.VF || {};
  global.VF.ui = {
    qp: qp, haptics: haptics, announce: announce, snackbar: snackbar,
    go: go, advance: advance, goBack: goBack, gate: gate,
    drawRing: drawRing, fmt: fmt, initScreen: initScreen, headerHTML: headerHTML
  };
})(window);
