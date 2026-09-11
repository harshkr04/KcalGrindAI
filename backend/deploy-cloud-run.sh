#!/usr/bin/env bash
# ==============================================================================
# Lumina AI Backend — Google Cloud Run Automated Deployment Script
# ==============================================================================
# Usage:
#   chmod +x deploy-cloud-run.sh
#   ./deploy-cloud-run.sh [PROJECT_ID] [REGION]
#
# Prerequisites:
#   - Google Cloud SDK (gcloud) installed and authenticated (`gcloud auth login`)
#   - Required roles: Cloud Run Admin, Secret Manager Admin, Artifact Registry Admin
# ==============================================================================

set -euo pipefail

PROJECT_ID="${1:-lumina-nutrition-app}"
REGION="${2:-us-central1}"
SERVICE_NAME="lumina-ai-backend"
IMAGE_NAME="gcr.io/${PROJECT_ID}/${SERVICE_NAME}:latest"

echo "=========================================================="
echo " Deploying Lumina AI Proxy Backend to Google Cloud Run"
echo " Project: ${PROJECT_ID}"
echo " Region:  ${REGION}"
echo " Service: ${SERVICE_NAME}"
echo "=========================================================="

# 1. Ensure project is active
gcloud config set project "${PROJECT_ID}"

# 2. Enable necessary GCP APIs
echo "--> Enabling required Google Cloud APIs..."
gcloud services enable \
  run.googleapis.com \
  secretmanager.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com

# 3. Create Secrets in Cloud Secret Manager (if they don't already exist)
echo "--> Verifying Secret Manager secrets..."

create_secret_if_missing() {
  local SECRET_NAME="$1"
  if ! gcloud secrets describe "${SECRET_NAME}" --project="${PROJECT_ID}" >/dev/null 2>&1; then
    echo "Creating secret: ${SECRET_NAME}..."
    gcloud secrets create "${SECRET_NAME}" \
      --replication-policy="automatic" \
      --project="${PROJECT_ID}"
    echo "Please set secret value manually using: gcloud secrets versions add ${SECRET_NAME} --data-file=/path/to/key"
  else
    echo "Secret ${SECRET_NAME} exists."
  fi
}

create_secret_if_missing "lumina-gemini-api-key"
create_secret_if_missing "lumina-nvidia-api-key"

# 4. Build and submit container image via Cloud Build
echo "--> Building container image via Google Cloud Build..."
gcloud builds submit --tag "${IMAGE_NAME}" .

# 5. Grant Cloud Run Service Account access to Secret Manager
PROJECT_NUMBER=$(gcloud projects describe "${PROJECT_ID}" --format="value(projectNumber)")
CLOUD_RUN_SA="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"

echo "--> Granting Secret Accessor permission to Cloud Run Service Account (${CLOUD_RUN_SA})..."
gcloud secrets add-iam-policy-binding "lumina-gemini-api-key" \
  --member="serviceAccount:${CLOUD_RUN_SA}" \
  --role="roles/secretmanager.secretAccessor" \
  --project="${PROJECT_ID}" >/dev/null

gcloud secrets add-iam-policy-binding "lumina-nvidia-api-key" \
  --member="serviceAccount:${CLOUD_RUN_SA}" \
  --role="roles/secretmanager.secretAccessor" \
  --project="${PROJECT_ID}" >/dev/null

# 6. Deploy to Google Cloud Run
echo "--> Deploying service to Cloud Run..."
gcloud run deploy "${SERVICE_NAME}" \
  --image="${IMAGE_NAME}" \
  --platform="managed" \
  --region="${REGION}" \
  --allow-unauthenticated \
  --memory="512Mi" \
  --cpu="1" \
  --min-instances=0 \
  --max-instances=10 \
  --concurrency=80 \
  --timeout="60s" \
  --set-env-vars="AI_PROVIDER=gemini,GEMINI_MODEL=gemini-2.5-flash,FIREBASE_PROJECT_ID=${PROJECT_ID},NODE_ENV=production" \
  --set-secrets="GEMINI_API_KEY=lumina-gemini-api-key:latest,NVIDIA_API_KEY=lumina-nvidia-api-key:latest"

# 7. Print output service URL
SERVICE_URL=$(gcloud run services describe "${SERVICE_NAME}" --platform="managed" --region="${REGION}" --format="value(status.url)")
echo "=========================================================="
echo " Deployment Complete!"
echo " Service URL: ${SERVICE_URL}"
echo " Health check: ${SERVICE_URL}/health"
echo " Update app/build.gradle.kts release BACKEND_URL to: ${SERVICE_URL}/"
echo "=========================================================="
