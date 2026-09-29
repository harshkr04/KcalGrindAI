# Production Dockerfile for Kcal Grind AI Proxy Backend
# Multi-stage build on Alpine Linux for minimal attack surface and image size (<150MB)

FROM node:22-alpine AS builder

WORKDIR /app

# Install dependencies (only production deps)
COPY package*.json ./
RUN npm ci --omit=dev && npm cache clean --force

# Production Runner
FROM node:22-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV PORT=8080
ENV HOST=0.0.0.0

# Create non-root app user security context
USER node

# Copy dependencies and application source
COPY --chown=node:node --from=builder /app/node_modules ./node_modules
COPY --chown=node:node package*.json ./
COPY --chown=node:node src/ ./src/

EXPOSE 8080

# Health check to ensure Cloud Run container liveness
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://127.0.0.1:8080/health || exit 1

CMD ["node", "src/server.js"]
