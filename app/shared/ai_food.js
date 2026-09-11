/* ==========================================================================
   VF.aiFood — shared AI food extraction for voice, text, and barcode logging.
   ========================================================================== */
(function (global) {
  'use strict';

  var FOODS = [
    { id: 'chicken_sandwich', name: 'Chicken sandwich', brand: 'Generic', serving: '1 sandwich', servingGrams: 250, calories: 420, protein: 28, carbs: 35, fat: 18, keywords: ['chicken sandwich', 'sandwich', 'chicken burger', 'grilled chicken sandwich'] },
    { id: 'coke', name: 'Coke', brand: 'Coca-Cola', serving: '330 ml can', servingGrams: 330, calories: 140, protein: 0, carbs: 39, fat: 0, keywords: ['coke', 'coca cola', 'cola', 'soda'] },
    { id: 'eggs', name: 'Eggs', brand: 'Generic', serving: '2 large', servingGrams: 100, calories: 140, protein: 12, carbs: 1, fat: 10, keywords: ['eggs', 'egg', 'boiled eggs', 'fried eggs', 'scrambled eggs'] },
    { id: 'toast', name: 'Toast', brand: 'Generic', serving: '2 slices', servingGrams: 60, calories: 160, protein: 5, carbs: 30, fat: 2, keywords: ['toast', 'bread', 'sourdough', 'whole wheat toast'] },
    { id: 'banana', name: 'Banana', brand: 'Generic', serving: '1 medium', servingGrams: 118, calories: 105, protein: 1.3, carbs: 27, fat: 0.4, keywords: ['banana', 'bananas'] },
    { id: 'avocado_toast', name: 'Avocado toast', brand: 'Generic', serving: '1 slice', servingGrams: 180, calories: 280, protein: 8, carbs: 26, fat: 18, keywords: ['avocado toast', 'avocado', 'avo toast'] },
    { id: 'grilled_chicken_salad', name: 'Grilled chicken salad', brand: 'Generic', serving: '1 bowl', servingGrams: 350, calories: 350, protein: 35, carbs: 12, fat: 18, keywords: ['grilled chicken salad', 'chicken salad', 'salad', 'garden salad'] },
    { id: 'protein_shake', name: 'Protein shake', brand: 'Generic', serving: '1 shake', servingGrams: 300, calories: 180, protein: 30, carbs: 15, fat: 3, keywords: ['protein shake', 'shake', 'protein drink', 'smoothie'] },
    { id: 'rice_bowl', name: 'Rice bowl', brand: 'Generic', serving: '1 bowl', servingGrams: 300, calories: 380, protein: 8, carbs: 72, fat: 4, keywords: ['rice', 'rice bowl', 'white rice', 'brown rice'] },
    { id: 'pasta', name: 'Pasta', brand: 'Generic', serving: '1 plate', servingGrams: 280, calories: 400, protein: 12, carbs: 70, fat: 8, keywords: ['pasta', 'spaghetti', 'penne', 'macaroni', 'noodles'] },
    { id: 'pizza', name: 'Pizza', brand: 'Generic', serving: '2 slices', servingGrams: 200, calories: 480, protein: 20, carbs: 55, fat: 20, keywords: ['pizza', 'slice', 'pepperoni pizza', 'margherita'] },
    { id: 'burger', name: 'Burger', brand: 'Generic', serving: '1 burger', servingGrams: 250, calories: 550, protein: 28, carbs: 40, fat: 30, keywords: ['burger', 'cheeseburger', 'hamburger', 'beef burger'] },
    { id: 'sushi', name: 'Sushi', brand: 'Generic', serving: '8 pieces', servingGrams: 200, calories: 280, protein: 12, carbs: 45, fat: 4, keywords: ['sushi', 'roll', 'maki', 'sashimi'] },
    { id: 'curry', name: 'Curry', brand: 'Generic', serving: '1 bowl', servingGrams: 350, calories: 420, protein: 18, carbs: 35, fat: 22, keywords: ['curry', 'chicken curry', 'vegetable curry', 'thai curry'] },
    { id: 'yogurt', name: 'Yogurt', brand: 'Generic', serving: '1 cup', servingGrams: 245, calories: 150, protein: 12, carbs: 18, fat: 4, keywords: ['yogurt', 'greek yogurt', 'yoghurt'] },
    { id: 'coffee', name: 'Coffee', brand: 'Generic', serving: '1 cup', servingGrams: 240, calories: 5, protein: 0, carbs: 0, fat: 0, keywords: ['coffee', 'espresso', 'americano', 'black coffee'] },
    { id: 'latte', name: 'Latte', brand: 'Generic', serving: '1 cup', servingGrams: 350, calories: 180, protein: 10, carbs: 15, fat: 8, keywords: ['latte', 'cappuccino', 'flat white', 'coffee with milk'] }
  ];

  var BARCODE_PRODUCTS = {
    '1234567890123': { id: 'chicken_sandwich', name: 'Chicken sandwich', brand: 'FreshEats', serving: '1 sandwich', servingGrams: 250, calories: 420, protein: 28, carbs: 35, fat: 18 },
    '9876543210987': { id: 'coke', name: 'Coke', brand: 'Coca-Cola', serving: '330 ml can', servingGrams: 330, calories: 140, protein: 0, carbs: 39, fat: 0 },
    '1111111111111': { id: 'protein_shake', name: 'Protein shake', brand: 'NutriMax', serving: '1 shake', servingGrams: 300, calories: 180, protein: 30, carbs: 15, fat: 3 }
  };

  function findFoodByKeywords(text) {
    var lower = text.toLowerCase();
    var matches = [];
    FOODS.forEach(function (food) {
      var score = 0;
      food.keywords.forEach(function (kw) {
        if (lower.indexOf(kw) > -1) score++;
      });
      if (score > 0) matches.push({ food: food, score: score });
    });
    matches.sort(function (a, b) { return b.score - a.score; });
    return matches.map(function (m) { return m.food; });
  }

  function findFoodByBarcode(code) {
    return BARCODE_PRODUCTS[code] || null;
  }

  function parseServingMultiplier(text, food) {
    var lower = text.toLowerCase();
    var mult = 1;
    var numMatch = lower.match(/(\d+)\s*(x|times|serving|servings)?/);
    if (numMatch) {
      mult = Math.max(1, parseInt(numMatch[1], 10));
    }
    var portionMatch = lower.match(/(large|medium|small|big|extra|double|single|half|quarter)/);
    if (portionMatch) {
      var p = portionMatch[1];
      if (p === 'double' || p === 'extra' || p === 'large' || p === 'big') mult = Math.max(mult, 2);
      else if (p === 'half') mult = Math.max(mult, 0.5);
      else if (p === 'quarter') mult = Math.max(mult, 0.25);
      else if (p === 'medium') mult = Math.max(mult, 1);
    }
    return mult;
  }

  function estimateNutrition(text) {
    var foods = findFoodByKeywords(text);
    var results = [];
    var totalCal = 0;
    foods.forEach(function (food) {
      var mult = parseServingMultiplier(text, food);
      var item = {
        id: food.id,
        name: food.name,
        brand: food.brand,
        serving: mult === 1 ? food.serving : mult + 'x ' + food.serving,
        servingGrams: Math.round(food.servingGrams * mult),
        calories: Math.round(food.calories * mult),
        protein: Math.round(food.protein * mult * 10) / 10,
        carbs: Math.round(food.carbs * mult * 10) / 10,
        fat: Math.round(food.fat * mult * 10) / 10,
        confirmed: false
      };
      totalCal += item.calories;
      results.push(item);
    });
    return { items: results, totalCalories: totalCal };
  }

  function nutritionFromFood(food, mult) {
    mult = mult || 1;
    return {
      calories: Math.round(food.calories * mult),
      protein: Math.round(food.protein * mult * 10) / 10,
      carbs: Math.round(food.carbs * mult * 10) / 10,
      fat: Math.round(food.fat * mult * 10) / 10
    };
  }

  function recalculateTotal(items) {
    var total = 0;
    items.forEach(function (it) { total += it.calories; });
    return total;
  }

  /* --- Session helpers ---------------------------------------------------- */
  function saveExtraction(data) {
    try { global.sessionStorage.setItem('vf.ai_extraction', JSON.stringify(data)); } catch (e) {}
  }
  function loadExtraction() {
    try {
      var raw = global.sessionStorage.getItem('vf.ai_extraction');
      return raw ? JSON.parse(raw) : null;
    } catch (e) { return null; }
  }
  function clearExtraction() {
    try { global.sessionStorage.removeItem('vf.ai_extraction'); } catch (e) {}
  }

  global.VF = global.VF || {};
  global.VF.aiFood = {
    FOODS: FOODS,
    BARCODE_PRODUCTS: BARCODE_PRODUCTS,
    findFoodByKeywords: findFoodByKeywords,
    findFoodByBarcode: findFoodByBarcode,
    estimateNutrition: estimateNutrition,
    nutritionFromFood: nutritionFromFood,
    recalculateTotal: recalculateTotal,
    saveExtraction: saveExtraction,
    loadExtraction: loadExtraction,
    clearExtraction: clearExtraction
  };
})(window);
