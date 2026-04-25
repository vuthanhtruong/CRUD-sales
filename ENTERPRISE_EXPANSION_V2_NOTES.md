# Enterprise Expansion V2

This upgrade expands the sales app into a richer e-commerce system with community, support, analytics and performance tuning.

## New backend modules

### Product comments / discussion
- Threaded product comments with parent replies.
- Public product discussion endpoint.
- Logged-in users can post comments and replies.
- Helpful counter for comments.
- Admin moderation: publish, hide, pending, delete.

### Support desk
- User support tickets with category and priority.
- Ticket message thread between user and admin.
- Admin internal notes.
- Ticket status workflow: OPEN, WAITING_ADMIN, WAITING_CUSTOMER, RESOLVED, CLOSED.

### Product insight / trending
- Product view metrics stored per product.
- Product detail records a view.
- Trending endpoint returns top viewed products.
- Redis cache for trending products, evicted when views are recorded.

## Frontend additions

- New Nova Commerce visual style with glass cards, gradient depth, sharper panels and modern dashboards.
- `/support`: user ticket center.
- `/admin/support`: admin ticket desk.
- `/admin/comments`: admin product discussion moderation.
- Product detail now includes:
  - product discussion,
  - nested replies,
  - helpful votes,
  - live view count.
- Home page now includes trending products powered by backend metrics.

## Query/performance tuning

- Added performance-oriented JPA/Hibernate settings:
  - default batch fetch size,
  - JDBC batch size,
  - ordered inserts/updates,
  - Hikari pool sizing,
  - disabled noisy SQL bind logging by default.
- Added indexes for high-frequency lookup paths:
  - product status/type/price/name,
  - order user/status/date,
  - product variant product/quantity and size/color,
  - product image product/primary,
  - product comments by product/status/date,
  - support tickets by user/status/date,
  - product metrics by views/date.

## Important runtime note

Maven wrapper could not download Maven from Maven Central in the sandbox, so backend compile could not be verified here. Run locally:

```bash
cd Backend
chmod +x mvnw
./mvnw clean package -DskipTests
```

Frontend should be checked locally with:

```bash
cd Frontend/untitled
npm install
ng serve
```
