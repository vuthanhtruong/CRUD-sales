# RabbitMQ Queue Integration Notes

RabbitMQ trong dự án này nên được trình bày là cơ chế xử lý bất đồng bộ cho **email** và **notification**, đặc biệt trong nghiệp vụ đơn hàng.

## Use case chính nên nói khi bảo vệ

1. User tạo đơn hàng thành công.
2. Admin cập nhật trạng thái đơn hàng.
3. Backend chỉ xử lý nghiệp vụ chính trong transaction: tạo/cập nhật đơn, trừ/hoàn kho, thanh toán/hoàn tiền ví nếu có.
4. Sau khi transaction commit, backend publish message vào RabbitMQ.
5. Consumer xử lý tác vụ phụ:
   - gửi email cho khách hàng qua `mail.queue`;
   - tạo thông báo trong hệ thống qua `notification.queue`.

Cách này giúp API trả response nhanh hơn và tránh để tác vụ phụ như SMTP làm ảnh hưởng giao dịch chính.

## Queue configuration

- Main exchange: `sale.exchange`
- Dead-letter exchange: `sale.dlx`
- Mail queue: `mail.queue`, routing key `mail.send`
- Notification queue: `notification.queue`, routing key `notification.create`
- Mail DLQ: `mail.dlq`, routing key `mail.dead`
- Notification DLQ: `notification.dlq`, routing key `notification.dead`

## Files chính

- `RabbitMQConfig.java`
- `QueuePublisherService.java`
- `MailQueueListener.java`
- `NotificationQueueListener.java`
- `OrderServiceImpl.java`
- `PasswordResetServiceImpl.java`

## Chức năng đang dùng queue

- Quên mật khẩu: publish email reset password vào `mail.queue`.
- Tạo đơn hàng: publish notification và email đơn hàng.
- Cập nhật trạng thái đơn hàng: publish notification và email trạng thái mới.

## Lý do kỹ thuật

- Email và notification không phải phần bắt buộc để transaction đơn hàng đúng.
- SMTP có thể chậm/lỗi, nên nên tách khỏi request chính.
- DLQ giúp giữ lại message lỗi để debug hoặc xử lý lại.
- Producer và consumer tách biệt, code dễ mở rộng sang nhiều loại event hơn.
