import 'dotenv/config';
import fs from 'fs';
import { createClient } from '@supabase/supabase-js';
import Fastify from 'fastify';
import cors from '@fastify/cors';
import axios from 'axios';
import { getApps, initializeApp } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';
import { GoogleGenAI, Type } from '@google/genai';

const LOG_FILE = './backend.log';
export function appendBackendLog(line) {
  try {
    fs.appendFileSync(LOG_FILE, `[${new Date().toISOString()}] ${line}\n`);
  } catch (e) {}
}

const fastify = Fastify({
  logger: true
});

await fastify.register(cors, {
  origin: '*'
});

// Provider Configuration
const AI_PROVIDER = process.env.AI_PROVIDER || (process.env.GEMINI_API_KEY ? 'gemini' : 'nvidia');
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || '';
const NVIDIA_BASE_URL = 'https://integrate.api.nvidia.com/v1';
const NVIDIA_API_KEY = process.env.NVIDIA_API_KEY || '';

// Model Defaults
const GEMINI_MODEL = process.env.GEMINI_MODEL || 'gemini-2.5-flash';
const NVIDIA_DEFAULT_MODEL = 'meta/llama-3.2-11b-vision-instruct';

const VISION_MODEL = AI_PROVIDER === 'gemini' ? GEMINI_MODEL : (process.env.VISION_MODEL || NVIDIA_DEFAULT_MODEL);
const TEXT_MODEL = AI_PROVIDER === 'gemini' ? GEMINI_MODEL : (process.env.TEXT_MODEL || NVIDIA_DEFAULT_MODEL);
const CHAT_MODEL = AI_PROVIDER === 'gemini' ? GEMINI_MODEL : (process.env.CHAT_MODEL || NVIDIA_DEFAULT_MODEL);

// Initialize Google GenAI client if key is present
let geminiClient = null;
if (GEMINI_API_KEY) {
  try {
    geminiClient = new GoogleGenAI({ apiKey: GEMINI_API_KEY });
    fastify.log.info({ provider: 'gemini', model: VISION_MODEL }, 'Google GenAI (Gemini 2.5 Flash) initialized');
  } catch (err) {
    fastify.log.error({ err: err.message }, 'Failed to initialize Google GenAI');
  }
} else {
  fastify.log.info('GEMINI_API_KEY not provided, Gemini provider will use NVIDIA fallback if needed');
}

// -------------------------------------------------------------
// Gemini Schemas & Tools
// -------------------------------------------------------------
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
        name: 'search_food',
        description: 'Look up nutritional information when user asks "how many calories in X" without wanting to log it.',
        parameters: {
          type: Type.OBJECT,
          properties: {
            query: { type: Type.STRING, description: 'Food name to search' }
          },
          required: ['query']
        }
      },
      {
        name: 'create_food_log',
        description: 'Log or add food/meal to the user daily diary. Call this whenever user asks to log, track, record, or add food or meal.',
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

// -------------------------------------------------------------
// NVIDIA Helper (Fallback & Multi-Provider Support)
// -------------------------------------------------------------
async function callNvidiaChat(messages, model, temperature = 0.2, tools = null) {
  if (!NVIDIA_API_KEY) {
    throw new Error('NVIDIA_API_KEY is not configured in backend/.env');
  }

  const payload = {
    model,
    messages,
    temperature,
    max_tokens: 1024,
    response_format: { type: 'json_object' }
  };

  if (tools && tools.length > 0) {
    payload.tools = tools;
  }

  const response = await axios.post(`${NVIDIA_BASE_URL}/chat/completions`, payload, {
    headers: {
      'Authorization': `Bearer ${NVIDIA_API_KEY}`,
      'Content-Type': 'application/json'
    },
    timeout: 60000
  });

  return response.data;
}

function extractJsonFromContent(content) {
  if (!content) return null;
  try {
    return JSON.parse(content);
  } catch {
    const match = content.match(/\{[\s\S]*\}/);
    if (match) {
      try {
        return JSON.parse(match[0]);
      } catch {
        return null;
      }
    }
    return null;
  }
}

// -------------------------------------------------------------
// Firebase Admin & Rate Limiter
// -------------------------------------------------------------
const projectId = process.env.FIREBASE_PROJECT_ID || 'lumina-nutrition-app';
if (!getApps().length) {
  initializeApp({
    projectId
  });
  fastify.log.info({ projectId }, 'Firebase Admin SDK initialized');
}

const rateLimits = new Map();
const RATE_LIMIT_WINDOW_MS = 60 * 60 * 1000;
const RATE_LIMIT_MAX_CALLS = 30;

function checkRateLimit(uid) {
  const now = Date.now();
  const windowStart = now - RATE_LIMIT_WINDOW_MS;
  const timestamps = (rateLimits.get(uid) || []).filter(t => t > windowStart);

  if (timestamps.length >= RATE_LIMIT_MAX_CALLS) {
    const oldest = timestamps[0];
    const resetSeconds = Math.ceil((oldest + RATE_LIMIT_WINDOW_MS - now) / 1000);
    return { allowed: false, remaining: 0, resetSeconds };
  }

  timestamps.push(now);
  rateLimits.set(uid, timestamps);
  return { allowed: true, remaining: RATE_LIMIT_MAX_CALLS - timestamps.length, resetSeconds: 0 };
}

// Global preHandler hook: gate all AI routes with Firebase Token verification
fastify.addHook('preHandler', async (request, reply) => {
  if (request.url === '/health' || request.url === '/') {
    return;
  }

  const authHeader = request.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    const reason = authHeader ? 'Header does not start with Bearer' : 'Authorization header is completely missing';
    appendBackendLog(`401 UNAUTHORIZED on ${request.method} ${request.url} - Reason: ${reason} (Header: ${authHeader ? authHeader.substring(0, 20) : 'null'})`);
    fastify.log.warn({ url: request.url, hasHeader: !!authHeader, headerVal: authHeader ? authHeader.substring(0, 15) : null }, 'Rejected request: Missing or malformed Authorization header');
    return reply.code(401).send({
      error: 'Unauthorized',
      message: 'Missing or invalid Authorization header. A valid Firebase ID token is required.',
      details: reason
    });
  }

  const idToken = authHeader.split('Bearer ')[1].trim();


  try {
    const decodedToken = await getAuth().verifyIdToken(idToken);
    request.user = decodedToken;

    const rateStatus = checkRateLimit(decodedToken.uid);
    reply.header('X-RateLimit-Limit', RATE_LIMIT_MAX_CALLS);
    reply.header('X-RateLimit-Remaining', rateStatus.remaining);

    if (!rateStatus.allowed) {
      reply.header('Retry-After', rateStatus.resetSeconds);
      return reply.code(429).send({
        error: 'Too Many Requests',
        message: `Hourly AI limit reached. Please try again in ${rateStatus.resetSeconds} seconds.`
      });
    }
  } catch (error) {
    appendBackendLog(`401 UNAUTHORIZED on ${request.method} ${request.url} - verifyIdToken error: [${error.code}] ${error.message}`);
    fastify.log.warn({ url: request.url, err: error.message, code: error.code }, 'Rejected request: getAuth().verifyIdToken failed');
    return reply.code(401).send({
      error: 'Unauthorized',
      message: 'Invalid or expired Firebase ID token.',
      details: error.message,
      code: error.code
    });
  }
});

