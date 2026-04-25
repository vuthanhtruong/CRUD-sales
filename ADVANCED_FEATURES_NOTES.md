# Advanced E-commerce Expansion Notes

This version extends the original CRUD Sales project into a broader e-commerce management system.

## Backend additions

### Customer features
- Wishlist / saved products
- Address book with default shipping address
- Notifications inbox
- Product reviews and rating summaries
- Coupon validation at checkout
- Order timeline events

### Admin features
- Coupon management: percentage / fixed amount, minimum order value, max discount, usage limit, activation window
- Review moderation: pending / approved / rejected, admin reply
- Advanced dashboard endpoints:
  - Revenue trend
  - Order status distribution
  - Top-selling products
  - Low-stock variants

### Order/checkout improvements
- Orders now store subtotal, discount amount, final total and coupon code.
- Checkout creates a first timeline event.
- Admin status updates create timeline events and user notifications.
- Cancelling an order restores inventory when appropriate.

## Frontend additions

### New customer pages
- `/wishlist`
- `/addresses`
- `/notifications`

### New admin pages
- `/admin/coupons`
- `/admin/reviews`

### UI upgrades
- Navigation expanded for customer and admin workflows.
- Product detail now includes wishlist action, public reviews, rating summary and review submission.
- Cart checkout now supports coupon preview and discounted payable total.
- Orders and admin orders show coupon/discount/timeline information.
- Admin dashboard now shows trend/status/top-product panels.

## Notes

The Maven wrapper cannot download Maven in this sandbox because external dependency download failed. The Angular CLI is also unavailable because `node_modules` is not installed in the sandbox. The code has been updated statically and packaged as a full ZIP for local build verification.
