# RabbitMQ dùng cho thông báo và email đơn hàng

Bản này đã chuyển use case RabbitMQ chính của dự án sang đúng nghiệp vụ bán hàng hơn: **thông báo và email liên quan đến đơn hàng**.

## Luồng checkout

```text
User checkout
→ Backend kiểm tra cart item, tồn kho, coupon, wallet nếu có
→ Lưu SalesOrder và OrderTimeline
→ Sau khi transaction commit thành công
→ Publish NotificationQueueMessageDTO vào notification.queue
→ Publish MailQueueMessageDTO vào mail.queue
→ Consumer xử lý tạo notification và gửi email
```

## Luồng cập nhật trạng thái đơn hàng

```text
Admin cập nhật trạng thái đơn
→ Backend cập nhật SalesOrder.status và OrderTimeline
→ Nếu huỷ đơn: hoàn kho, hoàn tiền ví nếu cần
→ Sau khi transaction commit thành công
→ Publish NotificationQueueMessageDTO vào notification.queue
→ Publish MailQueueMessageDTO vào mail.queue
→ Consumer xử lý tạo notification và gửi email báo trạng thái mới
```

## File đã chỉnh chính

- `Backend/src/main/java/com/example/demo/service/OrderServiceImpl.java`
  - Thêm publish email khi tạo đơn hàng.
  - Thêm publish email khi cập nhật trạng thái đơn hàng.
  - Giữ publish notification qua RabbitMQ.
  - Dùng `TransactionSynchronizationManager` để publish queue sau khi database transaction commit thành công.
  - Có fallback: nếu RabbitMQ chưa chạy, notification sẽ lưu trực tiếp; email sẽ thử gửi trực tiếp bằng SMTP; nếu SMTP cũng lỗi thì vẫn không làm fail checkout/update status.

- `Backend/src/main/resources/application.properties`
  - Thêm `app.frontend.orders-url` để email có link xem đơn hàng.

- `.env` và `.env.example`
  - Đã thay Gmail thật bằng placeholder để tránh lộ app password.
  - Khi chạy thật, hãy điền lại `GMAIL_USERNAME` và `GMAIL_APP_PASSWORD` của bạn.

- `docker-compose.yml`
  - Thêm biến `APP_FRONTEND_ORDERS_URL` cho backend container.

## Queue đang dùng

- `mail.queue`: gửi email reset password, email tạo đơn, email cập nhật trạng thái đơn.
- `notification.queue`: tạo in-app notification cho user.
- `mail.dlq`: chứa mail message lỗi sau khi consumer reject.
- `notification.dlq`: chứa notification message lỗi sau khi consumer reject.

## Lưu ý khi chạy

Frontend đã bị xoá `node_modules` khỏi zip cho nhẹ. Sau khi giải nén, vào thư mục frontend rồi chạy:

```bash
cd Frontend/untitled
npm install
npm start
```

Backend vẫn chạy bằng Maven/Spring Boot như cũ. Nếu chạy bằng Docker Compose, dùng:

```bash
docker compose up --build
```
