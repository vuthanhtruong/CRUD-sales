export interface JwtPayload {
  sub?: string;
  username?: string;
  role?: string;
  exp?: number;
  [key: string]: unknown;
}

export interface AuthState {
  token: string;
  username: string;
  role: string;
}

export function decodeJwtPayload(token: string): JwtPayload | null {
  const parts = token.split('.');
  if (parts.length < 2) return null;

  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const paddedBase64 = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
    const json = decodeURIComponent(
      Array.from(atob(paddedBase64))
        .map((char) => `%${char.charCodeAt(0).toString(16).padStart(2, '0')}`)
        .join(''),
    );

    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

export function clearAuthStorage(): void {
  localStorage.removeItem('token');

  // Legacy cleanup: old builds stored these separately.
  // Do not read these keys for auth/role checks anymore.
  localStorage.removeItem('username');
  localStorage.removeItem('role');
}

export function clearLegacyAuthKeys(): void {
  localStorage.removeItem('username');
  localStorage.removeItem('role');
}

export function readAuthState(): AuthState | null {
  const token = localStorage.getItem('token');

  if (!token) {
    clearLegacyAuthKeys();
    return null;
  }

  const payload = decodeJwtPayload(token);
  const username = payload?.sub || payload?.username;
  const role = payload?.role;
  const isExpired = typeof payload?.exp === 'number' && payload.exp * 1000 <= Date.now();

  if (!payload || !username || !role || isExpired) {
    clearAuthStorage();
    return null;
  }

  clearLegacyAuthKeys();

  return { token, username, role };
}

export function storeAuthToken(token: string): AuthState | null {
  localStorage.setItem('token', token);
  return readAuthState();
}

export function isAdminRole(role: string | null | undefined): boolean {
  return role === 'ADMIN' || role === 'ROLE_ADMIN';
}

export function isUserRole(role: string | null | undefined): boolean {
  return role === 'USER' || role === 'ROLE_USER';
}
