# Kcal Grind AI Backend — Google Cloud Run & Secret Manager Deployment Guide

> [!IMPORTANT]
> **Current Status: Infrastructure Prepared — Deployment Pending.**
> The Docker container configuration (`backend/Dockerfile`), build exclusions (`backend/.dockerignore`), Secret Manager templates, and automated deployment script (`backend/deploy-cloud-run.sh`) have been authored and verified locally.
> **Remote deployment to Google Cloud Run has NOT been executed yet.** The backend is currently running on localhost (`http://localhost:8000` / `http://10.0.2.2:8000` on Android emulator).
> Once Google Cloud credentials and gcloud CLI are provisioned, run `backend/deploy-cloud-run.sh` and populate the resulting URL into `local.properties` as `PROD_BACKEND_URL`.

This document outlines the architecture, secret management, and step-by-step procedures for deploying the Kcal Grind AI Proxy backend off localhost and onto Google Cloud Run when ready.

---

## 1. Architecture Overview

```
                      +-----------------------------+
                      |   Android Client (Release)  |
                      |   Firebase Auth (Bearer JWT)|
                      +--------------+--------------+
                                     | HTTPS
                                     v
                  +------------------+------------------+
                  |    Google Cloud Run (Serverless)    |
                  |    - Container: node:22-alpine      |
                  |    - Fastify (Port 8080)            |
                  |    - Firebase Admin Token Verify    |
                  |    - Rate Limiter (30 calls/hr/UID) |
                  +--------+-------------------+--------+
                           |                   |
     Secret Manager Mounts |                   | External AI API
                           v                   v
            +--------------+----+    +---------+-----------+
            | Secret Manager    |    | Google Gemini 2.5   |
            | - kcalgrindai-    |    | (Primary Provider)  |
            |   gemini          |    +---------------------+
            | - kcalgrindai-    |    | NVIDIA Integrate    |
            |   nvidia          |    | (Fallback Provider) |
            +-------------------+    +---------------------+
```

### Key Specifications:
- **Serverless Scaling:** 0 to 10 instances with automatic scale-to-zero when idle (zero cost during inactive hours).
- **Resource Allocation:** 512 MiB RAM, 1 vCPU per container instance.
- **Request Timeout:** 60 seconds (accommodates high-resolution multimodal vision analysis).
- **Concurrency:** 80 requests per instance.
- **Authentication:** All `/ai/*` routes require a valid Firebase ID token; `/health` is public for container health checks.

---

## 2. Cloud Secret Manager Configuration

API keys are **never** stored in environment variables, git repositories, or container image layers. They are mounted directly from Cloud Secret Manager at runtime.

### Secret Names:
1. `kcalgrindai-gemini-api-key`: Google AI Studio / Gemini API key.
2. `kcalgrindai-nvidia-api-key`: NVIDIA API key (fallback provider).

### Provisioning Commands:
```bash
# 1. Create the secrets
gcloud secrets create kcalgrindai-gemini-api-key --replication-policy="automatic"
gcloud secrets create kcalgrindai-nvidia-api-key --replication-policy="automatic"

# 2. Add secret versions securely (typing hidden or via file)
echo -n "YOUR_GEMINI_API_KEY" | gcloud secrets versions add kcalgrindai-gemini-api-key --data-file=-
echo -n "YOUR_NVIDIA_API_KEY" | gcloud secrets versions add kcalgrindai-nvidia-api-key --data-file=-

# 3. Grant Cloud Run Service Account read permissions
PROJECT_NUMBER=$(gcloud projects describe $(gcloud config get-value project) --format="value(projectNumber)")
gcloud secrets add-iam-policy-binding kcalgrindai-gemini-api-key \
  --member="serviceAccount:${PROJECT_NUMBER}-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"

gcloud secrets add-iam-policy-binding kcalgrindai-nvidia-api-key \
  --member="serviceAccount:${PROJECT_NUMBER}-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

---

## 3. Deployment Steps

### Method A: Automated Deployment Script
Run the automated deployment script from the `backend/` directory:
```bash
cd backend
chmod +x deploy-cloud-run.sh
./deploy-cloud-run.sh [PROJECT_ID] [REGION]
```

### Method B: Manual gcloud CLI Deployment
```bash
# 1. Build and push container to Google Artifact Registry / Container Registry
gcloud builds submit --tag gcr.io/${PROJECT_ID}/kcalgrindai-backend:latest .

# 2. Deploy to Cloud Run
gcloud run deploy kcalgrindai-backend \
  --image gcr.io/${PROJECT_ID}/kcalgrindai-backend:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 10 \
  --timeout 60s \
  --set-env-vars="AI_PROVIDER=gemini,GEMINI_MODEL=gemini-2.5-flash,FIREBASE_PROJECT_ID=${PROJECT_ID},NODE_ENV=production" \
  --set-secrets="GEMINI_API_KEY=kcalgrindai-gemini-api-key:latest,NVIDIA_API_KEY=kcalgrindai-nvidia-api-key:latest"
```

---

## 4. Verification & Testing

1. **Verify Health Endpoint:**
   ```bash
   curl -s https://kcalgrindai-backend-xxxxx.a.run.app/health
   ```
   **Expected Response:**
   ```json
   {
     "status": "ok",
     "service": "kcalgrindai-backend",
     "provider": "gemini",
     "visionModel": "gemini-2.5-flash",
     "chatModel": "gemini-2.5-flash"
   }
   ```

2. **Verify 401 Unauthorized Without Token:**
   ```bash
   curl -s -X POST https://kcalgrindai-backend-xxxxx.a.run.app/ai/analyze-text \
     -H "Content-Type: application/json" \
     -d '{"text":"apple"}'
   ```
   **Expected Response:**
   ```json
   {
     "error": "Unauthorized",
     "message": "Missing or invalid Authorization header. A valid Firebase ID token is required."
   }
   ```

---

## 5. Android Client Configuration

In `app/build.gradle.kts`:
- **Debug:** Uses local emulator loopback `http://10.0.2.2:8000/` (or `BACKEND_URL` in `local.properties`).
- **Release:** Reads `PROD_BACKEND_URL` from `local.properties` if configured; otherwise falls back to the local development backend (`http://10.0.2.2:8000/`) so builds remain fully functional without broken or misleading placeholder URLs.

All network requests from the Android app automatically attach the Firebase user's ID token via `AuthInterceptor` on `@Named("AiRetrofit")`.
