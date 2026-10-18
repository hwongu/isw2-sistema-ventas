import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';

import { Venta } from '../../models/venta.model';

export interface VentaDetailDialogData {
  venta: Venta;
  nombresProducto: Record<number, string>;
}

@Component({
  selector: 'app-venta-detail-dialog',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, MatButtonModule, MatDialogModule, MatTableModule],
  template: `
    <h2 mat-dialog-title>Venta #{{ data.venta.id }}</h2>
    <mat-dialog-content>
      <dl class="detail-list sale-header">
        <div><dt>Fecha</dt><dd>{{ data.venta.fecha | date: 'dd/MM/yyyy HH:mm' }}</dd></div>
        <div><dt>Estado</dt><dd>{{ data.venta.estado }}</dd></div>
        <div><dt>Total</dt><dd>{{ data.venta.total | currency: 'PEN' : 'symbol-narrow' }}</dd></div>
      </dl>
      @if (data.venta.detalles.length) {
        <div class="table-container dialog-table">
          <table mat-table [dataSource]="data.venta.detalles">
            <ng-container matColumnDef="producto"><th mat-header-cell *matHeaderCellDef>Producto</th><td mat-cell *matCellDef="let item">{{ nombreProducto(item.productoId) }}</td></ng-container>
            <ng-container matColumnDef="cantidad"><th mat-header-cell *matHeaderCellDef>Cantidad</th><td mat-cell *matCellDef="let item">{{ item.cantidad }}</td></ng-container>
            <ng-container matColumnDef="precio"><th mat-header-cell *matHeaderCellDef>Precio unitario</th><td mat-cell *matCellDef="let item">{{ item.precioUnitario | currency: 'PEN' : 'symbol-narrow' }}</td></ng-container>
            <ng-container matColumnDef="subtotal"><th mat-header-cell *matHeaderCellDef>Subtotal</th><td mat-cell *matCellDef="let item">{{ item.subtotal | currency: 'PEN' : 'symbol-narrow' }}</td></ng-container>
            <tr mat-header-row *matHeaderRowDef="columnas"></tr><tr mat-row *matRowDef="let row; columns: columnas"></tr>
          </table>
        </div>
      } @else { <p class="empty-dialog">La venta no contiene detalles.</p> }
    </mat-dialog-content>
    <mat-dialog-actions align="end"><button mat-button mat-dialog-close>Cerrar</button></mat-dialog-actions>
  `,
})
export class VentaDetailDialogComponent {
  readonly columnas = ['producto', 'cantidad', 'precio', 'subtotal'];
  constructor(@Inject(MAT_DIALOG_DATA) readonly data: VentaDetailDialogData) {}
  nombreProducto(id: number): string { return this.data.nombresProducto[id] ?? `Producto #${id}`; }
}
