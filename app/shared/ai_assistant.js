/* ==========================================================================
   VF.aiAssistant — AI Nutrition Assistant context and response engine.
   Reuses existing VF.store, VF.diary, and VF.aiFood.
   ========================================================================== */
(function (global) {
  'use strict';

  var HISTORY_KEY = 'vitality.ai_history.v1';
  var MAX_HISTORY = 50;

  function today() {
    var d = new Date();
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
  }

  function buildContext() {
    var user = VF.store.get();
    var targets = VF.store.targets();
    var totals = VF.diary.getDailyTotals();
    var meals = VF.diary.getTodayMeals();
    var recent = VF.diary.getRecentFoods(10);
    var remaining = targets ? Math.max(0, targets.calories - totals.calories) : null;
    var remainingProt = targets ? Math.max(0, targets.protein - totals.protein) : null;
    var remainingCarbs = targets ? Math.max(0, targets.carbs - totals.carbs) : null;
    var remainingFat = targets ? Math.max(0, targets.fat - totals.fat) : null;

    return {
      user: {
        goal: user.goal,
        activity: user.activity,
        diet: user.diet || [],
        allergies: user.allergies || []
      },
      targets: targets ? {
        calories: targets.calories,
        protein: targets.protein,
        carbs: targets.carbs,
        fat: targets.fat,
        glasses: targets.glasses
      } : null,
      today: {
        date: today(),
        calories: totals.calories,
        protein: totals.protein,
        carbs: totals.carbs,
        fat: totals.fat,
        fiber: totals.fiber,
        remaining: remaining,
        remainingProtein: remainingProt,
        remainingCarbs: remainingCarbs,
        remainingFat: remainingFat,
        meals: meals.map(function (m) {
          return {
            id: m.id,
            type: m.mealType,
            calories: m.totalCalories,
            items: (m.items || []).map(function (it) {
              return { name: it.name, serving: it.serving, calories: it.calories, protein: it.protein, carbs: it.carbs, fat: it.fat };
            })
          };
        })
      },
      recent: recent.slice(0, 5)
    };
  }

  function buildAnalyticsContext(rangeDays) {
    rangeDays = rangeDays || 7;
    var analytics = VF.insights.buildAnalyticsContext(rangeDays);
    return analytics;
  }

  function isOffline() {
    return global.navigator && global.navigator.onLine === false;
  }

  function filterAllergies(items, allergies) {
    if (!allergies || !allergies.length) return items;
    var lower = allergies.map(function (a) { return a.toLowerCase(); });
    return items.filter(function (item) {
      var name = (item.name || '').toLowerCase();
      return !lower.some(function (a) { return name.indexOf(a) > -1; });
    });
  }

  function suggestMeal(remainingCal, remainingProt, goal, diet, allergies) {
    var db = VF.diary.FOOD_DB.slice();
    if (diet && diet.indexOf('vegetarian') > -1) db = db.filter(function (f) { return f.id !== 'burger' && f.id !== 'chicken_breast' && f.id !== 'salmon' && f.id !== 'steak'; });
    if (diet && diet.indexOf('vegan') > -1) db = db.filter(function (f) { return f.id !== 'milk' && f.id !== 'yogurt' && f.id !== 'cheese' && f.id !== 'burger' && f.id !== 'chicken_breast' && f.id !== 'salmon' && f.id !== 'steak' && f.id !== 'egg'; });
    if (diet && diet.indexOf('high protein') > -1) db = db.sort(function (a, b) { return (b.protein / (b.calories || 1)) - (a.protein / (a.calories || 1)); });
    var candidates = db.filter(function (f) { return f.calories <= remainingCal; });
    if (allergies && allergies.length) candidates = filterAllergies(candidates, allergies);
    if (!candidates.length) return null;
    var pick = candidates[0];
    var mult = Math.min(2, Math.max(1, Math.round(remainingCal / pick.calories)));
    var nut = VF.aiFood.nutritionFromFood(pick, mult);
    return {
      title: pick.name + (mult > 1 ? ' (' + mult + 'x)' : ''),
      calories: nut.calories,
      protein: nut.protein,
      carbs: nut.carbs,
      fat: nut.fat,
      ingredients: [pick.name + ' — ' + (mult * (pick.servingGrams || 100)) + 'g'],
      action: 'log'
    };
  }

  function generateResponse(message) {
    var ctx = buildContext();
    var lower = message.toLowerCase().trim();

    if (isOffline()) {
      return { type: 'text', text: 'I\'m offline right now, so I can\'t reach the AI service. You can still search foods, view your diary, and log meals locally.' };
    }

    if (/how am i doing|daily summary|today's nutrition|analyze my day|how was my week|how consistent|why was my protein|what changed/.test(lower)) {
      var range = /week/.test(lower) ? 7 : /month/.test(lower) ? 30 : 7;
      var analytics = buildAnalyticsContext(range);
      var lines = [analytics.period, 'Avg calories: ' + VF.ui.fmt(analytics.avgCalories) + ' / ' + VF.ui.fmt(analytics.targetCalories) + ' kcal', 'Avg protein: ' + analytics.avgProtein + ' / ' + analytics.targetProtein + ' g', 'Avg carbs: ' + analytics.avgCarbs + ' / ' + analytics.targetCarbs + ' g', 'Avg fat: ' + analytics.avgFat + ' / ' + analytics.targetFat + ' g', 'Tracked: ' + analytics.tracking.tracked + ' of ' + analytics.tracking.total + ' days'];
      var insight = '';
      if (analytics.gaps && analytics.gaps.length > 0) {
        insight = 'Your logged ' + analytics.gaps[0].nutrient.toLowerCase() + ' has been ' + analytics.gaps[0].status + ' target on most tracked days.';
      } else if (analytics.adherence && analytics.adherence.withinTarget < analytics.adherence.tracked * 0.5) {
        insight = 'You\'ve been within your calorie target on ' + analytics.adherence.withinTarget + ' of ' + analytics.adherence.tracked + ' tracked days. Small adjustments can help.';
      } else {
        insight = 'You\'re making consistent progress. Keep logging to see more patterns.';
      }
      return { type: 'summary', title: analytics.period + ' Summary', lines: lines, insight: insight };
    }

    if (/how am i doing today|today's nutrition|analyze my day/.test(lower)) {
      var t = ctx.today;
      var lines = ['Today', 'Calories: ' + VF.ui.fmt(t.calories) + ' / ' + VF.ui.fmt(ctx.targets.calories) + ' kcal', 'Protein: ' + t.protein.toFixed(1) + ' / ' + ctx.targets.protein + ' g', 'Carbs: ' + t.carbs.toFixed(1) + ' / ' + ctx.targets.carbs + ' g', 'Fat: ' + t.fat.toFixed(1) + ' / ' + ctx.targets.fat + ' g'];
      var insight = '';
      if (t.remainingProtein <= 0) insight = 'You\'ve hit your protein target. Great job!';
      else if (t.remainingProtein < 30) insight = 'You\'re close to your protein target.';
      else insight = 'You\'re still short on protein. Consider a protein-rich meal next.';
      return { type: 'summary', title: 'Today\'s Nutrition', lines: lines, insight: insight };
    }

    if (/calories left|how many calories|remaining calories|calories remaining/.test(lower)) {
      var rem = ctx.today.remaining;
      var text = 'You have about ' + VF.ui.fmt(rem) + ' kcal remaining today.';
      if (ctx.today.remainingProtein > 0) text += ' You still need ' + ctx.today.remainingProtein.toFixed(0) + ' g of protein.';
      return { type: 'text', text: text };
    }

    if (/protein|how much protein|enough protein/.test(lower)) {
      var prot = ctx.today.protein;
      var target = ctx.targets.protein;
      var text = 'You\'ve logged ' + prot.toFixed(1) + ' g of protein out of ' + target + ' g.';
      if (prot >= target) text += ' You\'ve met your protein target!';
      else if (prot >= target * 0.8) text += ' You\'re close to your target.';
      else text += ' Consider adding a protein-rich food to your next meal.';
      return { type: 'text', text: text };
    }

    if (/suggest|recommend|what should i eat|what should i have|dinner|lunch|breakfast/.test(lower)) {
      var mealType = 'snack';
      if (/breakfast/.test(lower)) mealType = 'breakfast';
      else if (/lunch/.test(lower)) mealType = 'lunch';
      else if (/dinner/.test(lower)) mealType = 'dinner';
      var remCal = ctx.today.remaining;
      var meal = suggestMeal(remCal, ctx.today.remainingProtein, ctx.user.goal, ctx.user.diet, ctx.user.allergies);
      if (!meal) return { type: 'text', text: 'I couldn\'t find a suitable meal within your remaining calories. Try adjusting your targets or logging a custom food.' };
      return { type: 'recommendation', title: 'Suggested ' + mealType.charAt(0).toUpperCase() + mealType.slice(1), meal: meal };
    }

    if (/under \d+|less than \d+|low calorie|healthy/.test(lower)) {
      var calMatch = lower.match(/(\d+)\s*(kcal|cal)?/);
      var limit = calMatch ? parseInt(calMatch[1], 10) : 600;
      var meal = suggestMeal(limit, ctx.today.remainingProtein, ctx.user.goal, ctx.user.diet, ctx.user.allergies);
      if (!meal) return { type: 'text', text: 'I couldn\'t find a meal under ' + limit + ' kcal that fits your preferences.' };
      return { type: 'recommendation', title: 'Meal under ' + limit + ' kcal', meal: meal };
    }

    if (/log|i had|i ate|add/.test(lower)) {
      var extraction = VF.aiFood.estimateNutrition(message);
      if (!extraction.items.length) return { type: 'text', text: 'I couldn\'t identify any foods in that. Could you try describing it differently?' };
      var filtered = filterAllergies(extraction.items, ctx.user.allergies);
      if (filtered.length !== extraction.items.length) {
        return { type: 'text', text: 'Some items you mentioned may contain allergens. Please review before logging.' };
      }
      return { type: 'food_log', items: filtered, total: extraction.totalCalories, text: 'I found ' + extraction.items.map(function (it) { return it.name; }).join(', ') + '. Estimated total: ' + VF.ui.fmt(extraction.totalCalories) + ' kcal.' };
    }

    if (/vegetarian|vegan|high protein|low carb|keto|mediterranean/.test(lower)) {
      var dietType = ctx.user.diet && ctx.user.diet.length ? ctx.user.diet.join(', ') : 'no specific';
      return { type: 'text', text: 'Your current diet preference is set to ' + dietType + '. I\'ll use that when suggesting meals. Would you like a recommendation now?' };
    }

    if (/allerg|allergy|allergic/.test(lower)) {
      var allergies = ctx.user.allergies && ctx.user.allergies.length ? ctx.user.allergies.join(', ') : 'none';
      return { type: 'text', text: 'Your stored allergies are: ' + allergies + '. I\'ll make sure recommendations avoid these.' };
    }

    return { type: 'text', text: 'I can help with calorie targets, meal suggestions, food logging, and diary analysis. Try asking: "How am I doing today?" or "Suggest dinner under 600 kcal."' };
  }

  function loadHistory() {
    try {
      var raw = global.localStorage.getItem(HISTORY_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch (e) { return []; }
  }

  function saveHistory(history) {
    try { global.localStorage.setItem(HISTORY_KEY, JSON.stringify(history.slice(-MAX_HISTORY))); } catch (e) {}
  }

  function addToHistory(role, text, data) {
    var history = loadHistory();
    history.push({ role: role, text: text, data: data, ts: new Date().toISOString() });
    saveHistory(history);
  }

  function clearHistory() {
    try { global.localStorage.removeItem(HISTORY_KEY); } catch (e) {}
  }

  global.VF = global.VF || {};
  global.VF.aiAssistant = {
    buildContext: buildContext,
    buildAnalyticsContext: buildAnalyticsContext,
    generateResponse: generateResponse,
    loadHistory: loadHistory,
    saveHistory: saveHistory,
    clearHistory: clearHistory,
    addToHistory: addToHistory,
    isOffline: isOffline
  };
})(window);
