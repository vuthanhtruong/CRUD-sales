import { HttpInterceptorFn } from '@angular/common/http';

const PUBLIC_AUTH_ENDPOINTS = [
  '/api/accounts/login',
  '/api/accounts/register',
  '/api/accounts/forgot-password',
  '/api/accounts/reset-password',
];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  const isPublicAuthEndpoint = PUBLIC_AUTH_ENDPOINTS.some((endpoint) => req.url.endsWith(endpoint));

  if (!token || isPublicAuthEndpoint || req.headers.has('Authorization')) {
    return next(req);
  }

  return next(req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  }));
};
