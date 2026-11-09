import 'dotenv/config';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
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

fastify.setErrorHandler((error, request, reply) => {
  if (error.code === 'FST_ERR_CTP_BODY_TOO_LARGE' || error.statusCode === 413) {
    return reply.code(413).send({
      error: 'PayloadTooLarge',
      message: 'Request payload exceeds the 4 MB limit.'
    });
  }
  if (error.statusCode === 400 || error.code === 'FST_ERR_CTP_INVALID_MEDIA_TYPE') {
    return reply.code(400).send({
      error: 'BadRequest',
      message: error.message
    });
  }
  reply.send(error);
});

// Provider Configuration & Model Defaults
export const GEMINI_API_KEY = process.env.GEMINI_API_KEY || '';
export const GEMINI_PRIMARY_MODEL = process.env.GEMINI_PRIMARY_MODEL || 'gemini-2.5-flash';
export const GEMINI_FALLBACK_MODEL = process.env.GEMINI_FALLBACK_MODEL || (GEMINI_PRIMARY_MODEL === 'gemini-3.5-flash-lite' ? 'gemini-2.5-flash' : 'gemini-3.5-flash-lite');

if (process.env.TEST !== 'true' && GEMINI_PRIMARY_MODEL === GEMINI_FALLBACK_MODEL) {
  throw new Error(`Misconfiguration: GEMINI_PRIMARY_MODEL and GEMINI_FALLBACK_MODEL must be distinct model IDs (both are '${GEMINI_PRIMARY_MODEL}')`);
}

export const GEMINI_MODEL = GEMINI_PRIMARY_MODEL;
export const VISION_MODEL = GEMINI_PRIMARY_MODEL;
export const TEXT_MODEL = GEMINI_PRIMARY_MODEL;
export const CHAT_MODEL = GEMINI_PRIMARY_MODEL;

// Initialize Google GenAI client if key is present
export let geminiClient = null;
if (GEMINI_API_KEY) {
  try {
    geminiClient = new GoogleGenAI({ apiKey: GEMINI_API_KEY });
    fastify.log.info({ provider: 'gemini', primary: GEMINI_PRIMARY_MODEL, fallback: GEMINI_FALLBACK_MODEL }, `Google GenAI initialized (Primary: ${GEMINI_PRIMARY_MODEL}, Fallback: ${GEMINI_FALLBACK_MODEL})`);
  } catch (err) {
    fastify.log.error({ err: err.message }, 'Failed to initialize Google GenAI');
  }
} else {
  fastify.log.warn('GEMINI_API_KEY not provided, AI features will be unconfigured');
}

/**
 * Execute a Gemini generateContent request with automatic fallback
 * from GEMINI_PRIMARY_MODEL to GEMINI_FALLBACK_MODEL if the primary fails.
 */
