# RabbitMQ / Queue Integration Notes

## Trạng thái sau chỉnh sửa

Dự án đã chuyển về **monolith**. Backend Spring Boot là service duy nhất publish và consume queue.

## Queue chính

| Purpose | Queue | Routing Key | Consumer |
|---|---|---|---|
| Gửi email reset mật khẩu | `mail.queue` | `mail.send` | Backend `MailQueueListener` |
| Tạo in-app notification | `notification.queue` | `notification.create` | Backend `NotificationQueueListener` |

## Dead-letter queue

| Source Queue | DLQ | DLQ Routing Key |
|---|---|---|
| `mail.queue` | `mail.dlq` | `mail.dead` |
| `notification.queue` | `notification.dlq` | `notification.dead` |

Message xử lý lỗi sẽ được đưa vào DLQ thay vì bị mất. Điều này phù hợp với monolith vì vẫn đơn giản nhưng có đường kiểm tra lỗi queue rõ ràng.

## Fallback local/dev

- Nếu RabbitMQ chưa chạy, password-reset flow fallback sang gửi SMTP trực tiếp.
- Nếu SMTP chưa cấu hình, backend in reset link ra console để dev tiếp tục test được.
- Nếu RabbitMQ chạy nhưng SMTP lỗi, mail job sẽ dead-letter để tránh request bị treo hoặc message mất im lặng.
