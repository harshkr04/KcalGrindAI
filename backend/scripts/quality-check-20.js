import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import dotenv from 'dotenv';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

dotenv.config({ path: path.join(__dirname, '..', '.env') });

const { GoogleGenAI, Type } = await import('@google/genai');
const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });

const MEAL_PROMPTS = [
  "2 rotis (60g) and 1 small bowl moong dal tadka (150g)",
  "1 plate chicken biryani (300g) with cucumber raita (100g)",
  "2 whole wheat paneer parathas with 100g plain curd",
  "3 steamed idlis (120g) with 150g sambar and 30g coconut chutney",
  "1 bowl vegetable khichdi (250g) with 1 teaspoon desi ghee (5g)",
  "1 medium masala dosa (150g) with 100g sambar and tomato chutney",
  "1 plate kanda poha (150g) with roasted peanuts and lemon",
  "1 bowl rajma masala (180g) with 150g steamed basmati rice",
  "1 medium aloo paratha (100g) with a small pat of butter (5g)",
  "Egg bhurji made with 2 whole eggs, onion, tomato and 2 pavs (70g)"
];

const COACH_PROMPTS = [
  "What are some high-protein vegetarian breakfast options in Indian cuisine?",
  "How can I maintain my calorie deficit on social weekends without feeling deprived?",
  "Can I eat white basmati rice while trying to lose fat?",
  "How much water should I drink daily if my weight is 75 kg and I do moderate workouts?",
  "Can you review my lunch: 1 cup dal, 2 rotis, and a bowl of curd?",
  "What is the difference between BMR and TDEE in simple terms?",
  "Is peanut butter a good food for a weight loss diet?",
  "How can I reduce excess cooking oil in Indian gravies without losing flavor?",
  "What are 5 healthy Indian snacks under 150 calories?",
  "Should I eat back the active calories I burn during gym workouts?"
];

const FOOD_SCHEMA = {
  type: Type.OBJECT,
  properties: {
    foods: {
      type: Type.ARRAY,
      items: {
        type: Type.OBJECT,
        properties: {
          name: { type: Type.STRING },
          calories: { type: Type.NUMBER },
          protein: { type: Type.NUMBER },
          carbs: { type: Type.NUMBER },
          fat: { type: Type.NUMBER }
        },
        required: ["name", "calories", "protein", "carbs", "fat"]
      }
    },
    totalCalories: { type: Type.NUMBER },
    confidence: { type: Type.NUMBER }
  },
  required: ["foods", "totalCalories"]
};

async function evaluateModel(model, type, prompt) {
  const start = Date.now();
  if (type === 'meal') {
    const res = await ai.models.generateContent({
      model,
      contents: `Parse this meal into ingredients, calories and macros: "${prompt}". Return JSON.`,
      config: {
        temperature: 0.1,
        responseMimeType: 'application/json',
        responseSchema: FOOD_SCHEMA
      }
    });
    const duration = Date.now() - start;
    let parsed;
    try {
      parsed = JSON.parse(res.text);
    } catch {
      parsed = { totalCalories: 0, foods: [] };
    }
    return {
      durationMs: duration,
      calories: parsed.totalCalories || parsed.foods?.reduce((a, b) => a + (b.calories || 0), 0) || 0,
      foodCount: parsed.foods?.length || 0,
      sampleFood: parsed.foods?.[0]?.name || 'N/A'
    };
  } else {
    const res = await ai.models.generateContent({
      model,
      contents: `You are Sproutling, a helpful nutrition coach. Answer concisely (2-3 sentences): "${prompt}"`,
      config: { temperature: 0.3 }
    });
    const duration = Date.now() - start;
    return {
      durationMs: duration,
      words: res.text.trim().split(/\s+/).length,
      snippet: res.text.trim().substring(0, 100) + '...'
    };
  }
}

async function evaluateModelWithRetry(model, type, prompt) {
  for (let attempt = 1; attempt <= 6; attempt++) {
    try {
      return await evaluateModel(model, type, prompt);
    } catch (err) {
      if (err.status === 429 || (err.message && err.message.includes('429'))) {
        console.log(`    [Rate limited 429 on ${model}] Waiting 25s before retry (attempt ${attempt}/6)...`);
        await new Promise(r => setTimeout(r, 25000));
      } else {
        throw err;
      }
    }
  }
  return { durationMs: 0, calories: 0, words: 0, sampleFood: 'RateLimit', snippet: 'RateLimit' };
}

