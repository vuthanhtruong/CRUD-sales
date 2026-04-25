import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

function getRole(): string {
  return localStorage.getItem('role') || '';
}

function isAdminRole(role: string): boolean {
  return role === 'ADMIN' || role === 'ROLE_ADMIN';
}

function isUserRole(role: string): boolean {
  return role === 'USER' || role === 'ROLE_USER';
}

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const role = getRole();

  if (isAdminRole(role)) return true;

  router.navigate(['/home']);
  return false;
};

export const profileGuard: CanActivateFn = () => {
  const router = inject(Router);
  const role = getRole();
  const token = localStorage.getItem('token');

  if (token && (isAdminRole(role) || isUserRole(role))) return true;

  router.navigate(['/home']);
  return false;
};
