# syntax=docker/dockerfile:1

# ==============================================================================
# Dependencias compartidas por desarrollo y producción
# ==============================================================================
FROM node:24-alpine AS deps

WORKDIR /app

ENV NG_CLI_ANALYTICS=false

COPY package.json package-lock.json ./

# npm ci es reproducible y respeta el lockfile.
RUN npm ci --legacy-peer-deps


# ==============================================================================
# Desarrollo: `ng serve` con hot reload (usado por docker-compose.yml)
# ==============================================================================
FROM deps AS dev

COPY . .

EXPOSE 4200

CMD ["npm", "start", "--", "--host", "0.0.0.0", "--poll", "2000"]


# ==============================================================================
# Build de producción
# ==============================================================================
FROM deps AS build

COPY . .

RUN npm run build


# ==============================================================================
# Producción: Nginx sirviendo el bundle estático (stage por defecto)
# ==============================================================================
FROM nginx:1.27-alpine AS production

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget -qO- http://127.0.0.1/healthz >/dev/null || exit 1

CMD ["nginx", "-g", "daemon off;"]