fastify.addHook('onResponse', async (request, reply) => {
  if (request.url !== '/health' && request.url !== '/') {
    appendBackendLog(`RESPONSE: ${request.method} ${request.url} -> Status ${reply.statusCode}`);
  }
});

// -------------------------------------------------------------
// 1. Health check & provider info
// -------------------------------------------------------------
fastify.get('/health', async () => {
  return {
    status: 'ok',
    service: 'lumina-ai-backend',
    provider: geminiClient ? 'gemini' : (NVIDIA_API_KEY ? 'nvidia' : 'unconfigured'),
    visionModel: VISION_MODEL,
    chatModel: CHAT_MODEL,
    timestamp: new Date().toISOString()
  };
});

// -------------------------------------------------------------
// 2. POST /ai/analyze-photo
// -------------------------------------------------------------
fastify.post('/ai/analyze-photo', async (request, reply) => {
  const { imageBase64, dietTags = [], allergies = [] } = request.body || {};

  if (!imageBase64) {
    return reply.code(400).send({ error: 'imageBase64 is required' });
  }

  fastify.log.info({ tags: dietTags, allergiesCount: allergies.length, provider: geminiClient ? 'gemini' : 'nvidia' }, 'Analyzing photo');

  // Attempt with Gemini 2.5 Flash if client is active
  if (geminiClient) {
    try {
      let mimeType = 'image/jpeg';
      let cleanBase64 = imageBase64;
      if (imageBase64.startsWith('data:')) {
        const match = imageBase64.match(/^data:(image\/[a-zA-Z0-9+.-]+);base64,(.+)$/);
        if (match) {
          mimeType = match[1];
          cleanBase64 = match[2];
        } else {
          cleanBase64 = imageBase64.split(';base64,')[1] || imageBase64;
        }
      }

      const prompt = `You are Lumina Nutrition Vision AI.
Analyze this meal image.
Identify each food/ingredient, estimate grams (estimatedGrams), calories (rounded to nearest 5-10 kcal), and macros (protein, carbs, fat in grams).
Assign an honest confidence score (0.0 to 1.0) per food item reflecting visual certainty. For ambiguous food, assign lower confidence (0.35-0.70). For clear identifiable food, assign higher confidence (0.85-0.98).
Compute overallConfidence (0.0 to 1.0).
User dietary tags: ${dietTags.join(', ') || 'None'}. Allergies: ${allergies.join(', ') || 'None'}.`;

      const response = await geminiClient.models.generateContent({
        model: VISION_MODEL,
        contents: [
          {
            role: 'user',
            parts: [
              { text: prompt },
              {
                inlineData: {
                  data: cleanBase64,
                  mimeType
                }
              }
            ]
          }
        ],
        config: {
          temperature: 0.1,
          responseMimeType: 'application/json',
          responseSchema: FOOD_ANALYSIS_SCHEMA
        }
      });

      const parsed = JSON.parse(response.text);
      if (parsed && Array.isArray(parsed.foods)) {
        return parsed;
      }
    } catch (geminiError) {
      fastify.log.warn({ err: geminiError.message }, 'Gemini vision failed, attempting fallback');
    }
  }

  // Fallback to NVIDIA Vision
  if (NVIDIA_API_KEY) {
    try {
      const formattedImage = imageBase64.startsWith('data:') 
        ? imageBase64 
        : `data:image/jpeg;base64,${imageBase64}`;

      const systemPrompt = `You are Lumina Nutrition Vision AI.
Analyze the user's meal image and output a structured JSON object.
Rules:
- Identify each distinct food/ingredient.
- Estimate realistic weight in grams (estimatedGrams).
- Estimate calories rounded to nearest 5-10 kcal.
- Estimate macros (protein, carbs, fat) in grams.
- Assign an honest confidence score (0.0 to 1.0) per food item.
- Compute overallConfidence (0.0 to 1.0).
- Output ONLY valid JSON:
{
  "foods": [{ "name": "Food", "estimatedGrams": 150, "calories": 250, "macros": { "protein": 25, "carbs": 10, "fat": 12 }, "confidence": 0.90 }],
  "overallConfidence": 0.88
}`;

      const messages = [
        { role: 'system', content: systemPrompt },
        {
          role: 'user',
          content: [
            { type: 'text', text: 'Identify the foods and estimate nutrition in this meal.' },
            { type: 'image_url', image_url: { url: formattedImage } }
          ]
        }
      ];

      const result = await callNvidiaChat(messages, 'meta/llama-3.2-11b-vision-instruct', 0.1);
      if (result && result.choices && result.choices[0]?.message?.content) {
        const rawText = result.choices[0].message.content;
        const parsed = extractJsonFromContent(rawText);
        if (parsed && Array.isArray(parsed.foods) && parsed.foods.length > 0) {
          return parsed;
        }
      }
    } catch (error) {
      fastify.log.error(error?.response?.data || error.message);
    }
  }

  return reply.code(502).send({
    error: 'AI vision model unavailable. Please check API key configuration.'
  });
});

