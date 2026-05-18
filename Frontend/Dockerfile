# syntax=docker/dockerfile:1

FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build

FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build /app/dist /tmp/dist

RUN set -eux; \
    APP_DIR="$(find /tmp/dist -type f -name index.html -exec dirname {} \; | head -n 1)"; \
    test -n "$APP_DIR"; \
    cp -r "$APP_DIR"/. /usr/share/nginx/html/; \
    rm -rf /tmp/dist

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
