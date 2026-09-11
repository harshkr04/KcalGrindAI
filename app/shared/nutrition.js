/* ==========================================================================
   VF.nutrition — unit handling, validation and target calculation.
   ========================================================================== */
(function (global) {
  'use strict';

  var GOALS = {
    lose:      { label: 'Lose weight',    icon: 'monitor_weight',  blurb: 'Steady, sustainable fat loss.',        delta: -450, proteinPerKg: 1.8 },
    maintain:  { label: 'Maintain weight',icon: 'favorite',        blurb: 'Hold steady and eat consistently.',     delta: 0,    proteinPerKg: 1.6 },
    gain:      { label: 'Gain weight',    icon: 'trending_up',     blurb: 'Add weight at a comfortable pace.',     delta: 350,  proteinPerKg: 1.7 },
    muscle:    { label: 'Build muscle',   icon: 'fitness_center',  blurb: 'Fuel training and lean gains.',         delta: 250,  proteinPerKg: 2.0 },
    healthier: { label: 'Eat healthier',  icon: 'energy_savings_leaf', blurb: 'Better quality, no calorie chase.', delta: 0,    proteinPerKg: 1.5 },
    nutrition: { label: 'Track nutrition',icon: 'insights',        blurb: 'Just see what you eat, clearly.',       delta: 0,    proteinPerKg: 1.6 }
  };

  var ACTIVITY = {
    sedentary: { label: 'Sedentary',        blurb: 'Little exercise',                  icon: 'chair',           factor: 1.20 },
    light:     { label: 'Lightly active',   blurb: 'Light exercise 1–3 days/week',     icon: 'directions_walk', factor: 1.375 },
    moderate:  { label: 'Moderately active',blurb: 'Exercise 3–5 days/week',           icon: 'fitness_center',  factor: 1.55 },
    very:      { label: 'Very active',      blurb: 'Hard exercise 6–7 days/week',      icon: 'directions_run',  factor: 1.725 },
    extreme:   { label: 'Extremely active', blurb: 'Very intense physical activity',   icon: 'sprint',          factor: 1.90 }
  };

  var DIETS = [
    { id: 'none',          label: 'No preference', exclusive: true },
    { id: 'vegetarian',    label: 'Vegetarian' },
    { id: 'vegan',         label: 'Vegan' },
    { id: 'high_protein',  label: 'High protein' },
    { id: 'low_carb',      label: 'Low carb' },
    { id: 'balanced',      label: 'Balanced' },
    { id: 'mediterranean', label: 'Mediterranean' },
    { id: 'keto',          label: 'Keto' },
    { id: 'pescatarian',   label: 'Pescatarian' }
  ];

  var ALLERGENS = [
    { id: 'peanuts',   label: 'Peanuts',   icon: 'nutrition' },
    { id: 'tree_nuts', label: 'Tree nuts', icon: 'park' },
    { id: 'milk',      label: 'Milk',      icon: 'water_full' },
    { id: 'eggs',      label: 'Eggs',      icon: 'egg' },
    { id: 'soy',       label: 'Soy',       icon: 'grass' },
    { id: 'wheat',     label: 'Wheat',     icon: 'grain' },
    { id: 'fish',      label: 'Fish',      icon: 'set_meal' },
    { id: 'shellfish', label: 'Shellfish', icon: 'phishing' },
    { id: 'sesame',    label: 'Sesame',    icon: 'spa' }
  ];

  /* --- Units -------------------------------------------------------------- */
  var LIMITS = {
    age:      { min: 13, max: 100 },
    heightCm: { min: 100, max: 250 },
    weightKg: { min: 30, max: 300 }
  };

  function round1(n) { return Math.round(n * 10) / 10; }
  function cmToFtIn(cm) { var t = cm / 2.54; var ft = Math.floor(t / 12); return { ft: ft, inch: Math.round(t - ft * 12) }; }
  function ftInToCm(ft, inch) { return round1(((Number(ft) || 0) * 12 + (Number(inch) || 0)) * 2.54); }
  function kgToLb(kg) { return Math.round(kg * 2.2046226); }
  function lbToKg(lb) { return round1(Number(lb) / 2.2046226); }

  /* --- Validation --------------------------------------------------------- */
  function validateAge(v) {
    if (v === '' || v === null || v === undefined) return { state: 'empty', message: 'Age is needed to estimate your energy use.' };
    var n = Number(v);
    if (!isFinite(n) || String(v).indexOf('.') > -1) return { state: 'invalid', message: 'Enter your age in whole years.' };
    if (n < LIMITS.age.min) return { state: 'invalid', message: 'Vitality is for ages ' + LIMITS.age.min + ' and up.' };
    if (n > LIMITS.age.max) return { state: 'invalid', message: 'Enter an age of ' + LIMITS.age.max + ' or below.' };
    var boundary = n === LIMITS.age.min || n >= 90;
    return { state: 'valid', boundary: boundary, value: Math.round(n) };
  }

  function validateHeightCm(cm, units) {
    if (cm === '' || cm === null || cm === undefined || isNaN(Number(cm))) {
      return { state: 'empty', message: 'Height helps size your calorie target.' };
    }
    var n = Number(cm);
    if (n < LIMITS.heightCm.min || n > LIMITS.heightCm.max) {
      var msg = units === 'imperial'
        ? 'Enter a height between 3′3″ and 8′2″.'
        : 'Enter a height between ' + LIMITS.heightCm.min + ' and ' + LIMITS.heightCm.max + ' cm.';
      return { state: 'invalid', message: msg };
    }
    return { state: 'valid', boundary: n <= 130 || n >= 220, value: round1(n) };
  }

  function validateWeightKg(kg, units, label) {
    label = label || 'Weight';
    if (kg === '' || kg === null || kg === undefined || isNaN(Number(kg))) {
      return { state: 'empty', message: label + ' is needed to personalise your plan.' };
    }
    var n = Number(kg);
    if (n < LIMITS.weightKg.min || n > LIMITS.weightKg.max) {
      var msg = units === 'imperial'
        ? 'Enter a weight between ' + kgToLb(LIMITS.weightKg.min) + ' and ' + kgToLb(LIMITS.weightKg.max) + ' lb.'
        : 'Enter a weight between ' + LIMITS.weightKg.min + ' and ' + LIMITS.weightKg.max + ' kg.';
      return { state: 'invalid', message: msg };
    }
    return { state: 'valid', boundary: n <= 40 || n >= 250, value: round1(n) };
  }

  /* Non-blocking sanity note between goal and goal weight. Deliberately a
     note, never a gate: the flow must not lecture the user. */
  function goalWeightNote(goal, weightKg, goalWeightKg) {
    if (!goal || !weightKg || !goalWeightKg) return null;
    var delta = round1(goalWeightKg - weightKg);
    if (goal === 'lose' && delta >= 0) return 'Your goal weight isn’t lower than your current weight. We’ll aim to maintain instead — you can change either value.';
    if ((goal === 'gain' || goal === 'muscle') && delta <= -0.5) return 'Your goal weight is lower than your current weight. We’ll plan a gentle deficit — adjust if that isn’t what you meant.';
    if (Math.abs(delta) > weightKg * 0.25) return 'That’s a big change. We’ll pace it gradually and you can revisit it any time.';
    return null;
  }
  /* --- Target calculation -------------------------------------------------
     Mifflin–St Jeor, using the midpoint of the male (+5) and female (−161)
     constants (−78) because onboarding deliberately does not ask for sex —
     four questions, not a medical intake form. The result is an estimate and
     is presented as one. ---------------------------------------------------- */
  function bmr(weightKg, heightCm, age) {
    return 10 * weightKg + 6.25 * heightCm - 5 * age - 78;
  }

  var CAL_FLOOR = 1200;

  function macroSplit(calories, weightKg, goal, diets) {
    diets = diets || [];
    var g = GOALS[goal] || GOALS.maintain;
    var perKg = g.proteinPerKg;
    if (diets.indexOf('high_protein') > -1) perKg += 0.2;
    if (diets.indexOf('vegan') > -1 || diets.indexOf('vegetarian') > -1) perKg = Math.min(perKg, 1.8);

    var protein = Math.round(weightKg * perKg);
    var proteinKcal = protein * 4;
    // Protein never eats more than 40% of the day.
    if (proteinKcal > calories * 0.4) { protein = Math.round((calories * 0.4) / 4); proteinKcal = protein * 4; }

    var fatPct = 0.28;
    if (diets.indexOf('keto') > -1) fatPct = 0.70;
    else if (diets.indexOf('low_carb') > -1) fatPct = 0.42;
    else if (diets.indexOf('mediterranean') > -1) fatPct = 0.35;

    var fat = Math.round((calories * fatPct) / 9);
    var carbs = Math.round((calories - proteinKcal - fat * 9) / 4);
    if (carbs < 20) { // keto / very low carb: push the remainder into fat
      carbs = 20;
      fat = Math.round((calories - proteinKcal - carbs * 4) / 9);
    }
    return { protein: protein, carbs: carbs, fat: Math.max(fat, 20) };
  }

  function waterFor(weightKg, activity) {
    var factor = { sedentary: 0.030, light: 0.032, moderate: 0.034, very: 0.037, extreme: 0.040 }[activity] || 0.033;
    var litres = Math.max(1.8, Math.min(4.0, round1(weightKg * factor)));
    return { waterL: litres, glasses: Math.max(6, Math.round((litres * 1000) / 300)) }; // 300 ml glass
  }

  function calculate(profile) {
    var w = Number(profile.weightKg), h = Number(profile.heightCm), a = Number(profile.age);
    if (!w || !h || !a) throw new Error('missing_profile');

    var base = bmr(w, h, a);
    var factor = (ACTIVITY[profile.activity] || ACTIVITY.moderate).factor;
    var tdee = base * factor;

    var goal = GOALS[profile.goal] ? profile.goal : 'maintain';
    var delta = GOALS[goal].delta;
    // Respect the numbers the user actually typed: if goal weight contradicts
    // the chosen goal, fall back to maintenance rather than pushing them.
    if (goal === 'lose' && profile.goalWeightKg && profile.goalWeightKg >= w) delta = 0;
    if ((goal === 'gain' || goal === 'muscle') && profile.goalWeightKg && profile.goalWeightKg < w - 0.5) delta = -300;
    if (goal === 'lose' && profile.goalWeightKg && Math.abs(w - profile.goalWeightKg) < 2) delta = -250;

    var calories = Math.round((tdee + delta) / 10) * 10;
    var floored = false;
    var floor = Math.max(CAL_FLOOR, Math.round(base * 0.95 / 10) * 10);
    if (calories < floor) { calories = floor; floored = true; }

    var macros = macroSplit(calories, w, goal, profile.diet);
    var water = waterFor(w, profile.activity);

    return {
      calories: calories,
      protein: macros.protein,
      carbs: macros.carbs,
      fat: macros.fat,
      waterL: water.waterL,
      glasses: water.glasses,
      bmr: Math.round(base),
      tdee: Math.round(tdee),
      delta: delta,
      floored: floored,
      goal: goal,
      basis: 'recommended',
      calculatedAt: new Date().toISOString()
    };
  }

  /* A conservative plan used only if calculation genuinely cannot run
     (e.g. the profile was lost). Never silently replaces a real result. */
  function fallback(profile) {
    var w = Number(profile && profile.weightKg) || 70;
    var calories = 2000;
    var macros = macroSplit(calories, w, profile && profile.goal, profile && profile.diet);
    var water = waterFor(w, profile && profile.activity);
    return {
      calories: calories, protein: macros.protein, carbs: macros.carbs, fat: macros.fat,
      waterL: water.waterL, glasses: water.glasses, bmr: null, tdee: null, delta: 0,
      floored: false, goal: (profile && profile.goal) || 'maintain', basis: 'fallback',
      calculatedAt: new Date().toISOString()
    };
  }

  function macroCalories(t) { return (t.protein * 4) + (t.carbs * 4) + (t.fat * 9); }

  /* Macro/calorie reconciliation for the Customize screen. */
  function checkMacros(t) {
    var sum = macroCalories(t);
    var diff = sum - t.calories;
    var tolerance = Math.max(40, Math.round(t.calories * 0.03));
    return {
      sum: sum,
      diff: diff,
      withinTolerance: Math.abs(diff) <= tolerance,
      tolerance: tolerance,
      proteinPct: Math.round((t.protein * 4 / sum) * 100) || 0,
      carbsPct: Math.round((t.carbs * 4 / sum) * 100) || 0,
      fatPct: Math.round((t.fat * 9 / sum) * 100) || 0
    };
  }

function explain(t, goalId) {
    var g = GOALS[goalId] || GOALS.maintain;
    if (t.basis === 'fallback') return 'This is a safe starting plan. Add your details any time and we\'ll refine it.';
    if (t.floored) return 'We kept your target at a healthy minimum rather than going lower, while still moving toward ' + g.label.toLowerCase() + '.';
    if (t.delta < 0) return 'Your target sits about ' + Math.abs(t.delta) + ' kcal under your estimated daily burn — designed for gradual, steady progress.';
    if (t.delta > 0) return 'Your target sits about ' + t.delta + ' kcal above your estimated daily burn — enough to build without unnecessary excess.';
    return 'Your target matches your estimated daily burn, which keeps things stable while you get into a rhythm.';
  }

  /* --- Food/Nutrition Scaling ---------------------------------------------- */
  var UNIT_CONVERSIONS = {
    g: { toGrams: 1, label: 'g' },
    kg: { toGrams: 1000, label: 'kg' },
    oz: { toGrams: 28.3495, label: 'oz' },
    lb: { toGrams: 453.592, label: 'lb' },
    cup: { toGrams: 240, label: 'cup' },
    tbsp: { toGrams: 15, label: 'tbsp' },
    tsp: { toGrams: 5, label: 'tsp' },
    piece: { toGrams: 100, label: 'piece' },
    serving: { toGrams: 100, label: 'serving' },
    slice: { toGrams: 30, label: 'slice' },
    whole: { toGrams: 150, label: 'whole' },
    handful: { toGrams: 30, label: 'handful' },
    bowl: { toGrams: 250, label: 'bowl' },
    plate: { toGrams: 300, label: 'plate' }
  };

  function parsePortion(portionStr) {
    if (!portionStr) return { quantity: 100, unit: 'g', grams: 100 };
    var match = portionStr.trim().match(/^([\d.]+)\s*(\w+)$/i);
    if (!match) return { quantity: 100, unit: 'g', grams: 100 };
    var quantity = parseFloat(match[1]);
    var unit = match[2].toLowerCase();
    var conversion = UNIT_CONVERSIONS[unit] || UNIT_CONVERSIONS.g;
    var grams = Math.round(quantity * conversion.toGrams);
    return { quantity: quantity, unit: unit, grams: grams };
  }

  function scaleNutrition(food, newGrams) {
    var original = parsePortion(food.portion);
    var originalGrams = original.grams || 100;
    var factor = newGrams / originalGrams;
    return {
      calories: Math.round(food.calories * factor),
      macros: {
        protein: Math.round(food.macros.protein * factor * 10) / 10,
        carbs: Math.round(food.macros.carbs * factor * 10) / 10,
        fat: Math.round(food.macros.fat * factor * 10) / 10
      }
    };
  }

  function formatPortion(grams, preferredUnit) {
    if (!preferredUnit || !UNIT_CONVERSIONS[preferredUnit]) {
      if (grams >= 1000) return (grams / 1000).toFixed(1) + ' kg';
      return grams + ' g';
    }
    var conversion = UNIT_CONVERSIONS[preferredUnit];
    var quantity = grams / conversion.toGrams;
    if (quantity >= 100) return Math.round(quantity) + ' ' + conversion.label;
    if (quantity >= 10) return quantity.toFixed(1) + ' ' + conversion.label;
    return quantity.toFixed(2) + ' ' + conversion.label;
  }

  function getCommonUnits(foodName) {
    var name = (foodName || '').toLowerCase();
    if (name.includes('rice') || name.includes('quinoa') || name.includes('oats') || name.includes('pasta') || name.includes('cereal')) {
      return ['g', 'cup', 'bowl', 'serving'];
    }
    if (name.includes('chicken') || name.includes('beef') || name.includes('fish') || name.includes('salmon') || name.includes('meat') || name.includes('tofu') || name.includes('tempeh')) {
      return ['g', 'oz', 'piece', 'serving'];
    }
    if (name.includes('egg')) {
      return ['piece', 'g', 'serving'];
    }
    if (name.includes('oil') || name.includes('butter') || name.includes('sauce') || name.includes('dressing')) {
      return ['g', 'tbsp', 'tsp', 'ml'];
    }
    if (name.includes('nut') || name.includes('seed') || name.includes('almond') || name.includes('walnut')) {
      return ['g', 'oz', 'handful', 'serving'];
    }
    if (name.includes('cheese') || name.includes('yogurt') || name.includes('milk')) {
      return ['g', 'cup', 'serving', 'ml'];
    }
    if (name.includes('bread') || name.includes('toast') || name.includes('slice')) {
      return ['slice', 'g', 'piece', 'serving'];
    }
    if (name.includes('fruit') || name.includes('apple') || name.includes('banana') || name.includes('berry') || name.includes('avocado')) {
      return ['piece', 'g', 'whole', 'serving'];
    }
    if (name.includes('vegetable') || name.includes('broccoli') || name.includes('spinach') || name.includes('carrot') || name.includes('salad')) {
      return ['g', 'cup', 'bowl', 'serving', 'handful'];
    }
    return ['g', 'oz', 'cup', 'piece', 'serving', 'tbsp', 'tsp'];
  }

  function searchFoods(query, foodsDb) {
    var q = (query || '').toLowerCase().trim();
    if (!q) return [];
    return (foodsDb || []).filter(function (f) {
      return f.name.toLowerCase().includes(q) ||
        (f.category && f.category.toLowerCase().includes(q)) ||
        (f.tags && f.tags.some(function (t) { return t.toLowerCase().includes(q); }));
    }).slice(0, 20);
  }

  global.VF = global.VF || {};
  global.VF.nutrition = {
    GOALS: GOALS, ACTIVITY: ACTIVITY, DIETS: DIETS, ALLERGENS: ALLERGENS, LIMITS: LIMITS,
    round1: round1, cmToFtIn: cmToFtIn, ftInToCm: ftInToCm, kgToLb: kgToLb, lbToKg: lbToKg,
    validateAge: validateAge, validateHeightCm: validateHeightCm, validateWeightKg: validateWeightKg,
    goalWeightNote: goalWeightNote,
    bmr: bmr, calculate: calculate, fallback: fallback, macroSplit: macroSplit,
    macroCalories: macroCalories, checkMacros: checkMacros, explain: explain,
    parsePortion: parsePortion, scaleNutrition: scaleNutrition, formatPortion: formatPortion,
    getCommonUnits: getCommonUnits, searchFoods: searchFoods, UNIT_CONVERSIONS: UNIT_CONVERSIONS
  };
})(window);
