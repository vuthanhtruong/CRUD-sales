# CRUD Sales - Completion Notes

This completed version focuses on stability, security and practical e-commerce features.

## Added features

### Backend
- Real order workflow with `SalesOrder` and `OrderItem` entities.
- Checkout creates an order, stores receiver information, payment method and order items.
- User order history endpoint: `GET /api/orders/me`.
- Admin order management endpoint: `GET /api/orders`, `PUT /api/orders/{id}/status`.
- Order statuses: `PENDING`, `CONFIRMED`, `SHIPPING`, `COMPLETED`, `CANCELLED`.
- Payment methods: `COD`, `BANK_TRANSFER`, `CARD`.
- Admin dashboard stats endpoint: `GET /api/dashboard/stats`.
- Stock is restored when an order is cancelled before completion.
- Email is now saved and returned in account/profile data.

### Frontend
- Angular standalone HTTP setup fixed with `provideHttpClient(withInterceptors(...))`.
- User checkout form added inside cart modal.
- New user order history page: `/orders`.
- New admin order operations page: `/admin/orders`.
- Admin dashboard now shows live stats.
- Profile page now supports email.
- Navigation updated for My Orders and Admin Orders.

## Important fixes
- Fixed missing Angular `HttpClient` provider.
- Registered the auth interceptor globally.
- Hardened `/api/variants/**`; stock-changing endpoints are no longer public.
- Cart endpoints are restricted to `USER` role.
- Cart service verifies that each cart item belongs to the current user.
- Profile update now uses current authenticated user instead of trusting path username.
- Fixed product type search JPQL from `productType.productTypeId` to `productType.id`.
- Fixed `AccountDTO` mapping so password hash is not returned as username.
- Added stronger validation for login, register, profile and cart requests.

## Run notes
Backend still expects MySQL and Redis according to `Backend/src/main/resources/application.properties`.
Default seeded admin:
- username: `admin`
- password: `admin123`

Recommended next step before production: move database credentials and JWT secret to environment variables.
