# RabbitMQ / Queue Integration Notes

## What was added

- Added `spring-boot-starter-amqp` to the Backend and `notification-service`.
- Added a durable RabbitMQ direct exchange: `sale.exchange`.
- Added durable queues:
  - `mail.queue` for email jobs.
  - `notification.queue` for in-app notification jobs.
- Backend now publishes password-reset email jobs to `mail.queue`.
- `notification-service` now consumes `mail.queue` and sends email through Gmail SMTP.
- Backend now publishes order notification jobs to `notification.queue`.
- Backend consumes `notification.queue` and persists in-app notifications to MySQL.
- Added RabbitMQ and `notification-service` to `docker-compose.yml`.
- Added RabbitMQ Management UI port mapping: `15672`.

## Run with Docker Compose

```bash
docker compose up --build
```

RabbitMQ Management UI:

```text
http://localhost:15672
```

Default credentials:

```text
username: guest
password: guest
```

## Email configuration

Set Gmail SMTP values before running if you want real email delivery:

```bash
export GMAIL_USERNAME="your-email@gmail.com"
export GMAIL_APP_PASSWORD="your-gmail-app-password"
docker compose up --build
```

## Queue names and routing keys

| Purpose | Queue | Routing Key |
|---|---|---|
| Email sending | `mail.queue` | `mail.send` |
| In-app notification creation | `notification.queue` | `notification.create` |

## Fallback behavior

- If RabbitMQ is unavailable in local/dev mode, Backend falls back to direct SMTP for password reset email.
- If RabbitMQ is unavailable in local/dev mode, Backend falls back to direct in-app notification persistence for order notifications.
