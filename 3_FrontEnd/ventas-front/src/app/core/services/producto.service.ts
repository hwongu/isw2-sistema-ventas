import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Producto, ProductoRequest } from '../../models/producto.model';
import { AppConfigService } from '../config/app-config.service';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(AppConfigService);

  listar(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.config.ventasApiUrl}/productos`);
  }

  obtenerPorId(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.config.ventasApiUrl}/productos/${id}`);
  }

  crear(request: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(`${this.config.ventasApiUrl}/productos`, request);
  }

  actualizar(id: number, request: ProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(`${this.config.ventasApiUrl}/productos/${id}`, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.config.ventasApiUrl}/productos/${id}`);
  }
}
