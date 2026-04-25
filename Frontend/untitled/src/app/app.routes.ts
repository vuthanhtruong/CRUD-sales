import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { AdminComponent } from './admin/admin.component';
import { ProductsComponent } from './product/products.component';
import { ColorsComponent } from './product/colors.component';
import { ProductTypeComponent } from './product/product-type.component';
import { SizesComponent } from './product/sizes.component';
import { ProductDetailComponent } from './product/product-detail.component';
import { ProfileComponent } from './auth/profile.component';
import { OrdersComponent } from './orders/orders.component';
import { AdminOrdersComponent } from './admin-orders/admin-orders.component';
import { WishlistComponent } from './wishlist/wishlist.component';
import { AddressesComponent } from './addresses/addresses.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { AdminCouponsComponent } from './admin-coupons/admin-coupons.component';
import { AdminReviewsComponent } from './admin-reviews/admin-reviews.component';
import { SupportComponent } from './support/support.component';
import { AdminSupportComponent } from './admin-support/admin-support.component';
import { AdminCommentsComponent } from './admin-comments/admin-comments.component';
import { WalletComponent } from './wallet/wallet.component';
import { AdminWalletComponent } from './admin-wallet/admin-wallet.component';
import { adminGuard, profileGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'reset-password', component: HomeComponent },
  { path: 'product/:id', component: ProductDetailComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [profileGuard] },
  { path: 'orders', component: OrdersComponent, canActivate: [profileGuard] },
  { path: 'wishlist', component: WishlistComponent, canActivate: [profileGuard] },
  { path: 'addresses', component: AddressesComponent, canActivate: [profileGuard] },
  { path: 'notifications', component: NotificationsComponent, canActivate: [profileGuard] },
  { path: 'support', component: SupportComponent, canActivate: [profileGuard] },
  { path: 'wallet', component: WalletComponent, canActivate: [profileGuard] },
  { path: 'admin', component: AdminComponent, canActivate: [adminGuard] },
  { path: 'admin/orders', component: AdminOrdersComponent, canActivate: [adminGuard] },
  { path: 'admin/coupons', component: AdminCouponsComponent, canActivate: [adminGuard] },
  { path: 'admin/reviews', component: AdminReviewsComponent, canActivate: [adminGuard] },
  { path: 'admin/support', component: AdminSupportComponent, canActivate: [adminGuard] },
  { path: 'admin/comments', component: AdminCommentsComponent, canActivate: [adminGuard] },
  { path: 'admin/wallet', component: AdminWalletComponent, canActivate: [adminGuard] },
  { path: 'products', component: ProductsComponent, canActivate: [adminGuard] },
  { path: 'product-types', component: ProductTypeComponent, canActivate: [adminGuard] },
  { path: 'sizes', component: SizesComponent, canActivate: [adminGuard] },
  { path: 'colors', component: ColorsComponent, canActivate: [adminGuard] },
  { path: '**', redirectTo: 'home' }
];
