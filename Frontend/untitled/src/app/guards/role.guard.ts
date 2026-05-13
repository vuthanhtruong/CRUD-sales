import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { isAdminRole, isUserRole, readAuthState } from '../auth/jwt-auth.util';

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authState = readAuthState();

  if (authState && isAdminRole(authState.role)) return true;

  router.navigate(['/home']);
  return false;
};

export const profileGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authState = readAuthState();

  if (authState && (isAdminRole(authState.role) || isUserRole(authState.role))) return true;

  router.navigate(['/home']);
  return false;
};
