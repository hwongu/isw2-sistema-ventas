import { CurrencyPipe } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

import { Producto } from '../../models/producto.model';

@Component({
  selector: 'app-producto-detail-dialog',
  standalone: true,
  imports: [CurrencyPipe, MatButtonModule, MatDialogModule],
  template: `
    <h2 mat-dialog-title>Detalle del producto</h2>
    <mat-dialog-content>
      <dl class="detail-list">
        <div><dt>ID</dt><dd>{{ data.id }}</dd></div>
        <div><dt>Código</dt><dd>{{ data.codigo }}</dd></div>
        <div><dt>Nombre</dt><dd>{{ data.nombre }}</dd></div>
        <div><dt>Precio</dt><dd>{{ data.precio | currency: 'PEN' : 'symbol-narrow' }}</dd></div>
        <div><dt>Stock</dt><dd>{{ data.stock }}</dd></div>
      </dl>
    </mat-dialog-content>
    <mat-dialog-actions align="end"><button mat-button mat-dialog-close>Cerrar</button></mat-dialog-actions>
  `,
})
export class ProductoDetailDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) readonly data: Producto) {}
}
