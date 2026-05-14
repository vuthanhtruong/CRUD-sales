# Aster Store Monolith Handoff

Phiên bản này giữ kiến trúc **monolith** theo yêu cầu: không còn notification microservice tách riêng. Backend Spring Boot xử lý toàn bộ API, Redis cache, RabbitMQ queue consumer, Gmail password reset, wallet, order, product, support, review/comment và admin dashboard.

## Điểm chính

- Bỏ `microservices/notification-service` khỏi source và `docker-compose.yml`.
- Đưa mail queue consumer về backend: `MailQueueListener`.
- Giữ notification queue consumer trong backend: `NotificationQueueListener`.
- Thêm dead-letter queues cho mail và notification.
- Redis cache giữ vai trò cache dữ liệu đọc nhiều: products, product detail, userProducts, trendingProducts.
- Frontend chuyển API base về `/api`, chạy qua Angular proxy khi dev và nginx proxy khi Docker.

## Build commands

Backend:

```bash
cd Backend
chmod +x mvnw
./mvnw clean package -DskipTests
```

Frontend:

```bash
cd Frontend/untitled
npm ci
npm run build
```

Full stack:

```bash
docker compose up --build
```
