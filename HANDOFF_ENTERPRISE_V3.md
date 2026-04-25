# Aster Store Enterprise V3 handoff

This delivery expands the project into a more complete commerce platform with a redesigned English UI, customer/admin avatars, wallet payments, Gmail password reset, product-image primary selection, admin purchase permissions, and a starter microservice split.

## Major features added

### Identity and profile
- Registration now requires and stores email.
- User and admin profiles support avatar upload as a base64 data URL.
- Forgot-password and reset-password endpoints were added:
  - `POST /api/accounts/forgot-password`
  - `POST /api/accounts/reset-password`
- Gmail SMTP is configured through `application.properties` placeholders. Fill `GMAIL_USERNAME` and `GMAIL_APP_PASSWORD` before production use.

### Admin can shop
- Admin accounts can now use storefront features such as cart, checkout, orders, wallet, wishlist, addresses, comments and reviews.
- Cart creation is lazy, so accounts without a cart get one automatically on first use.

### Wallet and payment ledger
- Added wallet balance and transaction ledger.
- Added wallet top-up requests with admin approval/rejection.
- Added `WALLET` as a payment method.
- Checkout deducts wallet balance transactionally.
- Cancelled wallet-paid orders are refunded to the wallet.
- Admin finance page: `/admin/wallet`.
- Customer wallet page: `/wallet`.

### Product image management
- Existing product images now have a visible `Make primary` action directly in the image manager.
- Uploading the first image automatically makes it primary.
- Uploading a new image marked primary clears the old primary image.
- Deleting the primary image promotes a remaining image.

### UI redesign
- Rebranded the UI as **Aster Store**.
- Rebuilt the shell/navigation with a cleaner modern layout.
- Reworked global tokens, cards, buttons, modal, form and dashboard styles.
- UI copy was converted to English.

### Microservice starter
- Added `microservices/notification-service`, a standalone Spring Boot service for email delivery.
- Added a root `docker-compose.yml` for MySQL and Redis.

## Gmail configuration

Set in `Backend/src/main/resources/application.properties` or environment variables:

```properties
spring.mail.username=${GMAIL_USERNAME:}
spring.mail.password=${GMAIL_APP_PASSWORD:}
app.frontend.reset-password-url=http://localhost:4200/reset-password
```

Use a Gmail App Password, not your normal Gmail password.

## Wallet payment note

The wallet module is a real internal ledger inside the app database. Actual bank/card charging still requires a merchant account and provider credentials. The project is prepared for that handoff by separating top-up requests, admin approval and wallet debits/refunds.

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
npm install
ng serve
```

Notification microservice:

```bash
cd microservices/notification-service
mvn spring-boot:run
```
