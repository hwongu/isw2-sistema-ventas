import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Venta, VentaRequest } from '../../models/venta.model';
import { AppConfigService } from '../config/app-config.service';

@Injectable({ providedIn: 'root' })
export class VentaService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(AppConfigService);

  listar(): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.config.ventasApiUrl}/ventas`);
  }

  obtenerPorId(id: number): Observable<Venta> {
    return this.http.get<Venta>(`${this.config.ventasApiUrl}/ventas/${id}`);
  }

  registrar(request: VentaRequest): Observable<Venta> {
    return this.http.post<Venta>(`${this.config.ventasApiUrl}/ventas`, request);
  }
}
