import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { filter, finalize } from 'rxjs';

import { ProductoService } from '../../core/services/producto.service';
import { Producto } from '../../models/producto.model';
import { ProductoDetailDialogComponent } from './producto-detail-dialog.component';
import { ProductoFormDialogComponent } from './producto-form-dialog.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog.component';
import { obtenerMensajeError } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CurrencyPipe, MatButtonModule, MatIconModule, MatProgressSpinnerModule, MatTableModule],
  templateUrl: './productos.component.html',
  styleUrl: './productos.component.scss',
})
export class ProductosComponent implements OnInit {
  private readonly service = inject(ProductoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly productos = signal<Producto[]>([]);
  readonly cargando = signal(true);
  readonly error = signal(false);
  readonly eliminandoId = signal<number | null>(null);
  readonly columnas = ['codigo', 'nombre', 'precio', 'stock', 'acciones'];

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(false);
    this.service.listar().subscribe({
      next: (productos) => { this.productos.set(productos); this.cargando.set(false); },
      error: (error) => {
        console.error('Error al obtener productos', error);
        this.error.set(true);
        this.cargando.set(false);
        this.snackBar.open('No se pudo obtener la lista de productos', 'Cerrar', { duration: 5000 });
      },
    });
  }

  nuevo(): void {
    this.dialog.open(ProductoFormDialogComponent, { data: {}, width: '600px', maxWidth: '95vw' })
      .afterClosed().pipe(filter(Boolean)).subscribe(() => {
        this.snackBar.open('Producto registrado correctamente', 'Cerrar', { duration: 4000 });
        this.cargar();
      });
  }

  editar(producto: Producto): void {
    this.dialog.open(ProductoFormDialogComponent, { data: { producto }, width: '600px', maxWidth: '95vw' })
      .afterClosed().pipe(filter(Boolean)).subscribe(() => {
        this.snackBar.open('Producto actualizado correctamente', 'Cerrar', { duration: 4000 });
        this.cargar();
      });
  }

  eliminar(producto: Producto): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: { titulo: 'Eliminar producto', mensaje: '¿Desea eliminar el producto seleccionado?', confirmar: 'Eliminar' },
      width: '440px', maxWidth: '95vw',
    }).afterClosed().pipe(filter(Boolean)).subscribe(() => {
      this.eliminandoId.set(producto.id);
      this.service.eliminar(producto.id).pipe(finalize(() => this.eliminandoId.set(null))).subscribe({
        next: () => {
          this.snackBar.open('Producto eliminado correctamente', 'Cerrar', { duration: 4000 });
          this.cargar();
        },
        error: (error) => {
          console.error('Error al eliminar producto', error);
          this.snackBar.open(obtenerMensajeError(error, 'No se pudo eliminar el producto'), 'Cerrar', { duration: 5000 });
        },
      });
    });
  }

  verDetalle(id: number): void {
    this.service.obtenerPorId(id).subscribe({
      next: (producto) => this.dialog.open(ProductoDetailDialogComponent, { data: producto, width: '440px' }),
      error: (error) => {
        console.error('Error al obtener el producto', error);
        this.snackBar.open('No se pudo obtener el detalle del producto', 'Cerrar', { duration: 5000 });
      },
    });
  }
}
