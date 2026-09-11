/* ==========================================================================
   VF.diary — complete diary, food management, recipes, favorites, recent.
   ========================================================================== */
(function (global) {
  'use strict';

  var DIARY_KEY = 'vitality.diary.v1';
  var FOODS_KEY = 'vitality.foods.v1';
  var RECIPES_KEY = 'vitality.recipes.v1';
  var SAVED_MEALS_KEY = 'vitality.saved_meals.v1';
  var FAVORITES_KEY = 'vitality.favorites.v1';
  var RECENT_KEY = 'vitality.recent.v1';
  var UNDO_KEY = 'vitality.undo.v1';
  var WEIGHT_KEY = 'vitality.weight.v1';

  var MEAL_TYPES = [
    { id: 'breakfast', label: 'Breakfast', icon: 'wb_sunny' },
    { id: 'lunch',     label: 'Lunch',     icon: 'wb_sunny' },
    { id: 'dinner',    label: 'Dinner',    icon: 'nightlight' },
    { id: 'snack',     label: 'Snack',     icon: 'cookie' }
  ];

  var UNITS = [
    { id: 'g', label: 'g', factor: 1 },
    { id: 'oz', label: 'oz', factor: 28.3495 },
    { id: 'cup', label: 'cup', factor: 240 },
    { id: 'piece', label: 'piece', factor: 1 },
    { id: 'serving', label: 'serving', factor: 1 },
    { id: 'tbsp', label: 'tbsp', factor: 15 },
    { id: 'tsp', label: 'tsp', factor: 5 }
  ];

  var FOOD_DB = [
    { id: 'chicken_breast', name: 'Chicken Breast', brand: 'Generic', serving: '100 g', servingGrams: 100, calories: 165, protein: 31, carbs: 0, fat: 3.6, fiber: 0, keywords: ['chicken', 'chicken breast', 'grilled chicken', 'boiled chicken'] },
    { id: 'rice', name: 'White Rice', brand: 'Generic', serving: '1 cup', servingGrams: 240, calories: 242, protein: 4.4, carbs: 53, fat: 0.4, fiber: 0.6, keywords: ['rice', 'white rice', 'steamed rice'] },
    { id: 'egg', name: 'Egg', brand: 'Generic', serving: '1 large', servingGrams: 50, calories: 72, protein: 6.3, carbs: 0.4, fat: 5, fiber: 0, keywords: ['egg', 'eggs', 'boiled egg', 'fried egg'] },
    { id: 'banana', name: 'Banana', brand: 'Generic', serving: '1 medium', servingGrams: 118, calories: 105, protein: 1.3, carbs: 27, fat: 0.4, fiber: 3.1, keywords: ['banana', 'bananas'] },
    { id: 'oats', name: 'Oats', brand: 'Generic', serving: '40 g', servingGrams: 40, calories: 154, protein: 5.3, carbs: 27, fat: 2.6, fiber: 4, keywords: ['oats', 'oatmeal', 'porridge', 'overnight oats'] },
    { id: 'bread', name: 'Bread', brand: 'Generic', serving: '1 slice', servingGrams: 30, calories: 79, protein: 2.7, carbs: 15, fat: 1, fiber: 0.8, keywords: ['bread', 'toast', 'sourdough', 'whole wheat bread'] },
    { id: 'milk', name: 'Milk', brand: 'Generic', serving: '1 cup', servingGrams: 240, calories: 149, protein: 8, carbs: 12, fat: 8, fiber: 0, keywords: ['milk', 'cow milk', 'whole milk'] },
    { id: 'yogurt', name: 'Yogurt', brand: 'Generic', serving: '1 cup', servingGrams: 245, calories: 150, protein: 12, carbs: 18, fat: 4, fiber: 0, keywords: ['yogurt', 'greek yogurt', 'yoghurt'] },
    { id: 'cheese', name: 'Cheese', brand: 'Generic', serving: '30 g', servingGrams: 30, calories: 110, protein: 7, carbs: 0.5, fat: 9, fiber: 0, keywords: ['cheese', 'cheddar', 'mozzarella', 'parmesan'] },
    { id: 'apple', name: 'Apple', brand: 'Generic', serving: '1 medium', servingGrams: 182, calories: 95, protein: 0.5, carbs: 25, fat: 0.3, fiber: 4.4, keywords: ['apple', 'apples'] },
    { id: 'pasta', name: 'Pasta', brand: 'Generic', serving: '1 cup', servingGrams: 140, calories: 200, protein: 7, carbs: 40, fat: 1.2, fiber: 2.5, keywords: ['pasta', 'spaghetti', 'penne', 'macaroni'] },
    { id: 'pizza', name: 'Pizza', brand: 'Generic', serving: '1 slice', servingGrams: 120, calories: 285, protein: 12, carbs: 36, fat: 10, fiber: 2.5, keywords: ['pizza', 'slice', 'pepperoni', 'margherita'] },
    { id: 'burger', name: 'Burger', brand: 'Generic', serving: '1 burger', servingGrams: 250, calories: 550, protein: 28, carbs: 40, fat: 30, fiber: 2, keywords: ['burger', 'cheeseburger', 'hamburger', 'beef burger'] },
    { id: 'coke', name: 'Coke', brand: 'Coca-Cola', serving: '330 ml', servingGrams: 330, calories: 140, protein: 0, carbs: 39, fat: 0, fiber: 0, keywords: ['coke', 'coca cola', 'cola', 'soda'] },
    { id: 'coffee', name: 'Coffee', brand: 'Generic', serving: '1 cup', servingGrams: 240, calories: 5, protein: 0, carbs: 0, fat: 0, fiber: 0, keywords: ['coffee', 'espresso', 'americano', 'black coffee'] },
    { id: 'latte', name: 'Latte', brand: 'Generic', serving: '1 cup', servingGrams: 350, calories: 180, protein: 10, carbs: 15, fat: 8, fiber: 0, keywords: ['latte', 'cappuccino', 'flat white'] },
    { id: 'salad', name: 'Garden Salad', brand: 'Generic', serving: '1 bowl', servingGrams: 200, calories: 120, protein: 3, carbs: 12, fat: 7, fiber: 4, keywords: ['salad', 'garden salad', 'green salad', 'mixed greens'] },
    { id: 'steak', name: 'Steak', brand: 'Generic', serving: '150 g', servingGrams: 150, calories: 270, protein: 38, carbs: 0, fat: 12, fiber: 0, keywords: ['steak', 'beef', 'ribeye', 'sirloin'] },
    { id: 'salmon', name: 'Salmon', brand: 'Generic', serving: '100 g', servingGrams: 100, calories: 208, protein: 20, carbs: 0, fat: 13, fiber: 0, keywords: ['salmon', 'fish', 'grilled salmon'] },
    { id: 'broccoli', name: 'Broccoli', brand: 'Generic', serving: '100 g', servingGrams: 100, calories: 55, protein: 3.7, carbs: 7, fat: 0.4, fiber: 2.6, keywords: ['broccoli', 'green vegetable'] },
    { id: 'carrot', name: 'Carrot', brand: 'Generic', serving: '100 g', servingGrams: 100, calories: 41, protein: 0.9, carbs: 10, fat: 0.2, fiber: 2.8, keywords: ['carrot', 'carrots'] },
    { id: 'almonds', name: 'Almonds', brand: 'Generic', serving: '30 g', servingGrams: 30, calories: 173, protein: 6.4, carbs: 6, fat: 15, fiber: 3.5, keywords: ['almonds', 'nuts'] },
    { id: 'peanut_butter', name: 'Peanut Butter', brand: 'Generic', serving: '2 tbsp', servingGrams: 32, calories: 190, protein: 8, carbs: 6, fat: 16, fiber: 2, keywords: ['peanut butter', 'pb'] },
    { id: 'honey', name: 'Honey', brand: 'Generic', serving: '1 tbsp', servingGrams: 21, calories: 64, protein: 0.1, carbs: 17, fat: 0, fiber: 0, keywords: ['honey'] },
    { id: 'butter', name: 'Butter', brand: 'Generic', serving: '1 tbsp', servingGrams: 14, calories: 102, protein: 0.1, carbs: 0, fat: 12, fiber: 0, keywords: ['butter'] },
    { id: 'olive_oil', name: 'Olive Oil', brand: 'Generic', serving: '1 tbsp', servingGrams: 14, calories: 119, protein: 0, carbs: 0, fat: 14, fiber: 0, keywords: ['olive oil', 'oil', 'dressing'] }
  ];

  function today() {
    var d = new Date();
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
  }

  function uid() {
    return Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
  }

  function read(key) {
    try {
      var raw = global.localStorage.getItem(key);
      if (!raw) return null;
      return JSON.parse(raw);
    } catch (e) { return null; }
  }

  function write(key, data) {
    try { global.localStorage.setItem(key, JSON.stringify(data)); } catch (e) {}
  }

  function ensureArray(key, defaults) {
    var data = read(key);
    if (!Array.isArray(data)) data = defaults || [];
    write(key, data);
    return data;
  }

  /* --- Foods -------------------------------------------------------------- */
  function searchFoods(query) {
    if (!query) return FOOD_DB.slice(0, 20);
    var q = query.toLowerCase();
    return FOOD_DB.filter(function (f) {
      return f.name.toLowerCase().indexOf(q) > -1 || (f.keywords || []).some(function (k) { return k.indexOf(q) > -1; });
    }).slice(0, 50);
  }

  function getFoodById(id) {
    return FOOD_DB.find(function (f) { return f.id === id; }) || null;
  }

  function getCustomFoods() {
    return ensureArray(FOODS_KEY, []);
  }

  function saveCustomFood(food) {
    var foods = getCustomFoods();
    food.id = food.id || uid();
    food.createdAt = new Date().toISOString();
    var idx = foods.findIndex(function (f) { return f.id === food.id; });
    if (idx > -1) foods[idx] = food; else foods.push(food);
    write(FOODS_KEY, foods);
    return food;
  }

  function deleteCustomFood(id) {
    write(FOODS_KEY, getCustomFoods().filter(function (f) { return f.id !== id; }));
  }

  /* --- Recipes ------------------------------------------------------------ */
  function getRecipes() {
    return ensureArray(RECIPES_KEY, []);
  }

  function saveRecipe(recipe) {
    var recipes = getRecipes();
    recipe.id = recipe.id || uid();
    recipe.createdAt = new Date().toISOString();
    var idx = recipes.findIndex(function (r) { return r.id === recipe.id; });
    if (idx > -1) recipes[idx] = recipe; else recipes.push(recipe);
    write(RECIPES_KEY, recipes);
    return recipe;
  }

  function deleteRecipe(id) {
    write(RECIPES_KEY, getRecipes().filter(function (r) { return r.id !== id; }));
  }

  function recipeNutrition(recipe) {
    var total = { calories: 0, protein: 0, carbs: 0, fat: 0, fiber: 0 };
    (recipe.ingredients || []).forEach(function (ing) {
      total.calories += Number(ing.calories) || 0;
      total.protein += Number(ing.protein) || 0;
      total.carbs += Number(ing.carbs) || 0;
      total.fat += Number(ing.fat) || 0;
      total.fiber += Number(ing.fiber) || 0;
    });
    var perServing = recipe.servings && recipe.servings > 0 ? recipe.servings : 1;
    return {
      total: total,
      perServing: {
        calories: Math.round(total.calories / perServing),
        protein: Math.round((total.protein / perServing) * 10) / 10,
        carbs: Math.round((total.carbs / perServing) * 10) / 10,
        fat: Math.round((total.fat / perServing) * 10) / 10,
        fiber: Math.round((total.fiber / perServing) * 10) / 10
      }
    };
  }

  /* --- Saved Meals -------------------------------------------------------- */
  function getSavedMeals() {
    return ensureArray(SAVED_MEALS_KEY, []);
  }

  function saveMealAsTemplate(name, items, mealType) {
    var templates = getSavedMeals();
    var template = {
      id: uid(),
      name: name || 'Untitled Meal',
      mealType: mealType || 'snack',
      items: items || [],
      totalCalories: (items || []).reduce(function (s, it) { return s + (Number(it.calories) || 0); }, 0),
      createdAt: new Date().toISOString()
    };
    templates.push(template);
    write(SAVED_MEALS_KEY, templates);
    return template;
  }

  function deleteSavedMeal(id) {
    write(SAVED_MEALS_KEY, getSavedMeals().filter(function (m) { return m.id !== id; }));
  }

  /* --- Favorites ---------------------------------------------------------- */
  function getFavorites() {
    return ensureArray(FAVORITES_KEY, []);
  }

  function toggleFavorite(item) {
    var favs = getFavorites();
    var idx = favs.findIndex(function (f) { return f.id === item.id && f.type === item.type; });
    if (idx > -1) { favs.splice(idx, 1); write(FAVORITES_KEY, favs); return false; }
    item.favoritedAt = new Date().toISOString();
    favs.push(item);
    write(FAVORITES_KEY, favs);
    return true;
  }

  function isFavorite(id, type) {
    return getFavorites().some(function (f) { return f.id === id && f.type === type; });
  }

  /* --- Recent Foods ------------------------------------------------------- */
  function getRecentFoods(limit) {
    limit = limit || 20;
    return ensureArray(RECENT_KEY, []).slice(0, limit);
  }

  function addRecentFood(food) {
    var recent = getRecentFoods(50);
    var idx = recent.findIndex(function (r) { return r.id === food.id; });
    if (idx > -1) recent.splice(idx, 1);
    recent.unshift({
      id: food.id,
      name: food.name,
      brand: food.brand || '',
      serving: food.serving || '1 serving',
      servingGrams: food.servingGrams || 100,
      calories: food.calories || 0,
      protein: food.protein || 0,
      carbs: food.carbs || 0,
      fat: food.fat || 0,
      fiber: food.fiber || 0,
      addedAt: new Date().toISOString()
    });
    write(RECENT_KEY, recent);
  }

  /* --- Diary / Meals ------------------------------------------------------ */
  function today() {
    var d = new Date();
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
  }

  function readMeals() {
    return ensureArray(DIARY_KEY, []);
  }

  function writeMeals(meals) {
    write(DIARY_KEY, meals);
  }

  function getMealsByDate(date) {
    return readMeals().filter(function (m) { return m.date === date; });
  }

  function getMealsByType(date, type) {
    return readMeals().filter(function (m) { return m.date === date && m.mealType === type; });
  }

  function getMealById(id) {
    return readMeals().find(function (m) { return m.id === id; }) || null;
  }

  function getDailyTotals(date) {
    var meals = getMealsByDate(date || today());
    var totals = { calories: 0, protein: 0, carbs: 0, fat: 0, fiber: 0, items: 0 };
    meals.forEach(function (m) {
      totals.calories += (Number(m.totalCalories) || 0);
      (m.items || []).forEach(function (it) {
        totals.protein += (Number(it.protein) || 0);
        totals.carbs += (Number(it.carbs) || 0);
        totals.fat += (Number(it.fat) || 0);
        totals.fiber += (Number(it.fiber) || 0);
        totals.items += 1;
      });
    });
    return totals;
  }

  function logMeal(items, mealType, source, date) {
    source = source || 'unknown';
    mealType = mealType || suggestMealType();
    date = date || today();
    var dup = isDuplicate(items, date);
    if (dup) return { status: 'duplicate', meal: dup };
    var meal = {
      id: uid(),
      date: date,
      mealType: mealType,
      items: items || [],
      totalCalories: 0,
      loggedAt: new Date().toISOString(),
      source: source,
      synced: global.navigator && global.navigator.onLine === false ? false : true
    };
    var total = 0;
    (items || []).forEach(function (it) { total += (Number(it.calories) || 0); });
    meal.totalCalories = total;
    var meals = readMeals();
    meals.push(meal);
    writeMeals(meals);
    if (!meal.synced) return { status: 'offline', meal: meal };
    return { status: 'success', meal: meal };
  }

  function isDuplicate(items, date) {
    var meals = readMeals();
    var now = Date.now();
    for (var i = 0; i < meals.length; i++) {
      var m = meals[i];
      if (m.date !== (date || today())) continue;
      if (now - new Date(m.loggedAt).getTime() > 5 * 60 * 1000) continue;
      if (!m.items || !items) continue;
      if (m.items.length !== items.length) continue;
      var match = true;
      for (var j = 0; j < items.length; j++) {
        if ((m.items[j].name || '').toLowerCase() !== (items[j].name || '').toLowerCase()) { match = false; break; }
      }
      if (match) return m;
    }
    return null;
  }

  function updateMeal(id, changes) {
    var meals = readMeals();
    var idx = meals.findIndex(function (m) { return m.id === id; });
    if (idx === -1) return null;
    Object.keys(changes || {}).forEach(function (k) { meals[idx][k] = changes[k]; });
    if (changes.items) {
      meals[idx].totalCalories = (changes.items || []).reduce(function (s, it) { return s + (Number(it.calories) || 0); }, 0);
    }
    writeMeals(meals);
    return meals[idx];
  }

  function deleteMeal(id) {
    var meal = getMealById(id);
    if (!meal) return null;
    write(UNDO_KEY, { meal: meal, deletedAt: new Date().toISOString() });
    writeMeals(readMeals().filter(function (m) { return m.id !== id; }));
    return meal;
  }

  function undoDelete() {
    var undo = read(UNDO_KEY);
    if (!undo || !undo.meal) return null;
    var meals = readMeals();
    meals.push(undo.meal);
    writeMeals(meals);
    write(UNDO_KEY, null);
    return undo.meal;
  }

  function canUndo() {
    return !!read(UNDO_KEY);
  }

  function moveMeal(id, newDate, newMealType) {
    return updateMeal(id, { date: newDate, mealType: newMealType });
  }

  function duplicateMeal(id) {
    var meal = getMealById(id);
    if (!meal) return null;
    var newMeal = {
      id: uid(),
      date: today(),
      mealType: meal.mealType,
      items: (meal.items || []).map(function (it) {
        return Object.assign({}, it, { id: uid() });
      }),
      totalCalories: meal.totalCalories,
      loggedAt: new Date().toISOString(),
      source: meal.source,
      synced: true
    };
    var meals = readMeals();
    meals.push(newMeal);
    writeMeals(meals);
    return newMeal;
  }

  function suggestMealType() {
    var h = new Date().getHours();
    if (h < 11) return 'breakfast';
    if (h < 15) return 'lunch';
    if (h < 21) return 'dinner';
    return 'snack';
  }

  function formatDate(dateStr) {
    var d = new Date(dateStr + 'T00:00:00');
    return d.toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric' });
  }

  function formatShortDate(dateStr) {
    var d = new Date(dateStr + 'T00:00:00');
    return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  }

  function dateOffset(base, days) {
    var d = new Date(base + 'T00:00:00');
    d.setDate(d.getDate() + days);
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
  }

  /* --- Weight -------------------------------------------------------------- */
  function getWeightEntries() {
    return ensureArray(WEIGHT_KEY, []);
  }

  function logWeight(weightKg, date, note) {
    var entries = getWeightEntries();
    var entry = {
      id: uid(),
      weightKg: Number(weightKg) || 0,
      date: date || today(),
      note: note || '',
      loggedAt: new Date().toISOString()
    };
    entries.push(entry);
    entries.sort(function (a, b) { return a.date.localeCompare(b.date); });
    write(WEIGHT_KEY, entries);
    return entry;
  }

  function getWeightByDate(date) {
    return getWeightEntries().find(function (w) { return w.date === date; }) || null;
  }

  function getWeightRange(startDate, endDate) {
    return getWeightEntries().filter(function (w) { return w.date >= startDate && w.date <= endDate; });
  }

  function getLatestWeight() {
    var entries = getWeightEntries();
    return entries.length ? entries[entries.length - 1] : null;
  }

  function getWeightChange(days) {
    days = days || 7;
    var end = today();
    var start = dateOffset(end, -days);
    var range = getWeightRange(start, end);
    if (range.length < 2) return null;
    return range[range.length - 1].weightKg - range[0].weightKg;
  }

  global.VF = global.VF || {};
  global.VF.diary = {
    MEAL_TYPES: MEAL_TYPES,
    UNITS: UNITS,
    FOOD_DB: FOOD_DB,
    today: today,
    uid: uid,
    suggestMealType: suggestMealType,
    formatDate: formatDate,
    formatShortDate: formatShortDate,
    dateOffset: dateOffset,
    searchFoods: searchFoods,
    getFoodById: getFoodById,
    getCustomFoods: getCustomFoods,
    saveCustomFood: saveCustomFood,
    deleteCustomFood: deleteCustomFood,
    getRecipes: getRecipes,
    saveRecipe: saveRecipe,
    deleteRecipe: deleteRecipe,
    recipeNutrition: recipeNutrition,
    getSavedMeals: getSavedMeals,
    saveMealAsTemplate: saveMealAsTemplate,
    deleteSavedMeal: deleteSavedMeal,
    getFavorites: getFavorites,
    toggleFavorite: toggleFavorite,
    isFavorite: isFavorite,
    getRecentFoods: getRecentFoods,
    addRecentFood: addRecentFood,
    getMealsByDate: getMealsByDate,
    getMealsByType: getMealsByType,
    getMealById: getMealById,
    getDailyTotals: getDailyTotals,
    logMeal: logMeal,
    updateMeal: updateMeal,
    deleteMeal: deleteMeal,
    undoDelete: undoDelete,
    canUndo: canUndo,
    moveMeal: moveMeal,
    duplicateMeal: duplicateMeal,
    isDuplicate: isDuplicate,
    getWeightEntries: getWeightEntries,
    logWeight: logWeight,
    getWeightByDate: getWeightByDate,
    getWeightRange: getWeightRange,
    getLatestWeight: getLatestWeight,
    getWeightChange: getWeightChange
  };
})(window);