// -------------------------------------------------------------
// 3. POST /ai/analyze-text
// -------------------------------------------------------------
fastify.post('/ai/analyze-text', async (request, reply) => {
  const { text, dietTags = [], allergies = [] } = request.body || {};

  if (!text || !text.trim()) {
    return reply.code(400).send({ error: 'text is required' });
  }

  fastify.log.info({ text, provider: geminiClient ? 'gemini' : 'nvidia' }, 'Analyzing text meal description');

  // Attempt with Gemini 2.5 Flash
  if (geminiClient) {
    try {
      const prompt = `You are Lumina Nutrition Text Parser AI.
Analyze the user's meal description: "${text}".
Identify each food item mentioned with portion sizes.
Estimate reasonable grams, calories (rounded to nearest 5-10 kcal), and macros (protein, carbs, fat in grams).
Assign an honest confidence score (0.0 to 1.0) per item:
- High (0.85 - 0.99): clear, unambiguous food names and exact portions (e.g. "2 boiled eggs and 1 slice toast").
- Medium (0.50 - 0.84): moderately specific (e.g. "some pasta with red sauce").
- Low (0.10 - 0.49): extremely vague or ambiguous.
Compute overallConfidence as the average confidence.
User dietary tags: ${dietTags.join(', ') || 'None'}. Allergies: ${allergies.join(', ') || 'None'}.`;

      const response = await geminiClient.models.generateContent({
        model: TEXT_MODEL,
        contents: [
          {
            role: 'user',
            parts: [{ text: prompt }]
          }
        ],
        config: {
          temperature: 0.1,
          responseMimeType: 'application/json',
          responseSchema: FOOD_ANALYSIS_SCHEMA
        }
      });

      const parsed = JSON.parse(response.text);
      if (parsed && Array.isArray(parsed.foods) && parsed.foods.length > 0) {
        return parsed;
      }
    } catch (geminiError) {
      fastify.log.warn({ err: geminiError.message }, 'Gemini text parser failed, attempting fallback');
    }
  }

  // Fallback to NVIDIA
  if (NVIDIA_API_KEY) {
    try {
      const systemPrompt = `You are Lumina Nutrition Text Parser AI.
Analyze the user's meal description and output a structured JSON object.
Rules:
- Extract all food items mentioned with quantity/portions.
- Estimate reasonable grams (estimatedGrams).
- Estimate calories rounded to nearest 5-10 kcal.
- Estimate macros (protein, carbs, fat) in grams.
- Assign an honest confidence score (0.0 to 1.0) per item.
- Compute overallConfidence.
- Output ONLY valid JSON matching:
{
  "foods": [{ "name": "Food", "estimatedGrams": 100, "calories": 150, "macros": { "protein": 12, "carbs": 15, "fat": 5 }, "confidence": 0.92 }],
  "overallConfidence": 0.90
}`;

      const messages = [
        { role: 'system', content: systemPrompt },
        { role: 'user', content: `Meal description: "${text}"` }
      ];

      const result = await callNvidiaChat(messages, 'meta/llama-3.2-11b-vision-instruct', 0.1);
      if (result && result.choices && result.choices[0]?.message?.content) {
        const parsed = extractJsonFromContent(result.choices[0].message.content);
        if (parsed && Array.isArray(parsed.foods) && parsed.foods.length > 0) {
          return parsed;
        }
      }
    } catch (error) {
      fastify.log.error(error?.response?.data || error.message);
    }
  }

  return reply.code(502).send({
    error: 'AI text analysis unavailable. Please check API key configuration.'
  });
});

// -------------------------------------------------------------
// 4. POST /ai/transcribe
// -------------------------------------------------------------
fastify.post('/ai/transcribe', async (request, reply) => {
  const { transcript, text, dietTags = [], allergies = [] } = request.body || {};
  const query = transcript || text;

  if (!query || !query.trim()) {
    return reply.code(400).send({ error: 'transcript is required' });
  }

  return fastify.inject({
    method: 'POST',
    url: '/ai/analyze-text',
    headers: {
      authorization: request.headers.authorization || ''
    },
    payload: { text: query, dietTags, allergies }
  }).then(res => JSON.parse(res.body));
});

