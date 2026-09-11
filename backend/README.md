# Lumina AI Proxy Backend

A local Node.js + Fastify service that securely proxies AI nutrition analysis and chat requests to NVIDIA's OpenAI-compatible API (`https://integrate.api.nvidia.com/v1`).

---

## 1. Setup & Installation

From the `LuminaAndroid/backend/` directory:

```bash
npm install
```

## 2. Environment Configuration

Copy `.env.example` to `.env` and provide your NVIDIA API Key:

```env
PORT=8000
HOST=0.0.0.0
NVIDIA_API_KEY=nvapi-your-key-here
VISION_MODEL=meta/llama-3.2-90b-vision-instruct
TEXT_MODEL=nvidia/nemotron-3.5-lightning-30b-a3b
CHAT_MODEL=meta/llama-3.3-70b-instruct
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
| `/health` | GET | None | `{ status: "ok" }` |
| `/ai/analyze-photo` | POST | `{ imageBase64, dietTags?, allergies? }` | `{ foods: [...], overallConfidence }` |
| `/ai/analyze-text` | POST | `{ text, dietTags?, allergies? }` | `{ foods: [...], overallConfidence }` |
| `/ai/transcribe` | POST | `{ transcript, dietTags?, allergies? }` | `{ foods: [...], overallConfidence }` |
| `/ai/chat` | POST | `{ messages: [...], userContext: {...} }` | `{ reply, suggestedAction }` |
