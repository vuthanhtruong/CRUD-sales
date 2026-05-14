# CRUD Sales / Aster Store Monolith

Dự án đã được chỉnh về kiến trúc **monolith**: một backend Spring Boot duy nhất xử lý API, Redis cache và RabbitMQ queue. Không còn service notification tách riêng.

## Thành phần

- `Backend/`: Spring Boot 3.3, Java 17, MySQL, Redis cache, RabbitMQ queue, Gmail SMTP.
- `Frontend/untitled/`: Angular app.
- `docker-compose.yml`: chạy đầy đủ backend, frontend, MySQL, Redis và RabbitMQ.

## Redis dùng để làm gì?

Redis được dùng cho cache đọc nhiều, ghi ít:

- `products`: danh sách/số trang/số lượng sản phẩm phía admin.
- `product`: chi tiết sản phẩm theo id.
- `userProducts`: danh sách sản phẩm phía storefront.
- `trendingProducts`: sản phẩm trending, TTL ngắn hơn vì metric thay đổi thường xuyên.

Cache được xoá khi dữ liệu sản phẩm hoặc metric view thay đổi, tránh trả dữ liệu cũ quá lâu.

## Queue dùng để làm gì?

RabbitMQ được giữ trong monolith để xử lý việc không cần chạy đồng bộ ngay trong request:

- `mail.queue`: gửi email reset mật khẩu.
- `notification.queue`: tạo notification trong app khi đơn hàng thay đổi.
- `mail.dlq` và `notification.dlq`: dead-letter queue để giữ message lỗi thay vì mất hẳn.

Backend vừa publish vừa consume queue. Cách này vẫn giữ code đơn giản kiểu monolith nhưng không block request bởi email/notification processing.

## Chạy bằng Docker Compose

```bash
docker compose up --build
```

URL mặc định:

- Frontend: `http://localhost:4200`
- Backend API: `http://localhost:8080`
- MySQL host port: `3307`
- Redis host port: `6379`
- RabbitMQ Management UI: `http://localhost:15672`

RabbitMQ mặc định:

```text
username: guest
password: guest
```

## Gmail reset password

Muốn gửi email thật, set biến môi trường trước khi chạy:

```bash
export GMAIL_USERNAME="your-email@gmail.com"
export GMAIL_APP_PASSWORD="your-gmail-app-password"
docker compose up --build
```

Nếu RabbitMQ không chạy khi dev local, backend fallback sang gửi mail trực tiếp. Nếu RabbitMQ chạy nhưng SMTP lỗi, mail job sẽ vào `mail.dlq` để kiểm tra lại.

## Chạy local không Docker

Backend:

```bash
cd Backend
chmod +x mvnw
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

Frontend:

```bash
cd Frontend/untitled
npm ci
npm start
```

`npm start` đã cấu hình Angular proxy để gọi `/api` về `http://localhost:8080`.

## Tài khoản seed mặc định

```text
username: admin
password: admin123
```

## Windows one-command Docker start with Gmail SMTP

For forgot-password email to work, run this in PowerShell from the project root:

```powershell
.\run.ps1
```

The script creates `.env` with Gmail SMTP variables and starts Docker Compose.
Do not commit or share `.env`.

RabbitMQ dashboard:

```text
http://localhost:15672
# guest / guest
```

Verify backend received Gmail variables:

```powershell
docker exec sale_backend printenv | Select-String GMAIL
```
