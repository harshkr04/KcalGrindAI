# Kcal Grind AI Proxy Backend

A Node.js + Fastify service that securely proxies AI nutrition analysis and chat requests to Google Gemini 2.5 Flash.

---

## 1. Setup & Installation

From the `backend/` directory:

```bash
npm install
```

## 2. Environment Configuration

Copy `.env.example` to `.env` and provide your Google Gemini API Key:

```env
PORT=8000
HOST=0.0.0.0
GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-2.5-flash
FIREBASE_PROJECT_ID=kcal-grind-ai
```

## 3. Running the Server

- Development mode (with auto-reload):
  ```bash
  npm run dev
  ```

- Production mode:
  ```bash
  npm start
  ```

The server will listen on `http://0.0.0.0:8000`. From the Android emulator, it is reachable at `http://10.0.2.2:8000`.

---

## 4. Endpoints

| Endpoint | Method | Input Payload | Output |
|---|---|---|---|
| `/health` | GET | None | `{ status: "ok", provider: "gemini" }` |
| `/ai/analyze-photo` | POST | `{ imageBase64, dietTags?, allergies? }` | `{ foods: [...], overallConfidence }` |
| `/ai/analyze-text` | POST | `{ text, dietTags?, allergies? }` | `{ foods: [...], overallConfidence }` |
| `/ai/transcribe` | POST | `{ transcript, dietTags?, allergies? }` | `{ foods: [...], overallConfidence }` |
| `/ai/chat` | POST | `{ messages: [...], userContext: {...} }` | `{ reply, suggestedAction }` |
