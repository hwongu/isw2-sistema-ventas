import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';

import { Pago } from '../../models/pago.model';

@Component({
  selector: 'app-pago-detail-dialog',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, MatButtonModule, MatDialogModule],
  template: `
    <h2 mat-dialog-title>Detalle del pago</h2>
    <mat-dialog-content><dl class="detail-list">
      <div><dt>ID</dt><dd>{{ data.id }}</dd></div><div><dt>Venta</dt><dd>#{{ data.ventaId }}</dd></div>
      <div><dt>Fecha</dt><dd>{{ data.fecha | date: 'dd/MM/yyyy HH:mm' }}</dd></div>
      <div><dt>Monto</dt><dd>{{ data.monto | currency: 'PEN' : 'symbol-narrow' }}</dd></div>
      <div><dt>Método</dt><dd>{{ data.metodoPago }}</dd></div><div><dt>Estado</dt><dd>{{ data.estado }}</dd></div>
      <div><dt>Código de operación</dt><dd>{{ data.codigoOperacion }}</dd></div>
    </dl></mat-dialog-content>
    <mat-dialog-actions align="end"><button mat-button mat-dialog-close>Cerrar</button></mat-dialog-actions>
  `,
})
export class PagoDetailDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) readonly data: Pago) {}
}
