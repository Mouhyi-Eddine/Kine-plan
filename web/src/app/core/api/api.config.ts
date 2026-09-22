import { inject, InjectionToken, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

function resolveApiBaseUrl(): string {
  if (!isPlatformBrowser(inject(PLATFORM_ID))) {
    return 'http://localhost:8080/api/v1';
  }

  const { hostname, origin, protocol } = window.location;
  if (hostname.endsWith('.app.github.dev')) {
    const apiHost = hostname.replace(/-4200\.app\.github\.dev$/, '-8080.app.github.dev');
    return `${protocol}//${apiHost}/api/v1`;
  }
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return 'http://localhost:8080/api/v1';
  }
  return `${origin}/api/v1`;
}

export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: resolveApiBaseUrl,
});