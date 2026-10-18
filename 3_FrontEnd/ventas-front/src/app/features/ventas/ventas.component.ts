import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { filter, forkJoin } from 'rxjs';

import { ProductoService } from '../../core/services/producto.service';
import { VentaService } from '../../core/services/venta.service';
import { Venta } from '../../models/venta.model';
import { VentaDetailDialogComponent } from './venta-detail-dialog.component';
import { VentaFormDialogComponent } from './venta-form-dialog.component';
import { obtenerMensajeError } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-ventas',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, MatButtonModule, MatIconModule, MatProgressSpinnerModule, MatTableModule],
  templateUrl: './ventas.component.html',
  styleUrl: './ventas.component.scss',
})
export class VentasComponent implements OnInit {
  private readonly service = inject(VentaService);
  private readonly productoService = inject(ProductoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  readonly ventas = signal<Venta[]>([]);
  readonly cargando = signal(true);
  readonly error = signal(false);
  readonly columnas = ['id', 'fecha', 'total', 'estado', 'acciones'];
  readonly nombresProducto = signal<Record<number, string>>({});

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true); this.error.set(false);
    forkJoin({ ventas: this.service.listar(), productos: this.productoService.listar() }).subscribe({
      next: ({ ventas, productos }) => {
        this.ventas.set(ventas);
        this.nombresProducto.set(Object.fromEntries(productos.map((producto) => [producto.id, producto.nombre])));
        this.cargando.set(false);
      },
      error: (error) => {
        console.error('Error al obtener ventas', error);
        this.error.set(true); this.cargando.set(false);
        this.snackBar.open('No se pudo obtener la lista de ventas', 'Cerrar', { duration: 5000 });
      },
    });
  }

  nueva(): void {
    this.dialog.open(VentaFormDialogComponent, { width: '900px', maxWidth: '96vw' })
      .afterClosed().pipe(filter(Boolean)).subscribe((venta: Venta) => {
        this.snackBar.open(`Venta #${venta.id} registrada correctamente · Estado: ${venta.estado}`, 'Cerrar', { duration: 5500 });
        this.cargar();
      });
  }

  verDetalle(id: number): void {
    this.service.obtenerPorId(id).subscribe({
      next: (venta) => this.dialog.open(VentaDetailDialogComponent, {
        data: { venta, nombresProducto: this.nombresProducto() }, width: '780px', maxWidth: '95vw',
      }),
      error: (error) => {
        console.error('Error al obtener la venta', error);
        this.snackBar.open(obtenerMensajeError(error, 'No se pudo obtener el detalle de la venta'), 'Cerrar', { duration: 5000 });
      },
    });
  }
}