async function run20SampleComparison() {
  console.log('================================================================');
  console.log('   KcalGrind 20-Sample AI Quality Check');
  console.log('   Comparing Primary (gemini-2.5-flash) vs Fallback (gemini-3.5-flash-lite)');
  console.log('================================================================\n');

  const results = {
    meals: [],
    coach: []
  };

  console.log('--- Phase 1: 10 Meal Analysis Samples ---');
  for (let i = 0; i < MEAL_PROMPTS.length; i++) {
    const p = MEAL_PROMPTS[i];
    console.log(`[Meal ${i + 1}/10] "${p.substring(0, 40)}..."`);
    const rPrimary = await evaluateModelWithRetry('gemini-2.5-flash', 'meal', p);
    await new Promise(r => setTimeout(r, 8000));
    const rFallback = await evaluateModelWithRetry('gemini-3.5-flash-lite', 'meal', p);
    await new Promise(r => setTimeout(r, 8000));
    results.meals.push({
      index: i + 1,
      prompt: p,
      primary: rPrimary,
      fallback: rFallback
    });
    console.log(`  -> Primary: ${rPrimary.calories} kcal (${rPrimary.durationMs}ms) | Fallback: ${rFallback.calories} kcal (${rFallback.durationMs}ms)`);
  }

  console.log('\n--- Phase 2: 10 AI Coach Guidance Samples ---');
  for (let i = 0; i < COACH_PROMPTS.length; i++) {
    const p = COACH_PROMPTS[i];
    console.log(`[Coach ${i + 1}/10] "${p.substring(0, 40)}..."`);
    const rPrimary = await evaluateModelWithRetry('gemini-2.5-flash', 'coach', p);
    await new Promise(r => setTimeout(r, 8000));
    const rFallback = await evaluateModelWithRetry('gemini-3.5-flash-lite', 'coach', p);
    await new Promise(r => setTimeout(r, 8000));
    results.coach.push({
      index: i + 1,
      prompt: p,
      primary: rPrimary,
      fallback: rFallback
    });
    console.log(`  -> Primary: ${rPrimary.words} words (${rPrimary.durationMs}ms) | Fallback: ${rFallback.words} words (${rFallback.durationMs}ms)`);
  }

  const outPath = path.join(__dirname, '..', '..', '..', 'Docs', 'QUALITY_CHECK_20_SAMPLES.json');
  fs.writeFileSync(outPath, JSON.stringify(results, null, 2), 'utf8');
  console.log(`\n✓ Results written to ${outPath}`);

  // Summary statistics
  const validMealsPrimary = results.meals.filter(m => m.primary.durationMs > 0);
  const validMealsFallback = results.meals.filter(m => m.fallback.durationMs > 0);
  const avgMealTimePrimary = validMealsPrimary.length ? Math.round(validMealsPrimary.reduce((a, b) => a + b.primary.durationMs, 0) / validMealsPrimary.length) : 0;
  const avgMealTimeFallback = validMealsFallback.length ? Math.round(validMealsFallback.reduce((a, b) => a + b.fallback.durationMs, 0) / validMealsFallback.length) : 0;

  const validCoachPrimary = results.coach.filter(c => c.primary.durationMs > 0);
  const validCoachFallback = results.coach.filter(c => c.fallback.durationMs > 0);
  const avgCoachTimePrimary = validCoachPrimary.length ? Math.round(validCoachPrimary.reduce((a, b) => a + b.primary.durationMs, 0) / validCoachPrimary.length) : 0;
  const avgCoachTimeFallback = validCoachFallback.length ? Math.round(validCoachFallback.reduce((a, b) => a + b.fallback.durationMs, 0) / validCoachFallback.length) : 0;

  console.log('\n=== Summary Statistics ===');
  console.log(`Meal parsing latency: Primary avg = ${avgMealTimePrimary}ms | Fallback avg = ${avgMealTimeFallback}ms`);
  console.log(`Coach response latency: Primary avg = ${avgCoachTimePrimary}ms | Fallback avg = ${avgCoachTimeFallback}ms`);
}

run20SampleComparison().catch(console.error);
