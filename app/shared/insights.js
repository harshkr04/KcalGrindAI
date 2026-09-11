/* ==========================================================================
   VF.insights — nutrition analytics, trends, and progress calculations.
   Reuses VF.diary and VF.store. No external dependencies.
   ========================================================================== */
(function (global) {
  'use strict';

  function today() {
    var d = new Date();
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
  }

  function dateOffset(base, days) {
    var d = new Date(base + 'T00:00:00');
    d.setDate(d.getDate() + days);
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
  }

  function getDatesInRange(rangeDays) {
    var end = today();
    var start = dateOffset(end, -(rangeDays - 1));
    var dates = [];
    var cur = start;
    while (cur <= end) {
      dates.push(cur);
      cur = dateOffset(cur, 1);
    }
    return dates;
  }

  function getDailyTotalsForRange(rangeDays) {
    var dates = getDatesInRange(rangeDays);
    return dates.map(function (date) {
      var totals = VF.diary.getDailyTotals(date);
      var meals = VF.diary.getMealsByDate(date);
      return {
        date: date,
        calories: totals.calories,
        protein: totals.protein,
        carbs: totals.carbs,
        fat: totals.fat,
        fiber: totals.fiber,
        meals: meals.length,
        tracked: meals.length > 0
      };
    });
  }

  function getAverageCalories(rangeDays) {
    var days = getDailyTotalsForRange(rangeDays);
    var tracked = days.filter(function (d) { return d.tracked; });
    if (!tracked.length) return 0;
    var sum = tracked.reduce(function (s, d) { return s + d.calories; }, 0);
    return Math.round(sum / tracked.length);
  }

  function getAverageMacros(rangeDays) {
    var days = getDailyTotalsForRange(rangeDays);
    var tracked = days.filter(function (d) { return d.tracked; });
    if (!tracked.length) return { protein: 0, carbs: 0, fat: 0 };
    var sumProt = tracked.reduce(function (s, d) { return s + d.protein; }, 0);
    var sumCarbs = tracked.reduce(function (s, d) { return s + d.carbs; }, 0);
    var sumFat = tracked.reduce(function (s, d) { return s + d.fat; }, 0);
    return {
      protein: Math.round(sumProt / tracked.length * 10) / 10,
      carbs: Math.round(sumCarbs / tracked.length * 10) / 10,
      fat: Math.round(sumFat / tracked.length * 10) / 10
    };
  }

  function getTrackingConsistency(rangeDays) {
    var days = getDailyTotalsForRange(rangeDays);
    var tracked = days.filter(function (d) { return d.tracked; }).length;
    return { tracked: tracked, total: days.length, streak: calculateStreak() };
  }

  function calculateStreak() {
    var streak = 0;
    var d = today();
    while (true) {
      var meals = VF.diary.getMealsByDate(d);
      if (meals.length > 0) { streak++; d = dateOffset(d, -1); }
      else break;
    }
    return streak;
  }

  function getTargetAdherence(rangeDays) {
    var targets = VF.store.targets();
    if (!targets) return null;
    var days = getDailyTotalsForRange(rangeDays);
    var withinTarget = days.filter(function (d) { return d.tracked && d.calories <= targets.calories; }).length;
    var tracked = days.filter(function (d) { return d.tracked; }).length;
    return { withinTarget: withinTarget, tracked: tracked, target: targets.calories };
  }

  function getMealPatterns(rangeDays) {
    var dates = getDatesInRange(rangeDays);
    var counts = { breakfast: 0, lunch: 0, dinner: 0, snack: 0 };
    dates.forEach(function (date) {
      var meals = VF.diary.getMealsByDate(date);
      meals.forEach(function (m) { if (counts[m.mealType] !== undefined) counts[m.mealType]++; });
    });
    return counts;
  }

  function getNutritionGaps(rangeDays) {
    var targets = VF.store.targets();
    if (!targets) return [];
    var days = getDailyTotalsForRange(rangeDays);
    var tracked = days.filter(function (d) { return d.tracked; });
    if (!tracked.length) return [];
    var avgProt = tracked.reduce(function (s, d) { return s + d.protein; }, 0) / tracked.length;
    var avgCarbs = tracked.reduce(function (s, d) { return s + d.carbs; }, 0) / tracked.length;
    var avgFat = tracked.reduce(function (s, d) { return s + d.fat; }, 0) / tracked.length;
    var gaps = [];
    if (avgProt < targets.protein * 0.8) gaps.push({ nutrient: 'Protein', avg: Math.round(avgProt), target: targets.protein, status: 'below' });
    if (avgCarbs > targets.carbs * 1.2) gaps.push({ nutrient: 'Carbs', avg: Math.round(avgCarbs), target: targets.carbs, status: 'above' });
    if (avgFat > targets.fat * 1.2) gaps.push({ nutrient: 'Fat', avg: Math.round(avgFat), target: targets.fat, status: 'above' });
    return gaps;
  }

  function getWeightProgress(rangeDays) {
    var range = VF.diary.getWeightRange(dateOffset(today(), -(rangeDays - 1)), today());
    if (range.length < 2) return null;
    var start = range[0];
    var end = range[range.length - 1];
    var change = end.weightKg - start.weightKg;
    return {
      startWeight: start.weightKg,
      currentWeight: end.weightKg,
      change: Math.round(change * 10) / 10,
      entries: range
    };
  }

  function generateInsight(rangeDays) {
    rangeDays = rangeDays || 7;
    var gaps = getNutritionGaps(rangeDays);
    var adherence = getTargetAdherence(rangeDays);
    var consistency = getTrackingConsistency(rangeDays);
    var insight = null;

    if (gaps.length > 0) {
      var gap = gaps[0];
      insight = {
        type: 'gap',
        title: 'Nutrition Gap',
        text: 'Your logged ' + gap.nutrient.toLowerCase() + ' has been ' + gap.status + ' target on most tracked days. Consider adjusting your next meal.'
      };
    } else if (adherence && adherence.withinTarget < adherence.tracked * 0.5) {
      insight = {
        type: 'adherence',
        title: 'Target Adherence',
        text: 'You\'ve been within your calorie target on ' + adherence.withinTarget + ' of ' + adherence.tracked + ' tracked days. Small adjustments can help.'
      };
    } else if (consistency.streak >= 3) {
      insight = {
        type: 'streak',
        title: 'Tracking Streak',
        text: 'You\'ve logged meals for ' + consistency.streak + ' days in a row. Keep it up!'
      };
    } else if (consistency.tracked < 3) {
      insight = {
        type: 'encouragement',
        title: 'Getting Started',
        text: 'Track a few more meals and I\'ll start showing your nutrition patterns here.'
      };
    }
    return insight;
  }

  function buildAnalyticsContext(rangeDays) {
    rangeDays = rangeDays || 7;
    var totals = getDailyTotalsForRange(rangeDays);
    var tracked = totals.filter(function (d) { return d.tracked; });
    var avgCal = getAverageCalories(rangeDays);
    var avgMacros = getAverageMacros(rangeDays);
    var targets = VF.store.targets();
    var adherence = getTargetAdherence(rangeDays);
    var consistency = getTrackingConsistency(rangeDays);
    var patterns = getMealPatterns(rangeDays);
    var gaps = getNutritionGaps(rangeDays);
    var weight = getWeightProgress(rangeDays);

    return {
      range: rangeDays,
      period: rangeDays + ' days',
      avgCalories: avgCal,
      avgProtein: avgMacros.protein,
      avgCarbs: avgMacros.carbs,
      avgFat: avgMacros.fat,
      targetCalories: targets ? targets.calories : null,
      targetProtein: targets ? targets.protein : null,
      targetCarbs: targets ? targets.carbs : null,
      targetFat: targets ? targets.fat : null,
      adherence: adherence,
      tracking: consistency,
      mealPatterns: patterns,
      gaps: gaps,
      weight: weight,
      daily: totals
    };
  }

  global.VF = global.VF || {};
  global.VF.insights = {
    getDailyTotalsForRange: getDailyTotalsForRange,
    getAverageCalories: getAverageCalories,
    getAverageMacros: getAverageMacros,
    getTrackingConsistency: getTrackingConsistency,
    getTargetAdherence: getTargetAdherence,
    getMealPatterns: getMealPatterns,
    getNutritionGaps: getNutritionGaps,
    getWeightProgress: getWeightProgress,
    generateInsight: generateInsight,
    buildAnalyticsContext: buildAnalyticsContext
  };
})(window);
