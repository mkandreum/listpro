# syntax=docker/dockerfile:1.4
# Se construye desde la raíz del repo: docker build -f server/Dockerfile .

# ── 1. Web (React) ─────────────────────────────────────────────
FROM node:22-slim AS web
WORKDIR /app/web
COPY web/package*.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --no-audit --no-fund
COPY web/ ./
RUN npm run build

# ── 2. Servidor ───────────────────────────────────────────────
FROM node:22-slim AS runner
WORKDIR /app/server
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
ENV NODE_ENV=production PORT=3000 DB_PATH=/data/listpro.db MERCADONA_WH=mad1 SCRAPE_HOUR=6
COPY server/package*.json ./
RUN --mount=type=cache,target=/root/.npm npm install --omit=dev --no-audit --no-fund
COPY server/src ./src
COPY --from=web /app/server/public ./public
VOLUME /data
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 CMD curl -fs http://localhost:3000/api/health || exit 1
CMD ["npm", "start"]