// -------------------------------------------------------------
// Helper: Process Tool Execution for AI Coach
// -------------------------------------------------------------
function executeTool(fnName, fnArgs, userContext) {
  let toolOutput = {};
  let suggestedAction = null;
  let items = [];
  let mealType = 'snack';

  if (fnName === 'get_today_diary') {
    toolOutput = {
      targetCalories: userContext?.targetCalories || 2000,
      consumedCalories: userContext?.consumedCalories || 0,
      remainingCalories: userContext?.remainingCalories ?? 2000,
      meals: userContext?.meals || []
    };
  } else if (fnName === 'get_user_profile') {
    toolOutput = {
      goal: userContext?.goal || 'Maintain weight',
      targetCalories: userContext?.targetCalories || 2000,
      dietTags: userContext?.dietTags || [],
      allergies: userContext?.allergies || []
    };
  } else if (fnName === 'get_recent_trends') {
    toolOutput = userContext?.recentTrends || {
      avgCalories: userContext?.consumedCalories || 1850,
      targetCalories: userContext?.targetCalories || 2000,
      daysTracked: 6,
      totalDays: 7,
      trackingRatePercent: 85,
      avgProteinG: 85.0,
      avgCarbsG: 190.0,
      avgFatG: 55.0,
      weightChangeKg: -0.4
    };
  } else if (fnName === 'search_food') {
    toolOutput = {
      query: fnArgs.query || 'food',
      results: [
        { name: fnArgs.query || 'Food Item', calories: 150, proteinG: 5.0, carbsG: 20.0, fatG: 4.0 }
      ]
    };
  } else if (fnName === 'create_food_log') {
    let rawItems = fnArgs.items || [];
    if (typeof rawItems === 'string') {
      try {
        rawItems = JSON.parse(rawItems);
      } catch (e) {
        rawItems = [];
      }
    }
    if (!Array.isArray(rawItems)) {
      rawItems = rawItems ? [rawItems] : [];
    }
    mealType = fnArgs.mealType || 'snack';
    items = rawItems.map(it => ({
      name: it.name || 'Food Item',
      estimatedGrams: Number(it.estimatedGrams) || 100.0,
      calories: Number(it.calories) || 100,
      macros: {
        protein: Number(it.proteinG || it.macros?.protein) || 0.0,
        carbs: Number(it.carbsG || it.macros?.carbs) || 0.0,
        fat: Number(it.fatG || it.macros?.fat) || 0.0
      },
      confidence: Number(it.confidence) || 0.95
    }));
    toolOutput = {
      status: 'PROPOSED',
      message: 'Food log proposed for user confirmation',
      mealType,
      itemsCount: items.length
    };
    suggestedAction = JSON.stringify({
      type: 'create_food_log',
      action: 'confirm_food_log',
      data: {
        mealType,
        items
      }
    });
  } else if (fnName === 'log_water') {
    const amountMl = Number(fnArgs.amountMl) || 250;
    toolOutput = {
      status: 'SUCCESS',
      amountMl,
      message: `Logged ${amountMl}ml of water to your daily diary.`
    };
    suggestedAction = JSON.stringify({
      type: 'log_water',
      action: 'auto_logged_water',
      data: {
        amountMl
      }
    });
  }

  return { toolOutput, suggestedAction, items, mealType };
}

function buildDefaultReply(fnName, toolOutput, mealType, items) {
  if (fnName === 'create_food_log') {
    const foodNames = (items || []).map(i => i.name).join(', ') || 'food';
    const totalCals = (items || []).reduce((acc, it) => acc + (it.calories || 0), 0);
    return `I've prepared a ${mealType} log for ${foodNames} (${totalCals} kcal). Please review and tap 'Confirm & Log' below to add it to your diary!`;
  } else if (fnName === 'log_water') {
    return `I've logged ${toolOutput.amountMl || 250}ml of water to your daily diary!`;
  } else if (fnName === 'get_recent_trends') {
    return `Over the past week, you've tracked ${toolOutput.daysTracked} of ${toolOutput.totalDays} days with an average of ${Math.round(toolOutput.avgCalories)} kcal/day.`;
  } else if (fnName === 'get_today_diary') {
    return `Today you've consumed ${toolOutput.consumedCalories} kcal of your ${toolOutput.targetCalories} kcal goal, with ${toolOutput.remainingCalories} kcal remaining.`;
  } else if (fnName === 'get_user_profile') {
    return `Your current goal is ${toolOutput.goal} with a daily target of ${toolOutput.targetCalories} kcal.`;
  }
  return `Here is the requested information.`;
}