export async function executeGeminiWithFallback(params, operationName = 'GeminiOperation') {
  if (!geminiClient) {
    throw new Error('Gemini client not initialized (missing GEMINI_API_KEY)');
  }

  const primaryModel = process.env.GEMINI_PRIMARY_MODEL || process.env.GEMINI_MODEL || 'gemini-2.5-flash';
  const fallbackModel = process.env.GEMINI_FALLBACK_MODEL || 'gemini-3.5-flash-lite';

  try {
    const res = await geminiClient.models.generateContent({
      ...params,
      model: primaryModel
    });
    fastify.log.info({ operation: operationName, model: primaryModel, fallback: false }, `${operationName} completed with primary model`);
    return { response: res, modelUsed: primaryModel, fallback: false };
  } catch (primaryError) {
    fastify.log.warn(
      { operation: operationName, primaryModel, fallbackModel, err: primaryError.message },
      `Primary model ${primaryModel} failed; attempting fallback to ${fallbackModel}`
    );
    appendBackendLog(`MODEL_FALLBACK: ${operationName} primary=${primaryModel} failed (${primaryError.message}) -> falling back to ${fallbackModel}`);

    try {
      const res = await geminiClient.models.generateContent({
        ...params,
        model: fallbackModel
      });
      fastify.log.info({ operation: operationName, model: fallbackModel, fallback: true }, `${operationName} completed with fallback model`);
      return { response: res, modelUsed: fallbackModel, fallback: true };
    } catch (fallbackError) {
      fastify.log.error(
        { operation: operationName, fallbackModel, err: fallbackError.message },
        `Fallback model ${fallbackModel} also failed`
      );
      appendBackendLog(`MODEL_FALLBACK_FAILED: ${operationName} fallback=${fallbackModel} failed (${fallbackError.message})`);
      throw fallbackError;
    }
  }
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
// AI Circuit Breaker & Error Formatting Helper
// -------------------------------------------------------------
export function sendAiUnavailable(reply, err, customMessage = 'Meal analysis is busy right now. Please try again in a minute.') {
  let retryAfter = 60;
  const retryMatch = err?.message?.match(/retry(?:Delay)?["':\s]+([0-9.]+)s?/i);
  if (retryMatch && retryMatch[1]) {
    const parsedSec = Math.ceil(parseFloat(retryMatch[1]));
    if (!isNaN(parsedSec) && parsedSec > 0) {
      retryAfter = parsedSec;
    }
  }

  reply.header('Retry-After', retryAfter.toString());
  return reply.code(503).send({
    error: 'AiUnavailable',
    message: customMessage,
    retryable: true,
    retryAfter
  });
}

// -------------------------------------------------------------
// Firebase Admin & Rate Limiter
// -------------------------------------------------------------
if (process.env.NODE_ENV === 'production' && process.env.FIREBASE_AUTH_EMULATOR_HOST && process.env.FIREBASE_AUTH_EMULATOR_HOST.trim() && process.env.TEST !== 'true') {
  throw new Error('FATAL: FIREBASE_AUTH_EMULATOR_HOST cannot be set in production (emulator tokens are unsigned).');
}

const projectId = process.env.FIREBASE_PROJECT_ID;
if (!projectId || !projectId.trim()) {
  throw new Error('FATAL: FIREBASE_PROJECT_ID environment variable is missing or empty. A valid Firebase Project ID is required.');
}

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
// Security: no ALLOW_DEV_TOKENS / NODE_ENV=test bypass exists here by design.
fastify.addHook('preHandler', async (request, reply) => {
  if (request.url === '/health' || request.url === '/') {
    return;
  }

  // Production fail-closed: if Supabase is unconfigured in production, refuse all quota/sync routes with 503
  if (process.env.NODE_ENV === 'production' && !supabase) {
    if (request.url.startsWith('/ai/') || request.url.startsWith('/sync/')) {
      return reply.code(503).send({
        error: 'ServiceUnavailable',
        message: 'Shared database storage is required in production but currently unconfigured.'
      });
    }
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

  // Security (auth bypass hardening): test-token is strictly prohibited in production.
  // In non-production, test-token is gated on explicit non-production flag ALLOW_DEV_TOKENS === 'true'.
  if (idToken.startsWith('test-token-')) {
    const isProduction = process.env.NODE_ENV === 'production';
    const allowDevTokens = process.env.ALLOW_DEV_TOKENS === 'true' && !isProduction;

    if (!allowDevTokens || isProduction) {
      const reason = isProduction
        ? 'test-token prefix is strictly prohibited in production (NODE_ENV=production)'
        : 'test-token prefix rejected (ALLOW_DEV_TOKENS is not set to true)';
      appendBackendLog(`401 UNAUTHORIZED on ${request.method} ${request.url} - Reason: ${reason} (NODE_ENV=${process.env.NODE_ENV || 'unset'})`);
      fastify.log.warn({ url: request.url, nodeEnv: process.env.NODE_ENV || 'unset', isProduction }, `Rejected request: ${reason}`);
      return reply.code(401).send({
        error: 'Unauthorized',
        message: 'Invalid or expired Firebase ID token.',
        details: isProduction
          ? 'Test tokens are strictly prohibited in production environments.'
          : 'Test tokens are disabled. Set ALLOW_DEV_TOKENS=true in non-production environments to enable.'
      });
    }

    const testUid = idToken.replace('test-token-', '') || 'test-user';
    request.user = { uid: testUid, email: `${testUid}@local.test` };
    const rateStatus = checkRateLimit(request.user.uid);
    reply.header('X-RateLimit-Limit', RATE_LIMIT_MAX_CALLS);
    reply.header('X-RateLimit-Remaining', rateStatus.remaining);

    if (!rateStatus.allowed) {
      reply.header('Retry-After', rateStatus.resetSeconds);
      return reply.code(429).send({
        error: 'Too Many Requests',
        message: `Hourly AI limit reached. Please try again in ${rateStatus.resetSeconds} seconds.`
      });
    }
    return;
  }

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
  const primary = process.env.GEMINI_PRIMARY_MODEL || process.env.GEMINI_MODEL || 'gemini-2.5-flash';
  const fallback = process.env.GEMINI_FALLBACK_MODEL || 'gemini-3.5-flash-lite';
  return {
    status: 'ok',
    service: 'kcalgrindai-backend',
    provider: geminiClient ? 'gemini' : 'unconfigured',
    model: primary,
    models: {
      primary,
      fallback
    },
    visionModel: primary,
    chatModel: primary,
    timestamp: new Date().toISOString()
  };
});


// -------------------------------------------------------------
// 2. POST /ai/analyze-photo
// -------------------------------------------------------------
fastify.post('/ai/analyze-photo', { bodyLimit: 4 * 1024 * 1024 }, async (request, reply) => {
  const { imageBase64, dietTags = [], allergies = [] } = request.body || {};

  const validateTagList = (arr) => {
    if (!Array.isArray(arr)) return false;
    if (arr.length > 20) return false;
    for (const item of arr) {
      if (typeof item !== 'string' || item.length > 50) return false;
    }
    return true;
  };

  if (!validateTagList(dietTags) || !validateTagList(allergies)) {
    return reply.code(400).send({
      error: 'InvalidRequest',
      message: 'dietTags and allergies must be arrays of strings, max 20 items, max 50 chars each'
    });
  }

  if (!imageBase64 || typeof imageBase64 !== 'string' || !imageBase64.trim()) {
    return reply.code(400).send({ error: 'InvalidImage', message: 'imageBase64 is required and must be a non-empty string' });
  }

  // Accept optional data:image/...;base64, prefix by stripping it before decoding
  let cleanBase64 = imageBase64.trim();
  if (cleanBase64.startsWith('data:')) {
    const commaIdx = cleanBase64.indexOf(',');
    if (commaIdx !== -1) {
      cleanBase64 = cleanBase64.substring(commaIdx + 1).trim();
    }
  }

  // Decode buffer
  let imageBuffer;
  try {
    imageBuffer = Buffer.from(cleanBase64, 'base64');
    if (!imageBuffer || imageBuffer.length === 0) {
      return reply.code(400).send({ error: 'InvalidImage', message: 'Failed to decode base64 image data' });
    }
  } catch (e) {
    return reply.code(400).send({ error: 'InvalidImage', message: 'Invalid base64 payload' });
  }

  // Reject if the decoded size exceeds the route limit (4 MB)
  const MAX_IMAGE_BYTES = 4 * 1024 * 1024;
  if (imageBuffer.length > MAX_IMAGE_BYTES) {
    return reply.code(413).send({
      error: 'PayloadTooLarge',
      message: `Decoded image size (${imageBuffer.length} bytes) exceeds the 4 MB limit.`
    });
  }

  // MimeType passed to the provider must come from magic bytes, never from the client
  let detectedMime = null;
  if (imageBuffer.length >= 3 && imageBuffer[0] === 0xFF && imageBuffer[1] === 0xD8 && imageBuffer[2] === 0xFF) {
    detectedMime = 'image/jpeg';
  } else if (imageBuffer.length >= 8 &&
             imageBuffer[0] === 0x89 && imageBuffer[1] === 0x50 && imageBuffer[2] === 0x4E && imageBuffer[3] === 0x47 &&
             imageBuffer[4] === 0x0D && imageBuffer[5] === 0x0A && imageBuffer[6] === 0x1A && imageBuffer[7] === 0x0A) {
    detectedMime = 'image/png';
  } else if (imageBuffer.length >= 12 &&
             imageBuffer[0] === 0x52 && imageBuffer[1] === 0x49 && imageBuffer[2] === 0x46 && imageBuffer[3] === 0x46 &&
             imageBuffer[8] === 0x57 && imageBuffer[9] === 0x45 && imageBuffer[10] === 0x42 && imageBuffer[11] === 0x50) {
    detectedMime = 'image/webp';
  }

  if (!detectedMime) {
    return reply.code(400).send({
      error: 'InvalidImage',
      message: 'Unsupported image format. Only valid JPEG, PNG, or WEBP images with valid magic bytes are accepted.'
    });
  }

  // Quota enforcement: Free 5 photos/day, Pro 50 photos/day
  // Concurrency-safe atomic reservation (shared Supabase RPC in production)
  const uid = request.user?.uid || 'anonymous';
  const isPro = determineUserIsPro(request);
  const requestedTz = request.headers['x-timezone'] || request.body?.timezone;
  const timezone = await resolveUserTimezoneAsync(uid, requestedTz);

  let reservation;
  try {
    reservation = await tryReservePhotoQuotaAsync(uid, timezone, isPro);
  } catch (err) {
    if (process.env.NODE_ENV === 'production' || err.statusCode === 503) {
      fastify.log.error({ err: err.message, uid }, 'Photo quota reservation failed in production mode with 503');
      return reply.code(503).send({
        error: 'ServiceUnavailable',
        message: 'Photo analysis service is temporarily unavailable. Please try again shortly.'
      });
    }
    throw err;
  }

  if (!reservation.allowed) {
    fastify.log.warn({ uid, isPro, usedToday: reservation.usedToday, limit: reservation.dailyLimit }, 'Photo analysis quota exceeded');
    return reply.code(429).send({
      error: 'QuotaExceeded',
      message: isPro
        ? "You've reached today's Pro photo analysis limit."
        : "You've reached today's free photo analysis limit (5 photos/day). You can still log meals using search or text.",
      usedToday: reservation.usedToday,
      dailyLimit: reservation.dailyLimit,
      resetAt: 'midnight'
    });
  }

  if (!geminiClient || !GEMINI_API_KEY) {
    fastify.log.error('Gemini vision provider is unconfigured (missing GEMINI_API_KEY)');
    appendBackendLog('GEMINI_CONFIG_ERROR: missing GEMINI_API_KEY');
    await releasePhotoReservationAsync(uid, timezone);
    return reply.code(500).send({
      error: 'InternalServerError',
      message: 'AI analysis service is temporarily unavailable.'
    });
  }

  fastify.log.info({
    payloadBytes: imageBuffer.length,
    mime: detectedMime,
    provider: 'gemini',
    model: VISION_MODEL
  }, 'Analyzing photo payload');
  appendBackendLog(`PHOTO_ANALYZE: bytes=${imageBuffer.length} mime=${detectedMime} provider=gemini model=${VISION_MODEL}`);

  try {
    const prompt = `You are KcalGrind Nutrition Vision AI.
Analyze this meal image.
Identify each food/ingredient, estimate grams (estimatedGrams), calories (rounded to nearest 5-10 kcal), and macros (protein, carbs, fat in grams).
Assign an honest confidence score (0.0 to 1.0) per food item reflecting visual certainty. For ambiguous food, assign lower confidence (0.35-0.70). For clear identifiable food, assign higher confidence (0.85-0.98).
Compute overallConfidence (0.0 to 1.0).
If the image does not clearly show edible food or drink (blank or very dark image, screens, objects, people, empty plates), return foods as an empty array and overallConfidence 0. Never return placeholder items such as 'No Food' or 'Empty Plate'. Every item must be real food with estimatedGrams > 0.
User dietary tags: ${dietTags.join(', ') || 'None'}. Allergies: ${allergies.join(', ') || 'None'}.`;

    const { response, modelUsed } = await executeGeminiWithFallback({
      contents: [
        {
          role: 'user',
          parts: [
            { text: prompt },
            {
              inlineData: {
                data: cleanBase64,
                mimeType: detectedMime
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
    }, 'analyze-photo');

    const parsed = JSON.parse(response.text);
    if (parsed && Array.isArray(parsed.foods)) {
      const newUsed = await commitPhotoQuotaAsync(uid, timezone);
      parsed.foods = parsed.foods.filter(item => {
        const grams = Number(item.estimatedGrams) || 0;
        const cals = Number(item.calories) || 0;
        return !(grams <= 0 && cals <= 0);
      });
      const quotaMeta = {
        usedToday: newUsed,
        dailyLimit: reservation.dailyLimit,
        remaining: Math.max(0, reservation.dailyLimit - newUsed)
      };
      if (parsed.foods.length === 0) {
        return { foods: [], overallConfidence: 0, model: modelUsed, quota: quotaMeta };
      }
      return { ...parsed, model: modelUsed, quota: quotaMeta };
    }

    throw new Error('Invalid response structure from Gemini vision');
  } catch (geminiError) {
    fastify.log.error({ err: geminiError.message }, 'Gemini vision analysis failed');
    appendBackendLog(`GEMINI_VISION_FAILED: ${geminiError.message}`);
    await releasePhotoReservationAsync(uid, timezone);
    return sendAiUnavailable(reply, geminiError);
  }
});

// -------------------------------------------------------------
// 3. POST /ai/analyze-text
// -------------------------------------------------------------
fastify.post('/ai/analyze-text', async (request, reply) => {
  const { text, dietTags = [], allergies = [] } = request.body || {};

  if (!text || !text.trim()) {
    return reply.code(400).send({ error: 'text is required' });
  }

  if (!geminiClient || !GEMINI_API_KEY) {
    appendBackendLog('GEMINI_CONFIG_ERROR: missing GEMINI_API_KEY in text analysis');
    return reply.code(500).send({
      error: 'InternalServerError',
      message: 'AI analysis service is temporarily unavailable.'
    });
  }

  fastify.log.info({ textLength: text?.length, provider: 'gemini', model: TEXT_MODEL }, 'Analyzing text meal description');

  try {
    const prompt = `You are KcalGrind Nutrition Text Parser AI.
Analyze the user's meal description: "${text}".
Identify each food item mentioned with portion sizes.
Estimate reasonable grams, calories (rounded to nearest 5-10 kcal), and macros (protein, carbs, fat in grams).
Assign an honest confidence score (0.0 to 1.0) per item:
- High (0.85 - 0.99): clear, unambiguous food names and exact portions (e.g. "2 boiled eggs and 1 slice toast").
- Medium (0.50 - 0.84): moderately specific (e.g. "some pasta with red sauce").
- Low (0.10 - 0.49): extremely vague or ambiguous.
Compute overallConfidence as the average confidence.
User dietary tags: ${dietTags.join(', ') || 'None'}. Allergies: ${allergies.join(', ') || 'None'}.`;

    const { response, modelUsed } = await executeGeminiWithFallback({
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
    }, 'analyze-text');

    const parsed = JSON.parse(response.text);
    if (parsed && Array.isArray(parsed.foods) && parsed.foods.length > 0) {
      return { ...parsed, model: modelUsed };
    }

    throw new Error('Invalid response structure from Gemini text parser');
  } catch (geminiError) {
    fastify.log.error({ err: geminiError.message }, 'Gemini text parser failed');
    appendBackendLog(`GEMINI_TEXT_FAILED: ${geminiError.message}`);
    return sendAiUnavailable(reply, geminiError);
  }
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
export function buildCoachSystemInstruction(userContext) {
  return `You are Sproutling.
You provide intelligent, empathetic, evidence-based nutrition coaching.
User Context:
- Goal: ${formatGoalLabel(userContext?.goal || userContext?.profile?.goal)}
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
- Ground your answers strictly in the tool data.

Safety Guardrails:
- Safety Floors: NEVER recommend, validate, or endorse daily calorie targets below the app guardrails (strictly 1,200 kcal/day for women, 1,500 kcal/day for men). If a user requests an intake below these thresholds, refuse gently and explain that minimum energy intakes are vital for organ function and metabolic health.
- Eating Disorder & Self-Harm Protection: If a user asks for tips to starve, purge, skip meals to extremes, or expresses thoughts of self-harm or eating disorders: immediately refuse to assist with restriction, offer empathetic care, and provide supportive redirection to speak with a healthcare provider or contact an eating disorder helpline.
- Medical Advice Disclaimer: You are an AI nutrition assistant, not a physician or dietitian. Never diagnose medical conditions, prescribe clinical diets, or advise on prescription medications. Clearly remind the user to consult a doctor for medical questions.`;
}

// -------------------------------------------------------------
// Coach Safety Guardrails: Crisis, Self-Harm & Eating Disorder Interceptor
// -------------------------------------------------------------
export const CRISIS_SUPPORT_RESPONSE = `I care deeply about your health and safety, and I cannot provide guidance on extreme restriction, fasting, purging, or self-harm.

If you are experiencing distress, struggling with eating habits, or feeling overwhelmed, compassionate support is available right now:

India Helplines:
• AASRA (24/7 Suicide Prevention & Emotional Crisis): +91 98204 66726 (aasra.info)
• KIRAN (Mental Health Helpline by Govt. of India / MSJE): 1800-599-0019
• Vandrevala Foundation (24/7 Mental Health Crisis Line): +91 9999 666 555

US & International Helplines:
• 988 Suicide & Crisis Lifeline: Call or text 988 (free, confidential, 24/7)
• NEDA / ANAD Eating Disorders Helpline: 1-888-375-7767 (anad.org)

Please reach out to one of these services or speak with a licensed healthcare provider who can support you safely.`;

export function checkCoachSafetyCrisis(text = '', messages = []) {
  const combined = [
    text,
    ...(Array.isArray(messages) ? messages.map(m => m?.content || m?.text || '') : [])
  ].join(' ').toLowerCase();

  const crisisPatterns = [
    /\b(kill\s+myself|commit\s+suicide|end\s+my\s+life|want\s+to\s+die|suicidal)\b/i,
    /\b(starve\s+myself|how\s+to\s+starve|starving\s+myself|starve\s+to\s+death|starve\b)/i,
    /\b(purge|purging)\b/i,
    /\b(make\s+myself\s+throw\s+up|throw\s+up\s+after\s+(eating|dinner|food|meals?))\b/i,
    /\b(vomit\s+after\s+(eating|meals?|food))\b/i,
    /\b(laxatives?\s+(to\s+empty|after|for\s+weight)|empty\s+my\s+stomach\s+(with\s+laxatives?|after))\b/i,
    /\b(chew\s+and\s+spit)\b/i,
    /\b(hide\s+(my\s+)?(anorexia|bulimia|eating\s+disorder)|anorexia|bulimia)\b/i,
    /\b(punish\s+myself\s+by\s+(not\s+eating|starving))\b/i,
    /\b(\d+[- ]day\s+(water\s+)?fast|water\s+fast(ing)?|dry\s+fast(ing)?|fast\s+(for\s+)?(7|10|14|21|30)\s+days)\b/i,
    /\b(eat(ing)?\s+(only\s+)?(0|50|100|200|300|500)\s+calories(\s+a\s+day)?)\b/i
  ];

  return crisisPatterns.some(pattern => pattern.test(combined));
}

// Helper: Process Tool Execution for AI Coach
// -------------------------------------------------------------
export function formatGoalLabel(rawGoal) {
  if (!rawGoal) return 'maintain weight';
  const GOAL_LABELS = {
    'LOSE': 'lose weight',
    'lose': 'lose weight',
    'lose_weight': 'lose weight',
    'MAINTAIN': 'maintain weight',
    'maintain': 'maintain weight',
    'maintain_weight': 'maintain weight',
    'GAIN': 'gain weight',
    'gain': 'gain weight',
    'gain_weight': 'gain weight',
    'MUSCLE': 'build muscle',
    'muscle': 'build muscle',
    'build_muscle': 'build muscle',
    'HEALTHIER': 'eat healthier',
    'healthier': 'eat healthier',
    'eat_healthier': 'eat healthier',
    'NUTRITION': 'track nutrition',
    'nutrition': 'track nutrition',
    'track_nutrition': 'track nutrition'
  };
  const key = String(rawGoal).trim();
  return GOAL_LABELS[key] || GOAL_LABELS[key.toUpperCase()] || GOAL_LABELS[key.toLowerCase()] || key.toLowerCase();
}

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
      goal: formatGoalLabel(userContext?.goal || userContext?.profile?.goal),
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
// Supabase Client (Phase 10 — Cloud Sync & Shared Quota Storage)
// -------------------------------------------------------------
export let supabase = null;
export function setSupabaseClient(client) {
  supabase = client;
}

if (process.env.SUPABASE_URL && process.env.SUPABASE_SERVICE_KEY) {
  supabase = createClient(
    process.env.SUPABASE_URL,
    process.env.SUPABASE_SERVICE_KEY
  );
  fastify.log.info('Supabase client initialized for cloud sync and shared quota storage');
} else {
  if (process.env.NODE_ENV === 'production') {
    fastify.log.error('FATAL: SUPABASE_URL and SUPABASE_SERVICE_KEY are strictly required in production mode!');
    if (process.env.TEST !== 'true') {
      console.error('FATAL: Missing SUPABASE_URL or SUPABASE_SERVICE_KEY in production mode. Refusing to start.');
      process.exit(1);
    }
  } else {
    fastify.log.warn('SUPABASE_URL or SUPABASE_SERVICE_KEY not set — sync routes will return 503');
  }
}

// -------------------------------------------------------------
// AI Coach Daily Usage, Persistence & Quota Enforcement
// Free: EXACTLY 5 messages/day (Configurable via FREE_AI_COACH_DAILY_LIMIT)
// Pro:  EXACTLY 50 messages/day (Configurable via PRO_AI_COACH_DAILY_LIMIT)
// Resets automatically at 12:00 AM in user's validated local timezone.
// Persisted outside git in backend/data/
// -------------------------------------------------------------
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const DATA_DIR = process.env.DATA_DIR || path.resolve(__dirname, '..', 'data');
const USAGE_FILE = path.join(DATA_DIR, 'ai_coach_usage.json');
const TIMEZONES_FILE = path.join(DATA_DIR, 'user_timezones.json');

export const FREE_AI_COACH_DAILY_LIMIT = parseInt(process.env.FREE_AI_COACH_DAILY_LIMIT || '5', 10);
export const PRO_AI_COACH_DAILY_LIMIT = parseInt(process.env.PRO_AI_COACH_DAILY_LIMIT || '50', 10);

export const FREE_PHOTO_DAILY_LIMIT = parseInt(process.env.FREE_PHOTO_DAILY_LIMIT || '5', 10);
export const PRO_PHOTO_DAILY_LIMIT = parseInt(process.env.PRO_PHOTO_DAILY_LIMIT || '50', 10);

export const aiCoachDailyUsage = new Map(); // key: `${uid}:${localDay}` -> count
export const photoDailyUsage = new Map(); // key: `${uid}:${localDay}` -> count
export const userTimezones = new Map(); // key: uid -> validated IANA timezone string
export const inFlightAiCoachReservations = new Map(); // key: `${uid}:${localDay}` -> in-flight count
export const inFlightPhotoReservations = new Map(); // key: `${uid}:${localDay}` -> in-flight count
const PHOTO_USAGE_FILE = path.join(DATA_DIR, 'photo_usage.json');

function saveJsonAtomic(filePath, data) {
  try {
    fs.mkdirSync(path.dirname(filePath), { recursive: true });
    const tmpPath = `${filePath}.tmp.${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;
    fs.writeFileSync(tmpPath, JSON.stringify(data, null, 2), 'utf8');
    try {
      fs.renameSync(tmpPath, filePath);
    } catch (renameErr) {
      if (renameErr.code === 'EPERM' || renameErr.code === 'EBUSY') {
        fs.copyFileSync(tmpPath, filePath);
        try { fs.unlinkSync(tmpPath); } catch (_) {}
      } else {
        throw renameErr;
      }
    }
  } catch (err) {
    fastify.log.error({ err: err.message, filePath }, 'Failed to persist JSON to disk');
  }
}

function loadJsonSafe(filePath, defaultValue) {
  try {
    if (fs.existsSync(filePath)) {
      const raw = fs.readFileSync(filePath, 'utf8');
      return JSON.parse(raw);
    }
  } catch (err) {
    fastify.log.warn({ err: err.message, filePath }, 'Failed to load JSON file, using default');
  }
  return defaultValue;
}

export function persistUsageToDisk() {
  if (process.env.NODE_ENV === 'production') return;
  const obj = Object.fromEntries(aiCoachDailyUsage);
  saveJsonAtomic(USAGE_FILE, obj);
}

export function persistPhotoUsageToDisk() {
  if (process.env.NODE_ENV === 'production') return;
  const obj = Object.fromEntries(photoDailyUsage);
  saveJsonAtomic(PHOTO_USAGE_FILE, obj);
}

export function persistTimezonesToDisk() {
  if (process.env.NODE_ENV === 'production') return;
  const obj = Object.fromEntries(userTimezones);
  saveJsonAtomic(TIMEZONES_FILE, obj);
}

export function initPersistence() {
  if (process.env.NODE_ENV === 'production') return;
  const loadedUsage = loadJsonSafe(USAGE_FILE, {});
  aiCoachDailyUsage.clear();
  for (const [key, count] of Object.entries(loadedUsage)) {
    if (typeof count === 'number') {
      aiCoachDailyUsage.set(key, count);
    }
  }

  const loadedPhotoUsage = loadJsonSafe(PHOTO_USAGE_FILE, {});
  photoDailyUsage.clear();
  for (const [key, count] of Object.entries(loadedPhotoUsage)) {
    if (typeof count === 'number') {
      photoDailyUsage.set(key, count);
    }
  }

  const loadedTimezones = loadJsonSafe(TIMEZONES_FILE, {});
  userTimezones.clear();
  for (const [uid, tz] of Object.entries(loadedTimezones)) {
    if (isValidIanaTimezone(tz)) {
      userTimezones.set(uid, tz);
    }
  }
}

// Initialize persistence on module load
initPersistence();

export function isValidIanaTimezone(tz) {
  if (!tz || typeof tz !== 'string') return false;
  try {
    Intl.DateTimeFormat(undefined, { timeZone: tz });
    return true;
  } catch {
    return false;
  }
}

export function resolveUserTimezone(uid, requestedTimezone) {
  const existing = uid ? userTimezones.get(uid) : null;
  if (existing) {
    // Once associated, use stored validated IANA timezone consistently.
    // Do not allow arbitrary client headers or 'UTC' fallback to overwrite an existing verified timezone.
    if (isValidIanaTimezone(requestedTimezone) && requestedTimezone !== 'UTC' && requestedTimezone !== existing) {
      userTimezones.set(uid, requestedTimezone);
      persistTimezonesToDisk();
      return requestedTimezone;
    }
    return existing;
  }

  if (isValidIanaTimezone(requestedTimezone)) {
    if (uid && uid !== 'anonymous') {
      userTimezones.set(uid, requestedTimezone);
      persistTimezonesToDisk();
    }
    return requestedTimezone;
  }

  const systemTz = Intl.DateTimeFormat().resolvedOptions().timeZone;
  if (isValidIanaTimezone(systemTz)) {
    return systemTz;
  }
  return 'UTC';
}

// -------------------------------------------------------------
// Database-Backed Timezone Persistence (user_profiles.timezone)
// Stores the user's validated IANA timezone in Postgres rather than ephemeral memory
// -------------------------------------------------------------
export async function resolveUserTimezoneAsync(uid, requestedTimezone) {
  if (supabase && uid && uid !== 'anonymous') {
    try {
      const { data: profile } = await supabase
        .from('user_profiles')
        .select('timezone, timezone_changed_at')
        .eq('firebase_uid', uid)
        .maybeSingle();

      const existingTz = profile?.timezone;
      if (existingTz && isValidIanaTimezone(existingTz)) {
        if (isValidIanaTimezone(requestedTimezone) && requestedTimezone !== 'UTC' && requestedTimezone !== existingTz) {
          // Timezone locking: allow change only once every 7 days (protects daily quota consistency)
          const lastChanged = profile?.timezone_changed_at ? new Date(profile.timezone_changed_at).getTime() : 0;
          const now = Date.now();
          const cooldownMs = 7 * 24 * 60 * 60 * 1000; // 7 days

          if (now - lastChanged < cooldownMs) {
            fastify.log.warn({ uid, existingTz, requestedTimezone }, 'Timezone change locked: can only be changed once every 7 days');
            userTimezones.set(uid, existingTz);
            return existingTz;
          }

          const nowIso = new Date().toISOString();
          await supabase
            .from('user_profiles')
            .update({ timezone: requestedTimezone, timezone_changed_at: nowIso, updated_at: nowIso })
            .eq('firebase_uid', uid);
          userTimezones.set(uid, requestedTimezone);
          return requestedTimezone;
        }
        userTimezones.set(uid, existingTz);
        return existingTz;
      }

      if (isValidIanaTimezone(requestedTimezone)) {
        const nowIso = new Date().toISOString();
        if (profile) {
          await supabase
            .from('user_profiles')
            .update({ timezone: requestedTimezone, timezone_changed_at: nowIso, updated_at: nowIso })
            .eq('firebase_uid', uid);
        }
        userTimezones.set(uid, requestedTimezone);
        return requestedTimezone;
      }
    } catch (err) {
      fastify.log.warn({ err: err.message, uid }, 'Failed to query or persist user timezone in database');
    }
  }

  return resolveUserTimezone(uid, requestedTimezone);
}

export function getUserLocalDate(timezone) {
  try {
    if (timezone) {
      return new Intl.DateTimeFormat('en-CA', { timeZone: timezone, year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date());
    }
  } catch (e) {
    // Fallback if invalid timezone identifier
  }
  return new Intl.DateTimeFormat('en-CA', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date());
}

export function determineUserIsPro(request) {
  // Production Pro entitlement must come from verified subscription / token claims
  if (request.user?.isPro === true || request.user?.claims?.isPro === true || request.user?.subscription === 'pro') {
    return true;
  }
  // Development / Test environment only: allow test header if not in production
  if (process.env.NODE_ENV !== 'production') {
    if (request.headers['x-test-pro'] === 'true' || request.headers['x-user-pro'] === 'true') {
      return true;
    }
  }
  return false;
}

export function getPhotoUsage(uid, timezone, isPro) {
  const localDay = getUserLocalDate(timezone);
  const key = `${uid}:${localDay}`;
  const used = photoDailyUsage.get(key) || 0;
  const inFlight = inFlightPhotoReservations.get(key) || 0;
  const limit = isPro ? PRO_PHOTO_DAILY_LIMIT : FREE_PHOTO_DAILY_LIMIT;
  return {
    usedToday: used,
    inFlight,
    dailyLimit: limit,
    remaining: Math.max(0, limit - (used + inFlight)),
    isBlocked: (used + inFlight) >= limit,
    isPro: !!isPro,
    localDay,
    timezone
  };
}

export function tryReservePhotoQuota(uid, timezone, isPro) {
  const localDay = getUserLocalDate(timezone);
  const key = `${uid}:${localDay}`;
  const used = photoDailyUsage.get(key) || 0;
  const inFlight = inFlightPhotoReservations.get(key) || 0;
  const limit = isPro ? PRO_PHOTO_DAILY_LIMIT : FREE_PHOTO_DAILY_LIMIT;

  if ((used + inFlight) < limit) {
    inFlightPhotoReservations.set(key, inFlight + 1);
    return {
      allowed: true,
      usedToday: used,
      inFlight: inFlight + 1,
      dailyLimit: limit,
      remaining: Math.max(0, limit - (used + inFlight + 1)),
      isLimitReached: false,
      rolling24hCapReached: false,
      localDay
    };
  } else {
    return {
      allowed: false,
      usedToday: used,
      inFlight,
      dailyLimit: limit,
      remaining: 0,
      isLimitReached: true,
      rolling24hCapReached: false,
      localDay
    };
  }
}

export function commitPhotoQuota(uid, timezone) {
  const localDay = getUserLocalDate(timezone);
  const key = `${uid}:${localDay}`;
  const current = photoDailyUsage.get(key) || 0;
  const inFlight = inFlightPhotoReservations.get(key) || 0;
  photoDailyUsage.set(key, current + 1);
  if (inFlight <= 1) {
    inFlightPhotoReservations.delete(key);
  } else {
    inFlightPhotoReservations.set(key, inFlight - 1);
  }
  persistPhotoUsageToDisk();
  return current + 1;
}

export function releasePhotoReservation(uid, timezone) {
  const localDay = getUserLocalDate(timezone);
  const key = `${uid}:${localDay}`;
  const inFlight = inFlightPhotoReservations.get(key) || 0;
  if (inFlight <= 1) {
    inFlightPhotoReservations.delete(key);
  } else {
    inFlightPhotoReservations.set(key, inFlight - 1);
  }
}

export function recordPhotoAnalysisUsage(uid, timezone) {
  const localDay = getUserLocalDate(timezone);
  const key = `${uid}:${localDay}`;
  const current = photoDailyUsage.get(key) || 0;
  photoDailyUsage.set(key, current + 1);
  persistPhotoUsageToDisk();
  return current + 1;
}

export function resetPhotoUsageForTesting(uid, localDay) {
  if (uid && localDay) {
    photoDailyUsage.delete(`${uid}:${localDay}`);
    inFlightPhotoReservations.delete(`${uid}:${localDay}`);
  } else if (uid) {
    for (const key of photoDailyUsage.keys()) {
      if (key.startsWith(`${uid}:`)) {
        photoDailyUsage.delete(key);
      }
    }
    for (const key of inFlightPhotoReservations.keys()) {
      if (key.startsWith(`${uid}:`)) {
        inFlightPhotoReservations.delete(key);
      }
    }
  } else {
    photoDailyUsage.clear();
    inFlightPhotoReservations.clear();
  }
  persistPhotoUsageToDisk();
}

export function getAiCoachUsage(uid, timezone, isPro) {
  const localDay = getUserLocalDate(timezone);
  const key = `${uid}:${localDay}`;
  const used = aiCoachDailyUsage.get(key) || 0;
  const inFlight = inFlightAiCoachReservations.get(key) || 0;
  const limit = isPro ? PRO_AI_COACH_DAILY_LIMIT : FREE_AI_COACH_DAILY_LIMIT;
  return {
    usedToday: used,
    inFlight,
    dailyLimit: limit,
    remaining: Math.max(0, limit - used),
    isLimitReached: used >= limit,
    isPro,
    localDay
  };
}

export function tryReserveAiCoachQuota(uid, timezone, isPro) {
  const localDay = getUserLocalDate(timezone);
  const key = `${uid}:${localDay}`;
  const used = aiCoachDailyUsage.get(key) || 0;
  const inFlight = inFlightAiCoachReservations.get(key) || 0;
  const limit = isPro ? PRO_AI_COACH_DAILY_LIMIT : FREE_AI_COACH_DAILY_LIMIT;

  if (used + inFlight >= limit) {
    return {
      allowed: false,
      usedToday: used,
      inFlight,
      dailyLimit: limit,
      remaining: Math.max(0, limit - used),
      isLimitReached: true,
      isPro,
      localDay
    };
  }

  inFlightAiCoachReservations.set(key, inFlight + 1);
  return {
    allowed: true,
    usedToday: used,
    inFlight: inFlight + 1,
    dailyLimit: limit,
    remaining: Math.max(0, limit - (used + inFlight + 1)),
    isLimitReached: false,
    isPro,
    localDay
  };
}

export function commitAiCoachQuota(uid, timezone) {
  const localDay = getUserLocalDate(timezone);
  const key = `${uid}:${localDay}`;
  const inFlight = inFlightAiCoachReservations.get(key) || 1;
  if (inFlight <= 1) {
    inFlightAiCoachReservations.delete(key);
  } else {
    inFlightAiCoachReservations.set(key, inFlight - 1);
  }

  const current = aiCoachDailyUsage.get(key) || 0;
  aiCoachDailyUsage.set(key, current + 1);
  persistUsageToDisk();
  return current + 1;
}

export function releaseAiCoachReservation(uid, timezone) {
  const localDay = getUserLocalDate(timezone);
  const key = `${uid}:${localDay}`;
  const inFlight = inFlightAiCoachReservations.get(key) || 0;
  if (inFlight <= 1) {
    inFlightAiCoachReservations.delete(key);
  } else {
    inFlightAiCoachReservations.set(key, inFlight - 1);
  }
}

export function recordAiCoachMessageSent(uid, timezone) {
  const localDay = getUserLocalDate(timezone);
  const key = `${uid}:${localDay}`;
  const current = aiCoachDailyUsage.get(key) || 0;
  aiCoachDailyUsage.set(key, current + 1);
  persistUsageToDisk();
  return current + 1;
}

export function resetAiCoachUsageForTesting(uid, localDay) {
  if (localDay) {
    aiCoachDailyUsage.delete(`${uid}:${localDay}`);
    inFlightAiCoachReservations.delete(`${uid}:${localDay}`);
  } else {
    for (const key of aiCoachDailyUsage.keys()) {
      if (key.startsWith(`${uid}:`)) {
        aiCoachDailyUsage.delete(key);
      }
    }
    for (const key of inFlightAiCoachReservations.keys()) {
      if (key.startsWith(`${uid}:`)) {
        inFlightAiCoachReservations.delete(key);
      }
    }
  }
  persistUsageToDisk();
}

// -------------------------------------------------------------
// Asynchronous Supabase Shared Storage Quota Implementation
// Supports Cloud Run multi-instance scaling & persistence
// Falls back to in-memory/local disk if Supabase is unavailable
// -------------------------------------------------------------
export async function getAiCoachUsageAsync(uid, timezone, isPro) {
  const localDay = getUserLocalDate(timezone);
  const limit = isPro ? PRO_AI_COACH_DAILY_LIMIT : FREE_AI_COACH_DAILY_LIMIT;

  if (!supabase) {
    if (process.env.NODE_ENV === 'production') {
      throw new Error('Production error: Supabase shared quota storage is strictly required in production mode. Local in-memory fallback is disallowed.');
    }
    return getAiCoachUsage(uid, timezone, isPro);
  }

  try {
    const { data: row, error } = await supabase
      .from('ai_coach_daily_usage')
      .select('*')
      .eq('firebase_uid', uid)
      .eq('local_date', localDay)
      .maybeSingle();

    if (error) {
      fastify.log.warn({ error: error.message }, 'Supabase getAiCoachUsage error');
      if (process.env.NODE_ENV === 'production') {
        throw new Error(`Production database error: ${error.message}`);
      }
      return getAiCoachUsage(uid, timezone, isPro);
    }

    const used = row?.used_count || 0;
    const isStale = row?.last_reservation_at && (Date.now() - new Date(row.last_reservation_at).getTime() > 2 * 60 * 1000);
    const inFlight = isStale ? 0 : (row?.in_flight || 0);

    return {
      usedToday: used,
      inFlight,
      dailyLimit: limit,
      remaining: Math.max(0, limit - used),
      isLimitReached: used >= limit,
      isPro,
      localDay
    };
  } catch (err) {
    if (process.env.NODE_ENV === 'production') {
      throw err;
    }
    return getAiCoachUsage(uid, timezone, isPro);
  }
}

export async function tryReserveAiCoachQuotaAsync(uid, timezone, isPro) {
  const localDay = getUserLocalDate(timezone);
  const limit = isPro ? PRO_AI_COACH_DAILY_LIMIT : FREE_AI_COACH_DAILY_LIMIT;

  if (!supabase) {
    if (process.env.NODE_ENV === 'production') {
      const err = new Error('Production error: Supabase shared quota storage is strictly required in production mode. Local in-memory fallback is disallowed.');
      err.statusCode = 503;
      throw err;
    }
    return tryReserveAiCoachQuota(uid, timezone, isPro);
  }

  // Production path: strictly atomic Postgres RPC function with row locking (SELECT ... FOR UPDATE)
  // No fallback to non-atomic queries or in-memory state is permitted in production
  if (process.env.NODE_ENV === 'production') {
    if (typeof supabase.rpc !== 'function') {
      const err = new Error('Production error: Supabase RPC reserve_ai_coach_quota is required in production mode.');
      err.statusCode = 503;
      throw err;
    }

    const { data: rpcData, error: rpcError } = await supabase.rpc('reserve_ai_coach_quota', {
      p_uid: uid,
      p_date: localDay,
      p_limit: limit,
      p_timezone: timezone
    });

    if (rpcError) {
      fastify.log.error({ error: rpcError.message, uid }, 'Production Quota RPC error');
      const err = new Error(`Production database error: ${rpcError.message}`);
      err.statusCode = 503;
      throw err;
    }

    if (!rpcData || typeof rpcData !== 'object' || typeof rpcData.allowed !== 'boolean') {
      const err = new Error('Production error: Unexpected response from reserve_ai_coach_quota RPC');
      err.statusCode = 503;
      throw err;
    }

    if (rpcData.allowed) {
      const key = `${uid}:${localDay}`;
      inFlightAiCoachReservations.set(key, (inFlightAiCoachReservations.get(key) || 0) + 1);
    }
    return rpcData;
  }

  // Development and test path (with local fallback if RPC not installed)
  try {
    // 1. Attempt atomic Postgres RPC function first
    if (typeof supabase.rpc === 'function') {
      const { data: rpcData, error: rpcError } = await supabase.rpc('reserve_ai_coach_quota', {
        p_uid: uid,
        p_date: localDay,
        p_limit: limit,
        p_timezone: timezone
      });
      if (!rpcError && rpcData && typeof rpcData === 'object' && typeof rpcData.allowed === 'boolean') {
        if (rpcData.allowed) {
          const key = `${uid}:${localDay}`;
          inFlightAiCoachReservations.set(key, (inFlightAiCoachReservations.get(key) || 0) + 1);
        }
        return rpcData;
      }
    }

    // 2. Direct atomic query fallback with rolling 24-hour cap (2x daily limit backstop) & 2-minute stale reservation recovery
    const twentyFourHoursAgo = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString();
    const { data: recentRows } = await supabase
      .from('ai_coach_daily_usage')
      .select('used_count')
      .eq('firebase_uid', uid)
      .gte('updated_at', twentyFourHoursAgo);
    const rolling24hUsed = (recentRows || []).reduce((acc, r) => acc + (r.used_count || 0), 0);
    const rollingCap = limit * 2;

    if (rolling24hUsed >= rollingCap) {
      return {
        allowed: false,
        usedToday: rolling24hUsed,
        inFlight: 0,
        dailyLimit: limit,
        remaining: 0,
        isLimitReached: true,
        rolling24hCapReached: true,
        localDay
      };
    }

    const { data: row, error } = await supabase
      .from('ai_coach_daily_usage')
      .select('*')
      .eq('firebase_uid', uid)
      .eq('local_date', localDay)
      .maybeSingle();

    if (error) {
      fastify.log.warn({ error: error.message }, 'Supabase tryReserveAiCoachQuota error in dev/test');
      return tryReserveAiCoachQuota(uid, timezone, isPro);
    }

    const used = row?.used_count || 0;
    // Stale in-flight recovery: reservations older than 2 minutes are discarded
    const isStale = row?.last_reservation_at && (Date.now() - new Date(row.last_reservation_at).getTime() > 2 * 60 * 1000);
    const effectiveInFlight = isStale ? 0 : (row?.in_flight || 0);

    if (used + effectiveInFlight >= limit) {
      return {
        allowed: false,
        usedToday: used,
        inFlight: effectiveInFlight,
        dailyLimit: limit,
        remaining: Math.max(0, limit - used),
        isLimitReached: true,
        rolling24hCapReached: false,
        localDay
      };
    }

    const nextInFlight = effectiveInFlight + 1;
    const nowIso = new Date().toISOString();
    if (row) {
      await supabase
        .from('ai_coach_daily_usage')
        .update({
          in_flight: nextInFlight,
          last_reservation_at: nowIso,
          timezone,
          updated_at: nowIso
        })
        .eq('firebase_uid', uid)
        .eq('local_date', localDay);
    } else {
      await supabase
        .from('ai_coach_daily_usage')
        .insert({
          firebase_uid: uid,
          local_date: localDay,
          used_count: 0,
          in_flight: nextInFlight,
          last_reservation_at: nowIso,
          timezone,
          updated_at: nowIso
        });
    }

    // Mirror to local memory map
    const key = `${uid}:${localDay}`;
    inFlightAiCoachReservations.set(key, (inFlightAiCoachReservations.get(key) || 0) + 1);

    return {
      allowed: true,
      usedToday: used,
      inFlight: nextInFlight,
      dailyLimit: limit,
      remaining: Math.max(0, limit - (used + nextInFlight)),
      isLimitReached: false,
      rolling24hCapReached: false,
      localDay
    };
  } catch (err) {
    fastify.log.warn({ err: err.message }, 'Supabase quota reservation exception in dev/test, falling back to local');
    return tryReserveAiCoachQuota(uid, timezone, isPro);
  }
}

export async function commitAiCoachQuotaAsync(uid, timezone) {
  const localDay = getUserLocalDate(timezone);
  const key = `${uid}:${localDay}`;

  if (!supabase) {
    if (process.env.NODE_ENV === 'production') {
      const err = new Error('Production error: Supabase shared quota storage is strictly required in production mode.');
      err.statusCode = 503;
      throw err;
    }
    commitAiCoachQuota(uid, timezone);
    return (aiCoachDailyUsage.get(key) || 0);
  }

  // Commit locally for local memory mirror
  commitAiCoachQuota(uid, timezone);

  if (process.env.NODE_ENV === 'production') {
    if (typeof supabase.rpc !== 'function') {
      const err = new Error('Production error: Supabase RPC commit_ai_coach_quota is required in production mode.');
      err.statusCode = 503;
      throw err;
    }
    const { data: committedCount, error: rpcError } = await supabase.rpc('commit_ai_coach_quota', {
      p_uid: uid,
      p_date: localDay,
      p_timezone: timezone
    });
    if (rpcError) {
      fastify.log.error({ error: rpcError.message, uid }, 'Production commit_ai_coach_quota RPC error');
      const err = new Error(`Production database error: ${rpcError.message}`);
      err.statusCode = 503;
      throw err;
    }
    return committedCount;
  }

  try {
    if (typeof supabase.rpc === 'function') {
      const { data: committedCount, error: rpcError } = await supabase.rpc('commit_ai_coach_quota', {
        p_uid: uid,
        p_date: localDay,
        p_timezone: timezone
      });
      if (!rpcError && typeof committedCount === 'number') {
        return committedCount;
      }
    }

    const { data: row } = await supabase
      .from('ai_coach_daily_usage')
      .select('*')
      .eq('firebase_uid', uid)
      .eq('local_date', localDay)
      .maybeSingle();

    const used = (row?.used_count || 0) + 1;
    const inFlight = Math.max(0, (row?.in_flight || 1) - 1);
    const nowIso = new Date().toISOString();

    if (row) {
      await supabase
        .from('ai_coach_daily_usage')
        .update({
          used_count: used,
          in_flight: inFlight,
          timezone,
          updated_at: nowIso
        })
        .eq('firebase_uid', uid)
        .eq('local_date', localDay);
    } else {
      await supabase
        .from('ai_coach_daily_usage')
        .insert({
          firebase_uid: uid,
          local_date: localDay,
          used_count: used,
          in_flight: inFlight,
          timezone,
          updated_at: nowIso
        });
    }
    return used;
  } catch (err) {
    fastify.log.warn({ err: err.message }, 'Failed to commit quota to Supabase in dev/test');
    return (aiCoachDailyUsage.get(key) || 0);
  }
}

export async function releaseAiCoachReservationAsync(uid, timezone) {
  const localDay = getUserLocalDate(timezone);

  if (!supabase) {
    if (process.env.NODE_ENV === 'production') {
      const err = new Error('Production error: Supabase shared quota storage is strictly required in production mode.');
      err.statusCode = 503;
      throw err;
    }
    releaseAiCoachReservation(uid, timezone);
    return;
  }

  // Release locally
  releaseAiCoachReservation(uid, timezone);

  if (process.env.NODE_ENV === 'production') {
    if (typeof supabase.rpc !== 'function') {
      const err = new Error('Production error: Supabase RPC release_ai_coach_reservation is required in production mode.');
      err.statusCode = 503;
      throw err;
    }
    const { error: rpcError } = await supabase.rpc('release_ai_coach_reservation', {
      p_uid: uid,
      p_date: localDay
    });
    if (rpcError) {
      fastify.log.error({ error: rpcError.message, uid }, 'Production release_ai_coach_reservation RPC error');
      const err = new Error(`Production database error: ${rpcError.message}`);
      err.statusCode = 503;
      throw err;
    }
    return;
  }

  try {
    if (typeof supabase.rpc === 'function') {
      const { error: rpcError } = await supabase.rpc('release_ai_coach_reservation', {
        p_uid: uid,
        p_date: localDay
      });
      if (!rpcError) return;
    }

    const { data: row } = await supabase
      .from('ai_coach_daily_usage')
      .select('*')
      .eq('firebase_uid', uid)
      .eq('local_date', localDay)
      .maybeSingle();

    if (row) {
      const inFlight = Math.max(0, (row.in_flight || 1) - 1);
      await supabase
        .from('ai_coach_daily_usage')
        .update({
          in_flight: inFlight,
          updated_at: new Date().toISOString()
        })
        .eq('firebase_uid', uid)
        .eq('local_date', localDay);
    }
  } catch (err) {
    fastify.log.warn({ err: err.message }, 'Failed to release quota reservation in Supabase in dev/test');
  }
}

export async function resetAiCoachUsageForTestingAsync(uid, localDay) {
  resetAiCoachUsageForTesting(uid, localDay);

  if (supabase) {
    try {
      if (localDay) {
        await supabase
          .from('ai_coach_daily_usage')
          .delete()
          .eq('firebase_uid', uid)
          .eq('local_date', localDay);
      } else {
        await supabase
          .from('ai_coach_daily_usage')
          .delete()
          .eq('firebase_uid', uid);
      }
    } catch (_) {}
  }
}

export async function getPhotoUsageAsync(uid, timezone, isPro) {
  const localDay = getUserLocalDate(timezone);
  const limit = isPro ? PRO_PHOTO_DAILY_LIMIT : FREE_PHOTO_DAILY_LIMIT;

  if (!supabase) {
    if (process.env.NODE_ENV === 'production') {
      const err = new Error('Production error: Supabase shared quota storage is strictly required in production mode. Local in-memory fallback is disallowed.');
      err.statusCode = 503;
      throw err;
    }
    return getPhotoUsage(uid, timezone, isPro);
  }

  try {
    const { data: row, error } = await supabase
      .from('ai_photo_daily_usage')
      .select('*')
      .eq('firebase_uid', uid)
      .eq('local_date', localDay)
      .maybeSingle();

    if (error) {
      if (process.env.NODE_ENV === 'production') {
        const err = new Error(`Production database error: ${error.message}`);
        err.statusCode = 503;
        throw err;
      }
      return getPhotoUsage(uid, timezone, isPro);
    }

    const used = row?.used_count || 0;
    const inFlight = row?.in_flight || 0;
    return {
      usedToday: used,
      inFlight,
      dailyLimit: limit,
      remaining: Math.max(0, limit - used),
      isBlocked: used >= limit,
      isPro: !!isPro,
      localDay,
      timezone
    };
  } catch (err) {
    if (process.env.NODE_ENV === 'production') throw err;
    return getPhotoUsage(uid, timezone, isPro);
  }
}

export async function tryReservePhotoQuotaAsync(uid, timezone, isPro) {
  const localDay = getUserLocalDate(timezone);
  const limit = isPro ? PRO_PHOTO_DAILY_LIMIT : FREE_PHOTO_DAILY_LIMIT;

  if (!supabase) {
    if (process.env.NODE_ENV === 'production') {
      const err = new Error('Production error: Supabase shared quota storage is strictly required in production mode. Local in-memory fallback is disallowed.');
      err.statusCode = 503;
      throw err;
    }
    return tryReservePhotoQuota(uid, timezone, isPro);
  }

  // Production path: strictly atomic Postgres RPC function with row locking (SELECT ... FOR UPDATE)
  if (process.env.NODE_ENV === 'production') {
    if (typeof supabase.rpc !== 'function') {
      const err = new Error('Production error: Supabase RPC reserve_photo_quota is required in production mode.');
      err.statusCode = 503;
      throw err;
    }

    const { data: rpcData, error: rpcError } = await supabase.rpc('reserve_photo_quota', {
      p_uid: uid,
      p_date: localDay,
      p_limit: limit,
      p_timezone: timezone
    });

    if (rpcError) {
      fastify.log.error({ error: rpcError.message, uid }, 'Production Photo Quota RPC error');
      const err = new Error(`Production database error: ${rpcError.message}`);
      err.statusCode = 503;
      throw err;
    }

    if (!rpcData || typeof rpcData !== 'object' || typeof rpcData.allowed !== 'boolean') {
      const err = new Error('Production error: Unexpected response from reserve_photo_quota RPC');
      err.statusCode = 503;
      throw err;
    }

    if (rpcData.allowed) {
      const key = `${uid}:${localDay}`;
      inFlightPhotoReservations.set(key, (inFlightPhotoReservations.get(key) || 0) + 1);
    }
    return rpcData;
  }

  // Development and test path (with local fallback if RPC not installed)
  try {
    if (typeof supabase.rpc === 'function') {
      const { data: rpcData, error: rpcError } = await supabase.rpc('reserve_photo_quota', {
        p_uid: uid,
        p_date: localDay,
        p_limit: limit,
        p_timezone: timezone
      });
      if (!rpcError && rpcData && typeof rpcData === 'object' && typeof rpcData.allowed === 'boolean') {
        if (rpcData.allowed) {
          const key = `${uid}:${localDay}`;
          inFlightPhotoReservations.set(key, (inFlightPhotoReservations.get(key) || 0) + 1);
        }
        return rpcData;
      }
    }

    // Direct atomic query fallback with rolling 24-hour cap & 2-minute stale reservation recovery
    const twentyFourHoursAgo = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString();
    const { data: recentRows } = await supabase
      .from('ai_photo_daily_usage')
      .select('used_count')
      .eq('firebase_uid', uid)
      .gte('updated_at', twentyFourHoursAgo);
    const rolling24hUsed = (recentRows || []).reduce((acc, r) => acc + (r.used_count || 0), 0);
    const rollingCap = limit * 2;

    if (rolling24hUsed >= rollingCap) {
      return {
        allowed: false,
        usedToday: rolling24hUsed,
        inFlight: 0,
        dailyLimit: limit,
        remaining: 0,
        isLimitReached: true,
        rolling24hCapReached: true,
        localDay
      };
    }

    const { data: row, error } = await supabase
      .from('ai_photo_daily_usage')
      .select('*')
      .eq('firebase_uid', uid)
      .eq('local_date', localDay)
      .maybeSingle();

    if (error) {
      fastify.log.warn({ error: error.message }, 'Supabase tryReservePhotoQuota error in dev/test');
      return tryReservePhotoQuota(uid, timezone, isPro);
    }

    const used = row?.used_count || 0;
    const isStale = row?.last_reservation_at && (Date.now() - new Date(row.last_reservation_at).getTime() > 2 * 60 * 1000);
    const effectiveInFlight = isStale ? 0 : (row?.in_flight || 0);

    if (used + effectiveInFlight >= limit) {
      return {
        allowed: false,
        usedToday: used,
        inFlight: effectiveInFlight,
        dailyLimit: limit,
        remaining: Math.max(0, limit - used),
        isLimitReached: true,
        rolling24hCapReached: false,
        localDay
      };
    }

    const nextInFlight = effectiveInFlight + 1;
    const nowIso = new Date().toISOString();
    if (row) {
      await supabase
        .from('ai_photo_daily_usage')
        .update({
          in_flight: nextInFlight,
          last_reservation_at: nowIso,
          timezone,
          updated_at: nowIso
        })
        .eq('firebase_uid', uid)
        .eq('local_date', localDay);
    } else {
      await supabase
        .from('ai_photo_daily_usage')
        .insert({
          firebase_uid: uid,
          local_date: localDay,
          used_count: 0,
          in_flight: nextInFlight,
          last_reservation_at: nowIso,
          timezone,
          updated_at: nowIso
        });
    }

    const key = `${uid}:${localDay}`;
    inFlightPhotoReservations.set(key, (inFlightPhotoReservations.get(key) || 0) + 1);

    return {
      allowed: true,
      usedToday: used,
      inFlight: nextInFlight,
      dailyLimit: limit,
      remaining: Math.max(0, limit - (used + nextInFlight)),
      isLimitReached: false,
      rolling24hCapReached: false,
      localDay
    };
  } catch (err) {
    fastify.log.warn({ err: err.message }, 'Supabase photo reservation exception in dev/test, falling back to local');
    return tryReservePhotoQuota(uid, timezone, isPro);
  }
}

export async function commitPhotoQuotaAsync(uid, timezone) {
  const localDay = getUserLocalDate(timezone);
  const key = `${uid}:${localDay}`;

  if (!supabase) {
    if (process.env.NODE_ENV === 'production') {
      const err = new Error('Production error: Supabase shared quota storage is strictly required in production mode.');
      err.statusCode = 503;
      throw err;
    }
    commitPhotoQuota(uid, timezone);
    return (photoDailyUsage.get(key) || 0);
  }

  commitPhotoQuota(uid, timezone);

  if (process.env.NODE_ENV === 'production') {
    if (typeof supabase.rpc !== 'function') {
      const err = new Error('Production error: Supabase RPC commit_photo_quota is required in production mode.');
      err.statusCode = 503;
      throw err;
    }
    const { data: committedCount, error: rpcError } = await supabase.rpc('commit_photo_quota', {
      p_uid: uid,
      p_date: localDay,
      p_timezone: timezone
    });
    if (rpcError) {
      fastify.log.error({ error: rpcError.message, uid }, 'Production commit_photo_quota RPC error');
      const err = new Error(`Production database error: ${rpcError.message}`);
      err.statusCode = 503;
      throw err;
    }
    return committedCount;
  }

  try {
    if (typeof supabase.rpc === 'function') {
      const { data: committedCount, error: rpcError } = await supabase.rpc('commit_photo_quota', {
        p_uid: uid,
        p_date: localDay,
        p_timezone: timezone
      });
      if (!rpcError && typeof committedCount === 'number') {
        return committedCount;
      }
    }

    const { data: row } = await supabase
      .from('ai_photo_daily_usage')
      .select('*')
      .eq('firebase_uid', uid)
      .eq('local_date', localDay)
      .maybeSingle();

    const used = (row?.used_count || 0) + 1;
    const inFlight = Math.max(0, (row?.in_flight || 1) - 1);
    const nowIso = new Date().toISOString();

    if (row) {
      await supabase
        .from('ai_photo_daily_usage')
        .update({
          used_count: used,
          in_flight: inFlight,
          timezone,
          updated_at: nowIso
        })
        .eq('firebase_uid', uid)
        .eq('local_date', localDay);
    } else {
      await supabase
        .from('ai_photo_daily_usage')
        .insert({
          firebase_uid: uid,
          local_date: localDay,
          used_count: used,
          in_flight: inFlight,
          timezone,
          updated_at: nowIso
        });
    }
    return used;
  } catch (err) {
    fastify.log.warn({ err: err.message }, 'Failed to commit photo quota to Supabase in dev/test');
    return (photoDailyUsage.get(key) || 0);
  }
}

export async function releasePhotoReservationAsync(uid, timezone) {
  const localDay = getUserLocalDate(timezone);

  if (!supabase) {
    if (process.env.NODE_ENV === 'production') {
      const err = new Error('Production error: Supabase shared quota storage is strictly required in production mode.');
      err.statusCode = 503;
      throw err;
    }
    releasePhotoReservation(uid, timezone);
    return;
  }

  releasePhotoReservation(uid, timezone);

  if (process.env.NODE_ENV === 'production') {
    if (typeof supabase.rpc !== 'function') {
      const err = new Error('Production error: Supabase RPC release_photo_reservation is required in production mode.');
      err.statusCode = 503;
      throw err;
    }
    const { error: rpcError } = await supabase.rpc('release_photo_reservation', {
      p_uid: uid,
      p_date: localDay
    });
    if (rpcError) {
      fastify.log.error({ error: rpcError.message, uid }, 'Production release_photo_reservation RPC error');
      const err = new Error(`Production database error: ${rpcError.message}`);
      err.statusCode = 503;
      throw err;
    }
    return;
  }

  try {
    if (typeof supabase.rpc === 'function') {
      const { error: rpcError } = await supabase.rpc('release_photo_reservation', {
        p_uid: uid,
        p_date: localDay
      });
      if (!rpcError) return;
    }

    const { data: row } = await supabase
      .from('ai_photo_daily_usage')
      .select('*')
      .eq('firebase_uid', uid)
      .eq('local_date', localDay)
      .maybeSingle();

    if (row) {
      const inFlight = Math.max(0, (row.in_flight || 1) - 1);
      await supabase
        .from('ai_photo_daily_usage')
        .update({
          in_flight: inFlight,
          updated_at: new Date().toISOString()
        })
        .eq('firebase_uid', uid)
        .eq('local_date', localDay);
    }
  } catch (err) {
    fastify.log.warn({ err: err.message }, 'Failed to release photo reservation in Supabase in dev/test');
  }
}

export async function resetPhotoUsageForTestingAsync(uid, localDay) {
  resetPhotoUsageForTesting(uid, localDay);

  if (supabase) {
    try {
      if (localDay) {
        await supabase
          .from('ai_photo_daily_usage')
          .delete()
          .eq('firebase_uid', uid)
          .eq('local_date', localDay);
      } else {
        await supabase
          .from('ai_photo_daily_usage')
          .delete()
          .eq('firebase_uid', uid);
      }
    } catch (_) {}
  }
}

// -------------------------------------------------------------
// 5. POST /ai/chat
// -------------------------------------------------------------
fastify.post('/ai/chat', { bodyLimit: 4 * 1024 * 1024 }, async (request, reply) => {
  // Fail-closed in production if Supabase shared storage is unconfigured
  if (process.env.NODE_ENV === 'production' && !supabase) {
    fastify.log.error('Production fail-closed: Supabase shared storage is required for /ai/chat');
    return reply.code(503).send({
      error: 'ServiceUnavailable',
      message: 'Shared quota service is required in production but currently unconfigured.'
    });
  }

  const { messages = [], userContext = {} } = request.body || {};

  if (!messages || messages.length === 0) {
    return reply.code(400).send({ error: 'messages array is required' });
  }

  const uid = request.user?.uid || 'anonymous';
  const rawTz = request.headers['x-user-timezone'] || userContext?.timezone;
  const timezone = await resolveUserTimezoneAsync(uid, rawTz);
  const isPro = determineUserIsPro(request);

  // 0. Safety Pre-check: Immediate crisis, self-harm, and eating disorder interceptor
  const lastUserMsg = messages[messages.length - 1]?.content || '';
  if (checkCoachSafetyCrisis(lastUserMsg, messages)) {
    fastify.log.warn({ uid }, 'Coach safety crisis pre-check triggered');
    appendBackendLog(`COACH_CRISIS_INTERCEPTED: uid=${uid}`);
    return reply.send({
      role: 'assistant',
      content: CRISIS_SUPPORT_RESPONSE,
      model: 'safety-guardrail',
      crisisIntercepted: true
    });
  }

  // 1. Atomic Quota Reservation: Free limit = 5/day, Pro limit = 50/day
  // Concurrency-safe: checks (usedToday + inFlight) < limit and reserves slot (shared in Supabase or local memory)
  let reservation;
  try {
    reservation = await tryReserveAiCoachQuotaAsync(uid, timezone, isPro);
  } catch (err) {
    if (process.env.NODE_ENV === 'production' || err.statusCode === 503) {
      fastify.log.error({ err: err.message, uid }, 'Quota reservation failed in production mode with 503');
      return reply.code(503).send({
        error: 'ServiceUnavailable',
        message: 'AI coach service is temporarily unavailable. Please try again shortly.'
      });
    }
    throw err;
  }
  if (!reservation.allowed) {
    fastify.log.warn({ uid, isPro, usedToday: reservation.usedToday, dailyLimit: reservation.dailyLimit }, 'AI Coach daily quota reached');
    return reply.code(429).send({
      error: 'QuotaExceeded',
      message: isPro 
        ? "You've reached today's Pro AI Coach limit." 
        : "You've reached today's free AI Coach limit. Your messages reset tomorrow.",
      usedToday: reservation.usedToday,
      dailyLimit: reservation.dailyLimit,
      remaining: 0,
      isPro,
      localDay: reservation.localDay
    });
  }

  if (!geminiClient || !GEMINI_API_KEY) {
    appendBackendLog('GEMINI_CONFIG_ERROR: missing GEMINI_API_KEY in chat');
    await releaseAiCoachReservationAsync(uid, timezone);
    return reply.code(500).send({
      error: 'InternalServerError',
      message: 'AI coach service is temporarily unavailable.'
    });
  }

  let committed = false;
  try {
    fastify.log.info({ messageCount: messages.length, provider: 'gemini', model: CHAT_MODEL, quotaRemaining: reservation.remaining }, 'Processing AI Coach Chat');

    const systemInstruction = buildCoachSystemInstruction(userContext);

    // Call Gemini 2.5 Flash
    try {
        // Format messages for Gemini (role 'user' | 'model')
        const contents = messages.map(m => ({
          role: m.role === 'assistant' ? 'model' : 'user',
          parts: [{ text: m.content || '' }]
        }));

        const { response: geminiResponse, modelUsed } = await executeGeminiWithFallback({
          contents,
          config: {
            systemInstruction,
            temperature: 0.3,
            tools: GEMINI_TOOLS
          }
        }, 'ai-chat');

        const functionCalls = geminiResponse.functionCalls;
        if (functionCalls && functionCalls.length > 0) {
          const call = functionCalls[0];
          const fnName = call.name;
          const fnArgs = call.args || {};
          fastify.log.info({ fnName, fnArgs }, 'Gemini triggered function call');

          const { toolOutput, suggestedAction, items, mealType } = executeTool(fnName, fnArgs, userContext);
          const finalReply = buildDefaultReply(fnName, toolOutput, mealType, items);

          // Success: atomically commit quota reservation & write to shared store / disk
          await commitAiCoachQuotaAsync(uid, timezone);
          committed = true;

          return {
            reply: finalReply,
            toolInvoked: fnName,
            toolData: toolOutput,
            suggestedAction,
            model: modelUsed
          };
        }

        if (geminiResponse.text) {
          // Success: atomically commit quota reservation & write to shared store / disk
          await commitAiCoachQuotaAsync(uid, timezone);
          committed = true;

          return {
            reply: geminiResponse.text.trim(),
            suggestedAction: null,
            model: modelUsed
          };
        }

        throw new Error('Gemini returned empty text and no function calls');
      } catch (geminiError) {
        fastify.log.error({ err: geminiError.message }, 'Gemini chat call failed');
        appendBackendLog(`GEMINI_CHAT_FAILED: ${geminiError.message}`);
        if (!committed) {
          await releaseAiCoachReservationAsync(uid, timezone);
        }
        return sendAiUnavailable(reply, geminiError);
      }
  } catch (chatError) {
    if (!committed) {
      await releaseAiCoachReservationAsync(uid, timezone);
    }
    throw chatError;
  }
});

// Non-production test/diagnostic endpoints for AI Coach quota verification
if (process.env.NODE_ENV !== 'production') {
  fastify.get('/ai/coach-usage', async (request, reply) => {
    const uid = request.user?.uid || 'test-user';
    const rawTz = request.headers['x-user-timezone'];
    const timezone = resolveUserTimezone(uid, rawTz);
    const isPro = determineUserIsPro(request);
    return await getAiCoachUsageAsync(uid, timezone, isPro);
  });

  fastify.post('/ai/coach-usage/reset', async (request, reply) => {
    const uid = request.user?.uid || 'test-user';
    const { localDay } = request.body || {};
    await resetAiCoachUsageForTestingAsync(uid, localDay);
    return { success: true, message: `Reset AI Coach usage for ${uid}` };
  });

  fastify.post('/ai/coach-usage/set', async (request, reply) => {
    const uid = request.user?.uid || 'test-user';
    const rawTz = request.headers['x-user-timezone'];
    const timezone = resolveUserTimezone(uid, rawTz);
    const { count = 0, date } = request.body || {};
    const localDay = date || getUserLocalDate(timezone);
    aiCoachDailyUsage.set(`${uid}:${localDay}`, count);
    persistUsageToDisk();
    return { success: true, key: `${uid}:${localDay}`, count };
  });

  fastify.get('/ai/photo-usage', async (request, reply) => {
    const uid = request.user?.uid || 'test-user';
    const rawTz = request.headers['x-user-timezone'];
    const timezone = resolveUserTimezone(uid, rawTz);
    const isPro = determineUserIsPro(request);
    return getPhotoUsage(uid, timezone, isPro);
  });

  fastify.post('/ai/photo-usage/reset', async (request, reply) => {
    const uid = request.user?.uid || 'test-user';
    const { localDay } = request.body || {};
    resetPhotoUsageForTesting(uid, localDay);
    return { success: true, message: `Reset Photo analysis usage for ${uid}` };
  });

  fastify.post('/ai/photo-usage/set', async (request, reply) => {
    const uid = request.user?.uid || 'test-user';
    const rawTz = request.headers['x-user-timezone'];
    const timezone = resolveUserTimezone(uid, rawTz);
    const { count = 0, date } = request.body || {};
    const localDay = date || getUserLocalDate(timezone);
    photoDailyUsage.set(`${uid}:${localDay}`, count);
    persistPhotoUsageToDisk();
    return { success: true, key: `${uid}:${localDay}`, count };
  });
}

// -------------------------------------------------------------
// Supabase Client Helper Functions (Phase 10 — Cloud Sync)
// -------------------------------------------------------------

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
// DELETE /account
// Play Store Account Deletion: Purges cloud sync data & user records.
// Idempotent: returns 200 even if no records exist.
// -------------------------------------------------------------
fastify.delete('/account', async (request, reply) => {
  const uid = request.user?.uid;
  if (!uid) {
    return reply.code(401).send({ error: 'Unauthorized', message: 'User not authenticated' });
  }

  const result = {
    purged: true,
    uid,
    timestamp: new Date().toISOString(),
    details: {}
  };

  try {
    if (supabase) {
      // 1. Find meal logs to cascade-delete food log items
      const { data: mealLogs } = await supabase
        .from('meal_logs')
        .select('id')
        .eq('firebase_uid', uid);

      if (mealLogs && mealLogs.length > 0) {
        const mealLogIds = mealLogs.map(m => m.id);
        const { error: itemsErr } = await supabase
          .from('food_log_items')
          .delete()
          .in('meal_log_id', mealLogIds);
        if (itemsErr) result.details.food_log_items = itemsErr.message;
      }

      // 2. Delete meal logs
      const { error: mealsErr } = await supabase
        .from('meal_logs')
        .delete()
        .eq('firebase_uid', uid);
      if (mealsErr) result.details.meal_logs = mealsErr.message;

      // 3. Delete nutrition goals
      const { error: goalsErr } = await supabase
        .from('nutrition_goals')
        .delete()
        .eq('firebase_uid', uid);
      if (goalsErr) result.details.nutrition_goals = goalsErr.message;

      // 4. Delete weight entries
      const { error: weightErr } = await supabase
        .from('weight_entries')
        .delete()
        .eq('firebase_uid', uid);
      if (weightErr) result.details.weight_entries = weightErr.message;

      // 5. Delete water logs
      const { error: waterErr } = await supabase
        .from('water_logs')
        .delete()
        .eq('firebase_uid', uid);
      if (waterErr) result.details.water_logs = waterErr.message;

      // 6. Delete ai analysis logs
      const { error: aiErr } = await supabase
        .from('ai_analysis_log')
        .delete()
        .eq('firebase_uid', uid);
      if (aiErr) result.details.ai_analyses = aiErr.message;

      // 7. Find conversations to cascade-delete messages
      const { data: convs } = await supabase
        .from('conversations')
        .select('id')
        .eq('firebase_uid', uid);

      if (convs && convs.length > 0) {
        const convIds = convs.map(c => c.id);
        const { error: msgsErr } = await supabase
          .from('messages')
          .delete()
          .in('conversation_id', convIds);
        if (msgsErr) result.details.messages = msgsErr.message;
      }

      // 8. Delete conversations
      const { error: convsErr } = await supabase
        .from('conversations')
        .delete()
        .eq('firebase_uid', uid);
      if (convsErr) result.details.conversations = convsErr.message;

      // 9. Delete user profile
      const { error: profileErr } = await supabase
        .from('user_profiles')
        .delete()
        .eq('firebase_uid', uid);
      if (profileErr) result.details.user_profiles = profileErr.message;

      // 10. Delete user subscriptions (entitlement store)
      try {
        await supabase
          .from('user_subscriptions')
          .delete()
          .eq('user_id', uid);
      } catch (_) {}

      // 11. Delete saved meals and saved meal items
      try {
        const { data: savedMeals } = await supabase
          .from('saved_meals')
          .select('id')
          .eq('firebase_uid', uid);
        if (savedMeals && savedMeals.length > 0) {
          await supabase
            .from('saved_meal_items')
            .delete()
            .in('saved_meal_id', savedMeals.map(m => m.id));
        }
        await supabase
          .from('saved_meals')
          .delete()
          .eq('firebase_uid', uid);
      } catch (_) {}

      // 12. Delete recipes and recipe items
      try {
        const { data: recipes } = await supabase
          .from('recipes')
          .select('id')
          .eq('firebase_uid', uid);
        if (recipes && recipes.length > 0) {
          await supabase
            .from('recipe_items')
            .delete()
            .in('recipe_id', recipes.map(r => r.id));
        }
        await supabase
          .from('recipes')
          .delete()
          .eq('firebase_uid', uid);
      } catch (_) {}

      // 13. Delete custom foods
      try {
        await supabase
          .from('custom_foods')
          .delete()
          .eq('firebase_uid', uid);
      } catch (_) {}

      // 14. Delete daily goals (if separate from nutrition_goals)
      try {
        await supabase
          .from('daily_goals')
          .delete()
          .eq('firebase_uid', uid);
      } catch (_) {}

      // 15. Delete AI Coach daily usage records
      try {
        await supabase
          .from('ai_coach_daily_usage')
          .delete()
          .eq('firebase_uid', uid);
      } catch (_) {}

      // 16. Delete AI Photo daily usage records
      try {
        await supabase
          .from('ai_photo_daily_usage')
          .delete()
          .eq('firebase_uid', uid);
      } catch (_) {}
    }

    // Purge AI Coach & Photo usage, in-flight reservations, and timezone cache for this user
    resetAiCoachUsageForTesting(uid);
    resetPhotoUsageForTesting(uid);
    userTimezones.delete(uid);
    persistTimezonesToDisk();

    // Reset rate limiter for this user
    if (typeof rateLimits !== 'undefined' && rateLimits && typeof rateLimits.delete === 'function') {
      rateLimits.delete(uid);
    }

    appendBackendLog(`ACCOUNT PURGED: Successfully purged cloud data for user ${uid}`);
    return reply.code(200).send(result);
  } catch (err) {
    fastify.log.error({ err: err.message, uid }, 'Account purge failed');
    return reply.code(500).send({ error: 'Account purge failed', details: err.message });
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
    console.log(`KcalGrind AI Proxy Backend running on http://${host}:${port} (Provider: ${geminiClient ? `gemini (${GEMINI_MODEL})` : 'unconfigured'})`);
  } catch (err) {
    fastify.log.error(err);
    process.exit(1);
  }
};

export { fastify };

if (process.env.TEST !== 'true') {
  start();
}
