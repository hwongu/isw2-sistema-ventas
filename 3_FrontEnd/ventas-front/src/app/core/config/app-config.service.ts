import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { AppConfig } from './app-config.model';

@Injectable({ providedIn: 'root' })
export class AppConfigService {
  private readonly http = inject(HttpClient);
  private config?: AppConfig;

  async load(): Promise<void> {
    this.config = await firstValueFrom(
      this.http.get<AppConfig>('/config/app-config.json', {
        headers: { 'Cache-Control': 'no-cache' },
      }),
    );
  }

  get ventasApiUrl(): string {
    return this.requireConfig().ventasApiUrl.replace(/\/$/, '');
  }

  get pagosApiUrl(): string {
    return this.requireConfig().pagosApiUrl.replace(/\/$/, '');
  }

  private requireConfig(): AppConfig {
    if (!this.config) {
      throw new Error('La configuración de la aplicación todavía no está disponible.');
    }
    return this.config;
  }
}
