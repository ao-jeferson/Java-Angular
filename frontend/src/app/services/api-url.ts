import { InjectionToken } from '@angular/core';
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => {
    const config = window as Window & { MOVIMENTO_API_URL?: string };
    return `${(config.MOVIMENTO_API_URL ?? '').replace(/\/$/, '')}/api/v1`;
  },
});
