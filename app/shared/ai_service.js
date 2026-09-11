/* ==========================================================================
   VF.ai — AI food analysis abstraction layer.
   Clean interface so a real vision model/API can be connected later.
   All methods return Promises for async processing.
   ========================================================================== */
(function (global) {
  'use strict';

  var AI_STATES = {
    IDLE: 'idle',
    PREPARING: 'preparing',
    UPLOADING: 'uploading',
    ANALYZING: 'analyzing',
    ESTIMATING_PORTIONS: 'estimating_portions',
    CALCULATING_NUTRITION: 'calculating_nutrition',
    SUCCESS: 'success',
    TIMEOUT: 'timeout',
    NETWORK_ERROR: 'network_error',
    AI_ERROR: 'ai_error',
    UNSUPPORTED_IMAGE: 'unsupported_image',
    NO_FOOD_DETECTED: 'no_food_detected',
    PARTIAL_RECOGNITION: 'partial_recognition'
  };

  var CONFIDENCE_LEVELS = {
    HIGH: 'high',
    MEDIUM: 'medium',
    LOW: 'low'
  };

  var DEFAULT_TIMEOUT = 30000;
  var STAGE_DURATIONS = {
    preparing: 800,
    uploading: 1200,
    analyzing: 3000,
    estimating_portions: 2000,
    calculating_nutrition: 1500
  };

  function createFoodItem(overrides) {
    var base = {
      id: 'food_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9),
      name: 'Unknown Food',
      portion: '100g',
      portionGrams: 100,
      calories: 100,
      macros: { protein: 0, carbs: 0, fat: 0 },
      confidence: CONFIDENCE_LEVELS.MEDIUM,
      confidenceScore: 0.75,
      tags: []
    };
    return Object.assign(base, overrides);
  }

  function createAnalysisResult(overrides) {
    var base = {
      imageUrl: null,
      foods: [],
      totalCalories: 0,
      totalMacros: { protein: 0, carbs: 0, fat: 0 },
      overallConfidence: CONFIDENCE_LEVELS.MEDIUM,
      insights: '',
      tags: [],
      timestamp: new Date().toISOString()
    };
    return Object.assign(base, overrides);
  }

  function createErrorResult(code, message, recoverable) {
    return {
      error: true,
      code: code,
      message: message,
      recoverable: recoverable !== false,
      timestamp: new Date().toISOString()
    };
  }

  function simulateStageProgress(stage, onProgress) {
    var duration = STAGE_DURATIONS[stage] || 1000;
    var steps = 10;
    var stepTime = duration / steps;
    var current = 0;

    return new Promise(function (resolve) {
      var interval = setInterval(function () {
        current++;
        if (onProgress) onProgress(current / steps);
        if (current >= steps) {
          clearInterval(interval);
          resolve();
        }
      }, stepTime);
    });
  }

  function generateMockSingleFood() {
    var foods = [
      createFoodItem({
        name: 'Grilled Chicken Breast',
        portion: '180g',
        portionGrams: 180,
        calories: 298,
        macros: { protein: 55, carbs: 0, fat: 7 },
        confidence: CONFIDENCE_LEVELS.HIGH,
        confidenceScore: 0.94,
        tags: ['High Protein', 'Low Carb', 'Lean']
      }),
      createFoodItem({
        name: 'Avocado Toast with Egg',
        portion: '1 serving',
        portionGrams: 220,
        calories: 420,
        macros: { protein: 18, carbs: 32, fat: 22 },
        confidence: CONFIDENCE_LEVELS.HIGH,
        confidenceScore: 0.91,
        tags: ['Balanced', 'Healthy Fats']
      }),
      createFoodItem({
        name: 'Greek Yogurt Bowl with Berries',
        portion: '1 cup',
        portionGrams: 200,
        calories: 180,
        macros: { protein: 22, carbs: 18, fat: 5 },
        confidence: CONFIDENCE_LEVELS.MEDIUM,
        confidenceScore: 0.78,
        tags: ['High Protein', 'Probiotic']
      }),
      createFoodItem({
        name: 'Salmon Salad',
        portion: '1 bowl',
        portionGrams: 300,
        calories: 380,
        macros: { protein: 35, carbs: 12, fat: 22 },
        confidence: CONFIDENCE_LEVELS.HIGH,
        confidenceScore: 0.92,
        tags: ['Omega-3 Rich', 'High Protein', 'Low Carb']
      })
    ];
    return foods[Math.floor(Math.random() * foods.length)];
  }

  function generateMockMultipleFoods() {
    var meals = [
      {
        name: 'Chicken Rice Bowl',
        foods: [
          createFoodItem({ name: 'Grilled Chicken', portion: '180g', portionGrams: 180, calories: 298, macros: { protein: 55, carbs: 0, fat: 7 }, confidence: CONFIDENCE_LEVELS.HIGH, confidenceScore: 0.93, tags: ['High Protein'] }),
          createFoodItem({ name: 'Jasmine Rice', portion: '1 cup', portionGrams: 150, calories: 205, macros: { protein: 4, carbs: 45, fat: 0 }, confidence: CONFIDENCE_LEVELS.HIGH, confidenceScore: 0.95, tags: ['Carbs'] }),
          createFoodItem({ name: 'Steamed Broccoli', portion: '100g', portionGrams: 100, calories: 35, macros: { protein: 3, carbs: 7, fat: 0 }, confidence: CONFIDENCE_LEVELS.MEDIUM, confidenceScore: 0.82, tags: ['Vegetable'] }),
          createFoodItem({ name: 'Teriyaki Sauce', portion: '1 tbsp', portionGrams: 15, calories: 60, macros: { protein: 1, carbs: 14, fat: 0 }, confidence: CONFIDENCE_LEVELS.LOW, confidenceScore: 0.55, tags: ['Sauce'] })
        ]
      },
      {
        name: 'Salmon Quinoa Bowl',
        foods: [
          createFoodItem({ name: 'Grilled Salmon', portion: '150g', portionGrams: 150, calories: 280, macros: { protein: 39, carbs: 0, fat: 13 }, confidence: CONFIDENCE_LEVELS.HIGH, confidenceScore: 0.94, tags: ['Omega-3', 'High Protein'] }),
          createFoodItem({ name: 'Quinoa', portion: '100g', portionGrams: 100, calories: 120, macros: { protein: 4, carbs: 22, fat: 2 }, confidence: CONFIDENCE_LEVELS.HIGH, confidenceScore: 0.90, tags: ['Whole Grain'] }),
          createFoodItem({ name: 'Roasted Asparagus', portion: '5 spears', portionGrams: 80, calories: 45, macros: { protein: 2, carbs: 4, fat: 0 }, confidence: CONFIDENCE_LEVELS.MEDIUM, confidenceScore: 0.80, tags: ['Vegetable'] })
        ]
      }
    ];
    var meal = meals[Math.floor(Math.random() * meals.length)];
    var totalCalories = meal.foods.reduce(function (sum, f) { return sum + f.calories; }, 0);
    var totalMacros = meal.foods.reduce(function (sum, f) {
      return {
        protein: sum.protein + f.macros.protein,
        carbs: sum.carbs + f.macros.carbs,
        fat: sum.fat + f.macros.fat
      };
    }, { protein: 0, carbs: 0, fat: 0 });
    return { mealName: meal.name, foods: meal.foods, totalCalories: totalCalories, totalMacros: totalMacros };
  }

  function generateMockAmbiguousFood() {
    return [
      createFoodItem({
        name: 'Latte',
        portion: '1 cup (240ml)',
        portionGrams: 240,
        calories: 120,
        macros: { protein: 8, carbs: 12, fat: 4 },
        confidence: CONFIDENCE_LEVELS.LOW,
        confidenceScore: 0.45,
        tags: ['Coffee', 'Dairy']
      }),
      createFoodItem({
        name: 'Flat White',
        portion: '1 cup (180ml)',
        portionGrams: 180,
        calories: 90,
        macros: { protein: 6, carbs: 9, fat: 3 },
        confidence: CONFIDENCE_LEVELS.LOW,
        confidenceScore: 0.42,
        tags: ['Coffee', 'Dairy']
      })
    ];
  }

  var VF_AI = {
    AI_STATES: AI_STATES,
    CONFIDENCE_LEVELS: CONFIDENCE_LEVELS,

    analyzeImage: function (imageBlob, options) {
      options = options || {};
      var mode = options.mode || 'photo';
      var onStateChange = options.onStateChange || function () {};
      var onProgress = options.onProgress || function () {};

      var currentState = AI_STATES.IDLE;

      function setState(state) {
        currentState = state;
        onStateChange(state);
      }

      function setProgress(stage, progress) {
        onProgress({ stage: stage, progress: progress });
      }

      return new Promise(function (resolve, reject) {
        var timeoutHandle = setTimeout(function () {
          setState(AI_STATES.TIMEOUT);
          reject(createErrorResult('TIMEOUT', 'Analysis timed out. Please try again.', true));
        }, DEFAULT_TIMEOUT);

        function runAnalysis() {
          setState(AI_STATES.PREPARING);
          setProgress('preparing', 0);
          return simulateStageProgress('preparing', function (p) { setProgress('preparing', p); })
            .then(function () {
              setState(AI_STATES.UPLOADING);
              setProgress('uploading', 0);
              return simulateStageProgress('uploading', function (p) { setProgress('uploading', p); });
            })
            .then(function () {
              setState(AI_STATES.ANALYZING);
              setProgress('analyzing', 0);
              return simulateStageProgress('analyzing', function (p) { setProgress('analyzing', p); });
            })
            .then(function () {
              setState(AI_STATES.ESTIMATING_PORTIONS);
              setProgress('estimating_portions', 0);
              return simulateStageProgress('estimating_portions', function (p) { setProgress('estimating_portions', p); });
            })
            .then(function () {
              setState(AI_STATES.CALCULATING_NUTRITION);
              setProgress('calculating_nutrition', 0);
              return simulateStageProgress('calculating_nutrition', function (p) { setProgress('calculating_nutrition', p); });
            })
            .then(function () {
              clearTimeout(timeoutHandle);

              var imageUrl = URL.createObjectURL(imageBlob);
              var isMulti = Math.random() > 0.4;
              var isAmbiguous = Math.random() < 0.15;
              var isNoFood = Math.random() < 0.05;
              var isError = Math.random() < 0.03;

              if (isError) {
                setState(AI_STATES.AI_ERROR);
                reject(createErrorResult('AI_ERROR', 'AI analysis failed. Please try again.', true));
                return;
              }

              if (isNoFood) {
                setState(AI_STATES.NO_FOOD_DETECTED);
                resolve(createErrorResult('NO_FOOD_DETECTED', 'We couldn\'t identify a food in this photo.', true));
                return;
              }

              if (isAmbiguous) {
                var ambiguousFoods = generateMockAmbiguousFood();
                var result = createAnalysisResult({
                  imageUrl: imageUrl,
                  foods: ambiguousFoods,
                  totalCalories: ambiguousFoods[0].calories,
                  totalMacros: ambiguousFoods[0].macros,
                  overallConfidence: CONFIDENCE_LEVELS.LOW,
                  insights: 'I recognized the food category but need help identifying the specific item.',
                  tags: ['Needs Confirmation']
                });
                setState(AI_STATES.PARTIAL_RECOGNITION);
                resolve(result);
                return;
              }

              if (isMulti) {
                var multi = generateMockMultipleFoods();
                var result = createAnalysisResult({
                  imageUrl: imageUrl,
                  foods: multi.foods,
                  totalCalories: multi.totalCalories,
                  totalMacros: multi.totalMacros,
                  overallConfidence: CONFIDENCE_LEVELS.HIGH,
                  insights: 'Great meal! Good balance of protein, carbs, and vegetables.',
                  tags: ['Balanced Meal']
                });
                setState(AI_STATES.SUCCESS);
                resolve(result);
                return;
              }

              var singleFood = generateMockSingleFood();
              var result = createAnalysisResult({
                imageUrl: imageUrl,
                foods: [singleFood],
                totalCalories: singleFood.calories,
                totalMacros: singleFood.macros,
                overallConfidence: singleFood.confidence,
                insights: 'Excellent choice! High protein content supports muscle recovery.',
                tags: singleFood.tags
              });
              setState(AI_STATES.SUCCESS);
              resolve(result);
            })
            .catch(function (err) {
              clearTimeout(timeoutHandle);
              if (err.error) {
                reject(err);
              } else {
                setState(AI_STATES.AI_ERROR);
                reject(createErrorResult('AI_ERROR', 'Analysis failed: ' + err.message, true));
              }
            });
        }

        runAnalysis();
      });
    },

    analyzeText: function (text, options) {
      options = options || {};
      var onStateChange = options.onStateChange || function () {};
      var onProgress = options.onProgress || function () {};

      return new Promise(function (resolve, reject) {
        var timeoutHandle = setTimeout(function () {
          reject(createErrorResult('TIMEOUT', 'Analysis timed out. Please try again.', true));
        }, DEFAULT_TIMEOUT);

        var stages = ['preparing', 'analyzing', 'calculating_nutrition'];
        var current = 0;

        function runStage() {
          if (current >= stages.length) {
            clearTimeout(timeoutHandle);
            var singleFood = generateMockSingleFood();
            singleFood.name = text.split(',')[0].trim() || 'Described Meal';
            var result = createAnalysisResult({
              imageUrl: null,
              foods: [singleFood],
              totalCalories: singleFood.calories,
              totalMacros: singleFood.macros,
              overallConfidence: CONFIDENCE_LEVELS.MEDIUM,
              insights: 'Based on your description: ' + text,
              tags: ['Text Input']
            });
            resolve(result);
            return;
          }
          onStateChange(AI_STATES[stages[current].toUpperCase()]);
          onProgress({ stage: stages[current], progress: 0 });
          simulateStageProgress(stages[current], function (p) { onProgress({ stage: stages[current], progress: p }); })
            .then(function () {
              current++;
              runStage();
            })
            .catch(reject);
        }
        runStage();
      });
    },

    getConfidenceLabel: function (level) {
      switch (level) {
        case CONFIDENCE_LEVELS.HIGH: return 'High confidence';
        case CONFIDENCE_LEVELS.MEDIUM: return 'Medium confidence';
        case CONFIDENCE_LEVELS.LOW: return 'Needs confirmation';
        default: return 'Unknown confidence';
      }
    },

    getConfidenceColor: function (level) {
      switch (level) {
        case CONFIDENCE_LEVELS.HIGH: return 'text-secondary';
        case CONFIDENCE_LEVELS.MEDIUM: return 'text-tertiary';
        case CONFIDENCE_LEVELS.LOW: return 'text-warning';
        default: return 'text-on-surface-variant';
      }
    },

    getConfidenceIcon: function (level) {
      switch (level) {
        case CONFIDENCE_LEVELS.HIGH: return 'check_circle';
        case CONFIDENCE_LEVELS.MEDIUM: return 'help_outline';
        case CONFIDENCE_LEVELS.LOW: return 'priority_high';
        default: return 'help';
      }
    },

    formatMacros: function (macros) {
      return {
        protein: macros.protein + 'g',
        carbs: macros.carbs + 'g',
        fat: macros.fat + 'g'
      };
    },

    extractFoodsFromText: function (text, options) {
      options = options || {};
      var onStateChange = options.onStateChange || function () {};
      var onProgress = options.onProgress || function () {};

      var EXTRACTION_STAGES = [
        'parsing',
        'identifying_foods',
        'matching_database',
        'estimating_portions',
        'calculating_nutrition'
      ];

      var STAGE_DURATIONS = {
        parsing: 500,
        identifying_foods: 1500,
        matching_database: 1200,
        estimating_portions: 1000,
        calculating_nutrition: 800
      };

      return new Promise(function (resolve, reject) {
        var timeoutHandle = setTimeout(function () {
          reject(createErrorResult('TIMEOUT', 'Food extraction timed out. Please try again.', true));
        }, 30000);

        var currentStage = 0;
        var extractedFoods = [];

        function runStage() {
          if (currentStage >= EXTRACTION_STAGES.length) {
            clearTimeout(timeoutHandle);
            var result = createAnalysisResult({
              imageUrl: null,
              foods: extractedFoods,
              totalCalories: extractedFoods.reduce(function (sum, f) { return sum + f.calories; }, 0),
              totalMacros: extractedFoods.reduce(function (sum, f) {
                return {
                  protein: sum.protein + f.macros.protein,
                  carbs: sum.carbs + f.macros.carbs,
                  fat: sum.fat + f.macros.fat
                };
              }, { protein: 0, carbs: 0, fat: 0 }),
              overallConfidence: CONFIDENCE_LEVELS.MEDIUM,
              insights: 'Extracted from your description: ' + text,
              tags: ['Voice/Text Input']
            });
            resolve(result);
            return;
          }

          var stage = EXTRACTION_STAGES[currentStage];
          onStateChange(stage.toUpperCase().replace('_', '_'));
          onProgress({ stage: stage, progress: 0 });

          simulateStageProgress(stage, function (p) { onProgress({ stage: stage, progress: p }); })
            .then(function () {
              if (stage === 'identifying_foods') {
                extractedFoods = parseFoodDescription(text);
              } else if (stage === 'matching_database') {
                extractedFoods = matchFoodsToDatabase(extractedFoods);
              } else if (stage === 'estimating_portions') {
                extractedFoods = estimatePortions(extractedFoods, text);
              } else if (stage === 'calculating_nutrition') {
                extractedFoods = calculateNutrition(extractedFoods);
              }
              currentStage++;
              runStage();
            })
            .catch(reject);
        }
        runStage();
      });
    },

    extractFoodsFromVoice: function (audioBlob, options) {
      options = options || {};
      var onStateChange = options.onStateChange || function () {};
      var onProgress = options.onProgress || function () {};

      return new Promise(function (resolve, reject) {
        onStateChange('TRANSCRIBING');
        onProgress({ stage: 'transcribing', progress: 0 });

        simulateStageProgress('transcribing', function (p) { onProgress({ stage: 'transcribing', progress: p }); })
          .then(function () {
            var mockTranscriptions = [
              'I had two eggs, two slices of toast and a banana',
              'Grilled chicken salad with olive oil dressing and quinoa',
              'A banana and a protein shake',
              'Chicken sandwich and a Coke',
              'Pasta with tomato sauce and parmesan cheese'
            ];
            var transcription = mockTranscriptions[Math.floor(Math.random() * mockTranscriptions.length)];
            return VF.ai.extractFoodsFromText(transcription, { onStateChange: onStateChange, onProgress: onProgress });
          })
          .then(resolve)
          .catch(reject);
      });
    }
  };

  function parseFoodDescription(text) {
    var foods = [];
    var lowerText = text.toLowerCase();

    var foodPatterns = [
      { patterns: ['egg', 'eggs'], name: 'Egg', basePortion: '1 large', baseGrams: 50, caloriesPer100g: 155, macrosPer100g: { protein: 13, carbs: 1.1, fat: 11 } },
      { patterns: ['toast', 'bread', 'slice'], name: 'Toast', basePortion: '1 slice', baseGrams: 30, caloriesPer100g: 265, macrosPer100g: { protein: 9, carbs: 49, fat: 3.2 } },
      { patterns: ['banana'], name: 'Banana', basePortion: '1 medium', baseGrams: 118, caloriesPer100g: 89, macrosPer100g: { protein: 1.1, carbs: 23, fat: 0.3 } },
      { patterns: ['chicken', 'grilled chicken'], name: 'Grilled Chicken Breast', basePortion: '100g', baseGrams: 100, caloriesPer100g: 165, macrosPer100g: { protein: 31, carbs: 0, fat: 3.6 } },
      { patterns: ['salad', 'greens', 'lettuce'], name: 'Mixed Greens Salad', basePortion: '1 cup', baseGrams: 30, caloriesPer100g: 15, macrosPer100g: { protein: 1.4, carbs: 2.9, fat: 0.2 } },
      { patterns: ['olive oil', 'oil'], name: 'Olive Oil', basePortion: '1 tbsp', baseGrams: 14, caloriesPer100g: 884, macrosPer100g: { protein: 0, carbs: 0, fat: 100 } },
      { patterns: ['quinoa'], name: 'Quinoa', basePortion: '1 cup cooked', baseGrams: 185, caloriesPer100g: 120, macrosPer100g: { protein: 4.4, carbs: 22, fat: 1.9 } },
      { patterns: ['rice', 'jasmine rice', 'brown rice'], name: 'Rice', basePortion: '1 cup cooked', baseGrams: 158, caloriesPer100g: 130, macrosPer100g: { protein: 2.7, carbs: 28, fat: 0.3 } },
      { patterns: ['protein shake', 'shake', 'whey'], name: 'Protein Shake', basePortion: '1 scoop (30g)', baseGrams: 30, caloriesPer100g: 380, macrosPer100g: { protein: 80, carbs: 5, fat: 3 } },
      { patterns: ['coke', 'cola', 'soda'], name: 'Coca-Cola', basePortion: '1 can (330ml)', baseGrams: 330, caloriesPer100g: 42, macrosPer100g: { protein: 0, carbs: 10.6, fat: 0 } },
      { patterns: ['sandwich', 'burger'], name: 'Chicken Sandwich', basePortion: '1 sandwich', baseGrams: 200, caloriesPer100g: 250, macrosPer100g: { protein: 18, carbs: 30, fat: 8 } },
      { patterns: ['pasta', 'spaghetti', 'noodles'], name: 'Pasta', basePortion: '1 cup cooked', baseGrams: 140, caloriesPer100g: 131, macrosPer100g: { protein: 5, carbs: 25, fat: 1.1 } },
      { patterns: ['tomato sauce', 'marinara'], name: 'Tomato Sauce', basePortion: '1/2 cup', baseGrams: 125, caloriesPer100g: 32, macrosPer100g: { protein: 1.5, carbs: 7, fat: 0.2 } },
      { patterns: ['parmesan', 'cheese'], name: 'Parmesan Cheese', basePortion: '1 tbsp', baseGrams: 5, caloriesPer100g: 431, macrosPer100g: { protein: 38, carbs: 4.1, fat: 29 } },
      { patterns: ['avocado'], name: 'Avocado', basePortion: '1/2 medium', baseGrams: 75, caloriesPer100g: 160, macrosPer100g: { protein: 2, carbs: 8.5, fat: 14.7 } },
      { patterns: ['salmon', 'fish'], name: 'Salmon', basePortion: '100g', baseGrams: 100, caloriesPer100g: 208, macrosPer100g: { protein: 20, carbs: 0, fat: 13 } },
      { patterns: ['yogurt', 'greek yogurt'], name: 'Greek Yogurt', basePortion: '1 cup', baseGrams: 245, caloriesPer100g: 59, macrosPer100g: { protein: 10, carbs: 3.6, fat: 0.4 } },
      { patterns: ['apple'], name: 'Apple', basePortion: '1 medium', baseGrams: 182, caloriesPer100g: 52, macrosPer100g: { protein: 0.3, carbs: 14, fat: 0.2 } },
      { patterns: ['oatmeal', 'oats', 'porridge'], name: 'Oatmeal', basePortion: '1 cup cooked', baseGrams: 234, caloriesPer100g: 68, macrosPer100g: { protein: 2.4, carbs: 12, fat: 1.4 } },
      { patterns: ['peanut butter', 'pb'], name: 'Peanut Butter', basePortion: '2 tbsp', baseGrams: 32, caloriesPer100g: 588, macrosPer100g: { protein: 25, carbs: 20, fat: 50 } },
      { patterns: ['almond', 'almonds'], name: 'Almonds', basePortion: '1 oz (23 nuts)', baseGrams: 28, caloriesPer100g: 579, macrosPer100g: { protein: 21, carbs: 22, fat: 50 } }
    ];

    var quantityPatterns = [
      { regex: /\b(two|2)\b/, multiplier: 2 },
      { regex: /\b(three|3)\b/, multiplier: 3 },
      { regex: /\b(four|4)\b/, multiplier: 4 },
      { regex: /\b(five|5)\b/, multiplier: 5 },
      { regex: /\b(six|6)\b/, multiplier: 6 },
      { regex: /\b(one|1|a|an)\b/, multiplier: 1 }
    ];

    foodPatterns.forEach(function (food) {
      var matched = food.patterns.some(function (p) { return lowerText.includes(p); });
      if (!matched) return;

      var multiplier = 1;
      for (var i = 0; i < quantityPatterns.length; i++) {
        if (quantityPatterns[i].regex.test(lowerText)) {
          multiplier = quantityPatterns[i].multiplier;
          break;
        }
      }

      var portionGrams = food.baseGrams * multiplier;
      var factor = portionGrams / 100;
      foods.push({
        id: 'food_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9),
        name: food.name,
        portion: multiplier > 1 ? multiplier + 'x ' + food.basePortion : food.basePortion,
        portionGrams: Math.round(portionGrams),
        calories: Math.round(food.caloriesPer100g * factor),
        macros: {
          protein: Math.round(food.macrosPer100g.protein * factor * 10) / 10,
          carbs: Math.round(food.macrosPer100g.carbs * factor * 10) / 10,
          fat: Math.round(food.macrosPer100g.fat * factor * 10) / 10
        },
        confidence: CONFIDENCE_LEVELS.MEDIUM,
        confidenceScore: 0.75,
        tags: ['Text Parsed']
      });
    });

    if (foods.length === 0) {
      foods.push({
        id: 'food_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9),
        name: 'Unknown Meal',
        portion: '1 serving',
        portionGrams: 200,
        calories: 300,
        macros: { protein: 15, carbs: 30, fat: 12 },
        confidence: CONFIDENCE_LEVELS.LOW,
        confidenceScore: 0.4,
        tags: ['Unrecognized']
      });
    }

    return foods;
  }

  function matchFoodsToDatabase(foods) {
    return foods.map(function (food) {
      return Object.assign({}, food, { confidence: CONFIDENCE_LEVELS.HIGH, confidenceScore: 0.9, tags: (food.tags || []).concat(['Database Matched']) });
    });
  }

  function estimatePortions(foods, text) {
    return foods;
  }

  function calculateNutrition(foods) {
    return foods;
  }

  global.VF = global.VF || {};
  global.VF.ai = VF_AI;
})(window);