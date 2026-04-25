# Microservices handoff

This project now includes a starter `notification-service` microservice.

- Main monolith backend: `Backend` on port `8080`
- Notification microservice: `microservices/notification-service` on port `8081`

The main backend already has built-in password reset email support. The notification microservice is provided as a separate deployable service for customers who want to split mail delivery out of the commerce API.

Run it:

```bash
cd microservices/notification-service
mvn spring-boot:run
```

Set Gmail credentials with environment variables:

```bash
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-gmail-app-password
```