// -------------------------------------------------------------
// 5. POST /ai/chat
// -------------------------------------------------------------
fastify.post('/ai/chat', async (request, reply) => {
  const { messages = [], userContext = {} } = request.body || {};

  if (!messages || messages.length === 0) {
    return reply.code(400).send({ error: 'messages array is required' });
  }

  fastify.log.info({ messageCount: messages.length, provider: geminiClient ? 'gemini' : 'nvidia' }, 'Processing AI Coach Chat');

  const systemInstruction = `You are Lumina Nutrition AI Coach.
You provide intelligent, empathetic, evidence-based nutrition coaching.
User Context:
- Goal: ${userContext?.goal || userContext?.profile?.goal || 'Maintain'}
- Target Calories: ${userContext?.targetCalories || userContext?.profile?.targetCalories || 2000} kcal
- Consumed Calories Today: ${userContext?.consumedCalories ?? 0} kcal
- Remaining Calories Today: ${userContext?.remainingCalories ?? 2000} kcal
- Recent Trends: ${JSON.stringify(userContext?.recentTrends || { avgCalories: 1850, targetCalories: 2000, trackingRatePercent: 85, daysTracked: 6 })}

Guidelines:
- If asked about diary, remaining calories, meals logged today, or user profile, ALWAYS call get_today_diary or get_user_profile.
- If asked how the user is doing this week or recent trends/progress, ALWAYS call get_recent_trends.
- If the user asks to log, track, record, or add a meal or food (e.g. "log a banana for snack", "log 2 eggs"), ALWAYS call create_food_log with estimated calories and macros. Do NOT call search_food when the user explicitly asks to log.
- If the user asks to log water (e.g. "log a glass of water"), ALWAYS call log_water with amountMl (default 250ml per glass).
- Answer warmly, concisely, and practically.
- Ground your answers strictly in the tool data.`;

  // 1. Attempt with Gemini 2.5 Flash
  if (geminiClient) {
    try {
      // Format messages for Gemini (role 'user' | 'model')
      const contents = messages.map(m => ({
        role: m.role === 'assistant' ? 'model' : 'user',
        parts: [{ text: m.content || '' }]
      }));

      const geminiResponse = await geminiClient.models.generateContent({
        model: CHAT_MODEL,
        contents,
        config: {
          systemInstruction,
          temperature: 0.3,
          tools: GEMINI_TOOLS
        }
      });

      const functionCalls = geminiResponse.functionCalls;
      if (functionCalls && functionCalls.length > 0) {
        const call = functionCalls[0];
        const fnName = call.name;
        const fnArgs = call.args || {};
        fastify.log.info({ fnName, fnArgs }, 'Gemini triggered function call');

        const { toolOutput, suggestedAction, items, mealType } = executeTool(fnName, fnArgs, userContext);
        const finalReply = buildDefaultReply(fnName, toolOutput, mealType, items);

        return {
          reply: finalReply,
          toolInvoked: fnName,
          toolData: toolOutput,
          suggestedAction
        };
      }

      if (geminiResponse.text) {
        return {
          reply: geminiResponse.text.trim(),
          suggestedAction: null
        };
      }
    } catch (geminiError) {
      fastify.log.warn({ err: geminiError.message }, 'Gemini chat call failed, attempting fallback');
    }
  }

  // 2. Fallback to NVIDIA Chat
  if (NVIDIA_API_KEY) {
    const tools = [
      {
        type: 'function',
        function: {
          name: 'get_today_diary',
          description: 'Get meals logged today, consumed calories, and remaining calorie allowance',
          parameters: { type: 'object', properties: {} }
        }
      },
      {
        type: 'function',
        function: {
          name: 'get_user_profile',
          description: 'Get user nutrition goals, target calories, weight, dietary tags, and allergies',
          parameters: { type: 'object', properties: {} }
        }
      },
      {
        type: 'function',
        function: {
          name: 'get_recent_trends',
          description: 'Get recent 7-day average calories, target, tracking rate percentage, and weight changes',
          parameters: { type: 'object', properties: {} }
        }
      },
      {
        type: 'function',
        function: {
          name: 'create_food_log',
          description: 'Propose logging food items into the user meal diary. Requires user confirmation.',
          parameters: {
            type: 'object',
            properties: {
              mealType: { type: 'string', enum: ['breakfast', 'lunch', 'dinner', 'snack'] },
              items: {
                type: 'array',
                items: {
                  type: 'object',
                  properties: {
                    name: { type: 'string' },
                    estimatedGrams: { type: 'number' },
                    calories: { type: 'number' },
                    proteinG: { type: 'number' },
                    carbsG: { type: 'number' },
                    fatG: { type: 'number' }
                  },
                  required: ['name', 'calories']
                }
              }
            },
            required: ['items']
          }
        }
      },
      {
        type: 'function',
        function: {
          name: 'log_water',
          description: 'Log water intake in milliliters directly to the daily diary',
          parameters: {
            type: 'object',
            properties: {
              amountMl: { type: 'number', description: 'Amount of water in milliliters, e.g. 250 for 1 glass' }
            },
            required: ['amountMl']
          }
        }
      }
    ];

    const chatMessages = [
      { role: 'system', content: systemInstruction },
      ...messages
    ];

    try {
      const turn1Result = await callNvidiaChat(chatMessages, 'meta/llama-3.2-11b-vision-instruct', 0.4, tools);
      const choice = turn1Result?.choices?.[0];
      
      let toolCall = choice?.message?.tool_calls?.[0];
      if (!toolCall && choice?.message?.content) {
        try {
          const rawContent = choice.message.content.trim();
          if (rawContent.startsWith('{') && (rawContent.includes('"name"') || rawContent.includes('"function"'))) {
            const parsedTool = JSON.parse(rawContent);
            const fnName = parsedTool.name || parsedTool.function;
            if (fnName) {
              toolCall = {
                id: 'call_synth_' + Date.now(),
                function: {
                  name: fnName,
                  arguments: JSON.stringify(parsedTool.parameters || parsedTool.arguments || {})
                }
              };
            }
          }
        } catch (e) {}
      }

      if (toolCall) {
        const fnName = toolCall.function.name;
        let fnArgs = {};
        try {
          fnArgs = JSON.parse(toolCall.function.arguments || '{}');
        } catch {
          fnArgs = {};
        }

        const { toolOutput, suggestedAction, items, mealType } = executeTool(fnName, fnArgs, userContext);
        const finalReply = buildDefaultReply(fnName, toolOutput, mealType, items);

        return {
          reply: finalReply,
          toolInvoked: fnName,
          toolData: toolOutput,
          suggestedAction
        };
      }

      if (choice?.message?.content) {
        return {
          reply: choice.message.content.trim(),
          suggestedAction: null
        };
      }
    } catch (error) {
      fastify.log.error(error?.response?.data || error.message);
    }
  }

  return reply.code(502).send({
    error: 'AI Coach service unavailable. Please check API key configuration.'
  });
});

