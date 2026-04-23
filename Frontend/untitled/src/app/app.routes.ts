import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { AdminComponent } from './admin/admin.component';
import { ProductsComponent } from './product/products.component';
import { ColorsComponent } from './product/colors.component';
import { ProductTypeComponent } from './product/product-type.component';
import {SizesComponent} from './product/sizes.component';
import {ProductDetailComponent} from './product/product-detail.component';
import {ProfileComponent} from './auth/profile.component';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },

  { path: 'home', component: HomeComponent },

  { path: 'admin', component: AdminComponent },

  { path: 'products', component: ProductsComponent },
  { path: 'product-types', component: ProductTypeComponent },
  { path: 'sizes', component: SizesComponent },
  { path: 'colors', component: ColorsComponent },
  { path: 'product/:id', component: ProductDetailComponent },
  { path: 'profile', component: ProfileComponent },
  { path: '**', redirectTo: 'home' }
];
