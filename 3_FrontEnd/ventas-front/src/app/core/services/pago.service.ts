import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Pago, PagoRequest, PagoResponse } from '../../models/pago.model';
import { AppConfigService } from '../config/app-config.service';

@Injectable({ providedIn: 'root' })
export class PagoService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(AppConfigService);

  listar(): Observable<Pago[]> {
    return this.http.get<Pago[]>(`${this.config.pagosApiUrl}/pagos`);
  }

  obtenerPorId(id: number): Observable<Pago> {
    return this.http.get<Pago>(`${this.config.pagosApiUrl}/pagos/${id}`);
  }

  buscarPorVenta(ventaId: number): Observable<Pago[]> {
    return this.http.get<Pago[]>(`${this.config.pagosApiUrl}/pagos`, { params: { ventaId } });
  }

  crear(request: PagoRequest): Observable<PagoResponse> {
    return this.http.post<PagoResponse>(`${this.config.pagosApiUrl}/pagos`, request);
  }
}