// -------------------------------------------------------------
// Supabase Client (Phase 10 — Cloud Sync)
// -------------------------------------------------------------
let supabase = null;
if (process.env.SUPABASE_URL && process.env.SUPABASE_SERVICE_KEY) {
  supabase = createClient(
    process.env.SUPABASE_URL,
    process.env.SUPABASE_SERVICE_KEY
  );
  fastify.log.info('Supabase client initialized for cloud sync');
} else {
  fastify.log.warn('SUPABASE_URL or SUPABASE_SERVICE_KEY not set — sync routes will return 503');
}

// Helper: convert epoch millis to ISO string for Supabase timestamptz
function epochToIso(epochMs) {
  if (!epochMs) return new Date().toISOString();
  return new Date(epochMs).toISOString();
}

// Helper: convert ISO string back to epoch millis
function isoToEpoch(isoStr) {
  if (!isoStr) return Date.now();
  return new Date(isoStr).getTime();
}

// -------------------------------------------------------------
// POST /sync/push — batch upsert local Room data into Supabase
// Last-write-wins via updated_at/logged_at comparison
// -------------------------------------------------------------
fastify.post('/sync/push', async (request, reply) => {
  if (!supabase) {
    return reply.code(503).send({ error: 'Supabase not configured' });
  }

  const uid = request.user.uid;
  const body = request.body || {};
  const summary = { pushed: {}, skipped: {}, errors: {} };

  try {
    // 1. User Profile
    if (body.user_profile) {
      const p = body.user_profile;
      const { data: existing } = await supabase
        .from('user_profiles')
        .select('updated_at')
        .eq('firebase_uid', uid)
        .maybeSingle();

      const incomingTs = epochToIso(p.updatedAt);
      if (!existing || new Date(incomingTs) >= new Date(existing.updated_at)) {
        const { error } = await supabase.from('user_profiles').upsert({
          firebase_uid: uid,
          email: p.email || null,
          goal: p.goal,
          units: p.units,
          age: p.age,
          height_cm: p.heightCm,
          weight_kg: p.weightKg,
          goal_weight_kg: p.goalWeightKg,
          activity_level: p.activityLevel,
          diet_tags: p.dietTags || [],
          allergies: p.allergies || [],
          targets_source: p.targetsSource,
          created_at: epochToIso(p.createdAt),
          updated_at: incomingTs
        }, { onConflict: 'firebase_uid' });
        if (error) { summary.errors.user_profile = error.message; }
        else { summary.pushed.user_profile = 1; }
      } else {
        summary.skipped.user_profile = 1;
      }
    }

    // 2. Nutrition Goal
    if (body.nutrition_goal) {
      const g = body.nutrition_goal;
      const { data: existing } = await supabase
        .from('nutrition_goals')
        .select('updated_at')
        .eq('firebase_uid', uid)
        .maybeSingle();

      const incomingTs = epochToIso(g.updatedAt);
      if (!existing || new Date(incomingTs) >= new Date(existing.updated_at)) {
        // Ensure user_profiles row exists first
        const { error } = await supabase.from('nutrition_goals').upsert({
          firebase_uid: uid,
          calories: g.calories,
          protein_g: g.proteinG,
          carbs_g: g.carbsG,
          fat_g: g.fatG,
          water_liters: g.waterLiters,
          water_glasses: g.waterGlasses,
          bmr: g.bmr || null,
          tdee: g.tdee || null,
          is_custom: g.isCustom || false,
          updated_at: incomingTs
        }, { onConflict: 'firebase_uid' });
        if (error) { summary.errors.nutrition_goal = error.message; }
        else { summary.pushed.nutrition_goal = 1; }
      } else {
        summary.skipped.nutrition_goal = 1;
      }
    }

    // 3. Meal Logs + Food Log Items
    if (Array.isArray(body.meal_logs) && body.meal_logs.length > 0) {
      let pushedMeals = 0, skippedMeals = 0;
      let pushedItems = 0;

      for (const meal of body.meal_logs) {
        // Check if this local_id already exists for this user
        const { data: existing } = await supabase
          .from('meal_logs')
          .select('id, logged_at')
          .eq('firebase_uid', uid)
          .eq('local_id', meal.id)
          .maybeSingle();

        const incomingTs = epochToIso(meal.loggedAt);

        if (existing && new Date(incomingTs) < new Date(existing.logged_at)) {
          skippedMeals++;
          continue;
        }

        let cloudMealId;
        if (existing) {
          // Update existing
          const { error } = await supabase.from('meal_logs')
            .update({
              log_date: meal.date,
              meal_type: meal.mealType,
              total_calories: meal.totalCalories,
              logged_at: incomingTs,
              source: meal.source,
              synced: true
            })
            .eq('id', existing.id);
          if (error) { summary.errors.meal_logs = error.message; continue; }
          cloudMealId = existing.id;
        } else {
          // Insert new
          const { data: inserted, error } = await supabase.from('meal_logs')
            .insert({
              firebase_uid: uid,
              local_id: meal.id,
              log_date: meal.date,
              meal_type: meal.mealType,
              total_calories: meal.totalCalories,
              logged_at: incomingTs,
              source: meal.source,
              synced: true
            })
            .select('id')
            .single();
          if (error) { summary.errors.meal_logs = error.message; continue; }
          cloudMealId = inserted.id;
        }
        pushedMeals++;

        // Push food log items for this meal
        const items = (body.food_log_items || []).filter(i => i.mealLogId === meal.id);
        if (items.length > 0) {
          // Delete existing items for this cloud meal, re-insert
          await supabase.from('food_log_items').delete().eq('meal_log_id', cloudMealId);

          const itemRows = items.map(item => ({
            meal_log_id: cloudMealId,
            local_id: item.id,
            local_meal_log_id: item.mealLogId,
            food_id: item.foodId || null,
            name: item.name,
            brand: item.brand || null,
            serving_description: item.servingDescription,
            serving_grams: item.servingGrams,
            calories: item.calories,
            protein_g: item.proteinG,
            carbs_g: item.carbsG,
            fat_g: item.fatG,
            fiber_g: item.fiberG,
            source: item.source,
            confidence: item.confidence || null,
            confirmed: item.confirmed || false
          }));

          const { error: itemError } = await supabase
            .from('food_log_items')
            .insert(itemRows);
          if (itemError) { summary.errors.food_log_items = itemError.message; }
          else { pushedItems += items.length; }
        }
      }
      summary.pushed.meal_logs = pushedMeals;
      summary.pushed.food_log_items = pushedItems;
      if (skippedMeals > 0) summary.skipped.meal_logs = skippedMeals;
    }

    // 4. Weight Entries
    if (Array.isArray(body.weight_entries) && body.weight_entries.length > 0) {
      let pushed = 0, skipped = 0;
      for (const entry of body.weight_entries) {
        const { data: existing } = await supabase
          .from('weight_entries')
          .select('id, logged_at')
          .eq('firebase_uid', uid)
          .eq('local_id', entry.id)
          .maybeSingle();

        const incomingTs = epochToIso(entry.loggedAt);
        if (existing && new Date(incomingTs) < new Date(existing.logged_at)) {
          skipped++;
          continue;
        }

        if (existing) {
          const { error } = await supabase.from('weight_entries')
            .update({ weight_kg: entry.weightKg, log_date: entry.date, note: entry.note || null, logged_at: incomingTs })
            .eq('id', existing.id);
          if (error) { summary.errors.weight_entries = error.message; continue; }
        } else {
          const { error } = await supabase.from('weight_entries')
            .insert({ firebase_uid: uid, local_id: entry.id, weight_kg: entry.weightKg, log_date: entry.date, note: entry.note || null, logged_at: incomingTs });
          if (error) { summary.errors.weight_entries = error.message; continue; }
        }
        pushed++;
      }
      summary.pushed.weight_entries = pushed;
      if (skipped > 0) summary.skipped.weight_entries = skipped;
    }

    // 5. Water Logs
    if (Array.isArray(body.water_logs) && body.water_logs.length > 0) {
      let pushed = 0, skipped = 0;
      for (const log of body.water_logs) {
        const { data: existing } = await supabase
          .from('water_logs')
          .select('id, logged_at')
          .eq('firebase_uid', uid)
          .eq('local_id', log.id)
          .maybeSingle();

        const incomingTs = epochToIso(log.loggedAt);
        if (existing && new Date(incomingTs) < new Date(existing.logged_at)) {
          skipped++;
          continue;
        }

        if (existing) {
          const { error } = await supabase.from('water_logs')
            .update({ log_date: log.date, amount_ml: log.amountMl, logged_at: incomingTs })
            .eq('id', existing.id);
          if (error) { summary.errors.water_logs = error.message; continue; }
        } else {
          const { error } = await supabase.from('water_logs')
            .insert({ firebase_uid: uid, local_id: log.id, log_date: log.date, amount_ml: log.amountMl, logged_at: incomingTs });
          if (error) { summary.errors.water_logs = error.message; continue; }
        }
        pushed++;
      }
      summary.pushed.water_logs = pushed;
      if (skipped > 0) summary.skipped.water_logs = skipped;
    }

    // 6. AI Analyses (push-only, no pull needed)
    if (Array.isArray(body.ai_analyses) && body.ai_analyses.length > 0) {
      let pushed = 0;
      for (const analysis of body.ai_analyses) {
        const { data: existing } = await supabase
          .from('ai_analysis_log')
          .select('id')
          .eq('firebase_uid', uid)
          .eq('local_id', analysis.id)
          .maybeSingle();

        if (!existing) {
          let resultJson = analysis.resultJson;
          if (typeof resultJson === 'string') {
            try { resultJson = JSON.parse(resultJson); } catch { resultJson = { raw: resultJson }; }
          }

          const { error } = await supabase.from('ai_analysis_log').insert({
            firebase_uid: uid,
            local_id: analysis.id,
            input_type: analysis.inputType,
            raw_input_ref: analysis.rawInputRef || null,
            result_json: resultJson,
            overall_confidence: analysis.overallConfidence || null,
            created_at: epochToIso(analysis.createdAt)
          });
          if (error) { summary.errors.ai_analyses = error.message; continue; }
          pushed++;
        }
      }
      summary.pushed.ai_analyses = pushed;
    }

    // 7. Conversations + Messages
    if (Array.isArray(body.conversations) && body.conversations.length > 0) {
      let pushedConvs = 0, pushedMsgs = 0;
      for (const conv of body.conversations) {
        const { data: existing } = await supabase
          .from('conversations')
          .select('id, last_message_at')
          .eq('firebase_uid', uid)
          .eq('local_id', conv.id)
          .maybeSingle();

        const incomingTs = epochToIso(conv.lastMessageAt);
        let cloudConvId;

        if (existing) {
          if (new Date(incomingTs) >= new Date(existing.last_message_at)) {
            await supabase.from('conversations')
              .update({ last_message_at: incomingTs })
              .eq('id', existing.id);
          }
          cloudConvId = existing.id;
        } else {
          const { data: inserted, error } = await supabase.from('conversations')
            .insert({
              firebase_uid: uid,
              local_id: conv.id,
              started_at: epochToIso(conv.startedAt),
              last_message_at: incomingTs
            })
            .select('id')
            .single();
          if (error) { summary.errors.conversations = error.message; continue; }
          cloudConvId = inserted.id;
          pushedConvs++;
        }

        // Push messages for this conversation
        const msgs = (body.messages || []).filter(m => m.conversationId === conv.id);
        for (const msg of msgs) {
          const { data: existingMsg } = await supabase
            .from('messages')
            .select('id')
            .eq('local_id', msg.id)
            .eq('conversation_id', cloudConvId)
            .maybeSingle();

          if (!existingMsg) {
            let structuredData = msg.structuredDataJson || null;
            if (typeof structuredData === 'string') {
              try { structuredData = JSON.parse(structuredData); } catch { /* keep as-is */ }
            }

            const { error: msgErr } = await supabase.from('messages').insert({
              conversation_id: cloudConvId,
              local_id: msg.id,
              local_conversation_id: msg.conversationId,
              role: msg.role,
              text_content: msg.text,
              structured_data: structuredData,
              created_at: epochToIso(msg.createdAt)
            });
            if (msgErr) { summary.errors.messages = msgErr.message; continue; }
            pushedMsgs++;
          }
        }
      }
      summary.pushed.conversations = pushedConvs;
      summary.pushed.messages = pushedMsgs;
    }

    return { success: true, summary };
  } catch (err) {
    fastify.log.error({ err: err.message }, 'Sync push failed');
    return reply.code(500).send({ error: 'Sync push failed', details: err.message });
  }
});

