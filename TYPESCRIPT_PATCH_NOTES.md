# TypeScript patch notes

Fixed Angular compile error in `src/app/product/product-detail.component.ts`:

- Removed a union-typed `req` variable that could be either `Observable<WishlistItemDTO>` or `Observable<void>`.
- Split wishlist add/remove into separate `subscribe` calls so TypeScript/RxJS overload resolution works correctly.
- Added safe wishlist count update with `Math.max(0, ...)`.

Original error:

```text
TS2349: This expression is not callable.
Each member of the union type ... has signatures, but none of those signatures are compatible with each other.
```

Run again:

```bash
cd Frontend/untitled
ng serve
```
