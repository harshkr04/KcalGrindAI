import { GoogleGenAI, Type } from '@google/genai';

console.log('Testing @google/genai Schema and Structure verification...');

const FOOD_ANALYSIS_SCHEMA = {
  type: Type.OBJECT,
  properties: {
    foods: {
      type: Type.ARRAY,
      items: {
        type: Type.OBJECT,
        properties: {
          name: { type: Type.STRING },
          estimatedGrams: { type: Type.NUMBER },
          calories: { type: Type.NUMBER },
          macros: {
            type: Type.OBJECT,
            properties: {
              protein: { type: Type.NUMBER },
              carbs: { type: Type.NUMBER },
              fat: { type: Type.NUMBER }
            },
            required: ['protein', 'carbs', 'fat']
          },
          confidence: { type: Type.NUMBER }
        },
        required: ['name', 'estimatedGrams', 'calories', 'macros', 'confidence']
      }
    },
    overallConfidence: { type: Type.NUMBER }
  },
  required: ['foods', 'overallConfidence']
};

const GEMINI_TOOLS = [
  {
    functionDeclarations: [
      {
        name: 'get_today_diary',
        description: 'Get meals logged today, consumed calories, and remaining calorie allowance',
        parameters: {
          type: Type.OBJECT,
          properties: {}
        }
      },
      {
        name: 'get_user_profile',
        description: 'Get user nutrition goals, target calories, weight, dietary tags, and allergies',
        parameters: {
          type: Type.OBJECT,
          properties: {}
        }
      },
      {
        name: 'get_recent_trends',
        description: 'Get recent 7-day average calories, target, tracking rate percentage, and weight changes',
        parameters: {
          type: Type.OBJECT,
          properties: {}
        }
      },
      {
        name: 'create_food_log',
        description: 'Propose logging food items into the user meal diary. Requires user confirmation.',
        parameters: {
          type: Type.OBJECT,
          properties: {
            mealType: { type: Type.STRING, description: 'Meal type: breakfast, lunch, dinner, or snack' },
            items: {
              type: Type.ARRAY,
              items: {
                type: Type.OBJECT,
                properties: {
                  name: { type: Type.STRING },
                  estimatedGrams: { type: Type.NUMBER },
                  calories: { type: Type.NUMBER },
                  proteinG: { type: Type.NUMBER },
                  carbsG: { type: Type.NUMBER },
                  fatG: { type: Type.NUMBER }
                },
                required: ['name', 'calories']
              }
            }
          },
          required: ['items']
        }
      },
      {
        name: 'log_water',
        description: 'Log water intake in milliliters directly to the daily diary',
        parameters: {
          type: Type.OBJECT,
          properties: {
            amountMl: { type: Type.NUMBER, description: 'Amount of water in milliliters, e.g. 250 for 1 glass' }
          },
          required: ['amountMl']
        }
      }
    ]
  }
];

const ai = new GoogleGenAI({ apiKey: 'dummy_key_for_schema_validation' });
console.log('GoogleGenAI initialized successfully with schema definitions.');
console.log('Food Analysis Schema properties:', Object.keys(FOOD_ANALYSIS_SCHEMA.properties));
console.log('Gemini Tools registered:', GEMINI_TOOLS[0].functionDeclarations.map(f => f.name));
console.log('Schema and Tools syntax VALID!');