// -------------------------------------------------------------
// GET /sync/pull?since=<ISO timestamp>
// Returns rows changed since the given timestamp for the user
// (ai_analyses excluded — they're audit-only)
// -------------------------------------------------------------
fastify.get('/sync/pull', async (request, reply) => {
  if (!supabase) {
    return reply.code(503).send({ error: 'Supabase not configured' });
  }

  const uid = request.user.uid;
  const since = request.query.since || '1970-01-01T00:00:00Z';

  try {
    // 1. User Profile
    const { data: profile } = await supabase
      .from('user_profiles')
      .select('*')
      .eq('firebase_uid', uid)
      .gte('updated_at', since)
      .maybeSingle();

    // 2. Nutrition Goal
    const { data: goal } = await supabase
      .from('nutrition_goals')
      .select('*')
      .eq('firebase_uid', uid)
      .gte('updated_at', since)
      .maybeSingle();

    // 3. Meal Logs
    const { data: mealLogs } = await supabase
      .from('meal_logs')
      .select('*')
      .eq('firebase_uid', uid)
      .gte('logged_at', since)
      .order('logged_at', { ascending: true });

    // 4. Food Log Items (for the fetched meals)
    let foodLogItems = [];
    if (mealLogs && mealLogs.length > 0) {
      const mealIds = mealLogs.map(m => m.id);
      const { data: items } = await supabase
        .from('food_log_items')
        .select('*')
        .in('meal_log_id', mealIds);
      foodLogItems = items || [];
    }

    // 5. Weight Entries
    const { data: weightEntries } = await supabase
      .from('weight_entries')
      .select('*')
      .eq('firebase_uid', uid)
      .gte('logged_at', since)
      .order('logged_at', { ascending: true });

    // 6. Water Logs
    const { data: waterLogs } = await supabase
      .from('water_logs')
      .select('*')
      .eq('firebase_uid', uid)
      .gte('logged_at', since)
      .order('logged_at', { ascending: true });

    // 7. Conversations
    const { data: conversations } = await supabase
      .from('conversations')
      .select('*')
      .eq('firebase_uid', uid)
      .gte('last_message_at', since)
      .order('last_message_at', { ascending: true });

    // 8. Messages (for the fetched conversations)
    let messages = [];
    if (conversations && conversations.length > 0) {
      const convIds = conversations.map(c => c.id);
      const { data: msgs } = await supabase
        .from('messages')
        .select('*')
        .in('conversation_id', convIds)
        .gte('created_at', since)
        .order('created_at', { ascending: true });
      messages = msgs || [];
    }

    return {
      user_profile: profile || null,
      nutrition_goal: goal || null,
      meal_logs: mealLogs || [],
      food_log_items: foodLogItems,
      weight_entries: weightEntries || [],
      water_logs: waterLogs || [],
      conversations: conversations || [],
      messages: messages || [],
      synced_at: new Date().toISOString()
    };
  } catch (err) {
    fastify.log.error({ err: err.message }, 'Sync pull failed');
    return reply.code(500).send({ error: 'Sync pull failed', details: err.message });
  }
});

// -------------------------------------------------------------
// Start server
// -------------------------------------------------------------
const start = async () => {
  try {
    const port = parseInt(process.env.PORT || '8000', 10);
    const host = process.env.HOST || '0.0.0.0';
    await fastify.listen({ port, host });
    console.log(`Lumina AI Proxy Backend running on http://${host}:${port} (Provider: ${geminiClient ? 'gemini' : (NVIDIA_API_KEY ? 'nvidia' : 'unconfigured')})`);
  } catch (err) {
    fastify.log.error(err);
    process.exit(1);
  }
};

start();
