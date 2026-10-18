import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';

import { ProductoService } from '../../core/services/producto.service';
import { VentaService } from '../../core/services/venta.service';
import { obtenerMensajeError } from '../../core/utils/http-error.util';
import { Producto } from '../../models/producto.model';
import { VentaRequest } from '../../models/venta.model';

interface DetalleVisual { producto: Producto; cantidad: number; subtotal: number; }

@Component({
  selector: 'app-venta-form-dialog',
  standalone: true,
  imports: [CurrencyPipe, MatButtonModule, MatDialogModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule, MatSelectModule, ReactiveFormsModule],
  template: `
    <h2 mat-dialog-title>Nueva venta</h2>
    <mat-dialog-content>
      @if (cargandoProductos()) {
        <div class="dialog-loading"><mat-spinner diameter="36"></mat-spinner><span>Cargando productos...</span></div>
      } @else {
        <form class="sale-selector" [formGroup]="form" (ngSubmit)="agregar()">
          <mat-form-field appearance="outline">
            <mat-label>Producto</mat-label>
            <mat-select formControlName="productoId" (selectionChange)="seleccionarProducto($event.value)">
              @for (producto of productos(); track producto.id) {
                <mat-option [value]="producto.id">{{ producto.codigo }} - {{ producto.nombre }} - {{ producto.precio | currency: 'PEN' : 'symbol-narrow' }}</mat-option>
              }
            </mat-select>
            @if (form.controls.productoId.touched && form.controls.productoId.invalid) { <mat-error>Seleccione un producto.</mat-error> }
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Cantidad</mat-label><input matInput type="number" min="1" step="1" formControlName="cantidad">
            @if (form.controls.cantidad.touched && form.controls.cantidad.invalid) { <mat-error>Ingrese una cantidad entera mayor que 0.</mat-error> }
          </mat-form-field>
          <button mat-stroked-button class="secondary-button" type="submit"><mat-icon>add</mat-icon> Agregar</button>
        </form>
        @if (productoSeleccionado(); as producto) {
          <div class="product-summary"><span>Precio: <strong>{{ producto.precio | currency: 'PEN' : 'symbol-narrow' }}</strong></span><span>Stock disponible: <strong>{{ producto.stock }}</strong></span></div>
        }
        @if (detalles().length) {
          <div class="table-container compact-table">
            <table>
              <thead><tr><th>Producto</th><th>Cantidad</th><th>Precio</th><th>Subtotal</th><th>Acciones</th></tr></thead>
              <tbody>
                @for (detalle of detalles(); track detalle.producto.id) {
                  <tr><td>{{ detalle.producto.nombre }}</td><td>{{ detalle.cantidad }}</td><td>{{ detalle.producto.precio | currency: 'PEN' : 'symbol-narrow' }}</td><td>{{ detalle.subtotal | currency: 'PEN' : 'symbol-narrow' }}</td><td><button mat-button class="detail-button" type="button" (click)="quitar(detalle.producto.id)">Eliminar</button></td></tr>
                }
              </tbody>
            </table>
          </div>
          <p class="sale-total">Total aproximado: <strong>{{ totalVisual() | currency: 'PEN' : 'symbol-narrow' }}</strong></p>
        } @else {
          <p class="empty-dialog">Agregue al menos un producto a la venta.</p>
        }
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="guardando()">Cancelar</button>
      <button mat-flat-button class="primary-button" (click)="registrar()" [disabled]="guardando() || !detalles().length">
        @if (guardando()) { <mat-spinner diameter="20"></mat-spinner> } @else { Registrar venta }
      </button>
    </mat-dialog-actions>
  `,
})
export class VentaFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly productoService = inject(ProductoService);
  private readonly ventaService = inject(VentaService);
  private readonly dialogRef = inject(MatDialogRef<VentaFormDialogComponent>);
  private readonly snackBar = inject(MatSnackBar);
  readonly productos = signal<Producto[]>([]);
  readonly detalles = signal<DetalleVisual[]>([]);
  readonly productoSeleccionado = signal<Producto | null>(null);
  readonly cargandoProductos = signal(true);
  readonly guardando = signal(false);
  readonly totalVisual = computed(() => this.detalles().reduce((total, item) => total + item.subtotal, 0));
  readonly form = this.fb.nonNullable.group({
    productoId: [0, [Validators.required, Validators.min(1)]],
    cantidad: [1, [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)]],
  });

  ngOnInit(): void {
    this.productoService.listar().pipe(finalize(() => this.cargandoProductos.set(false))).subscribe({
      next: (productos) => this.productos.set(productos),
      error: (error) => {
        console.error('Error al cargar productos para la venta', error);
        this.snackBar.open(obtenerMensajeError(error, 'No se pudieron cargar los productos'), 'Cerrar', { duration: 5000 });
      },
    });
  }

  seleccionarProducto(id: number): void {
    this.productoSeleccionado.set(this.productos().find((producto) => producto.id === id) ?? null);
  }

  agregar(): void {
    if (this.form.invalid || !this.productoSeleccionado()) { this.form.markAllAsTouched(); return; }
    const producto = this.productoSeleccionado()!;
    const cantidad = this.form.controls.cantidad.value;
    const existente = this.detalles().find((item) => item.producto.id === producto.id);
    const cantidadTotal = cantidad + (existente?.cantidad ?? 0);
    if (cantidadTotal > producto.stock) {
      this.snackBar.open(`Stock insuficiente. Disponible: ${producto.stock}`, 'Cerrar', { duration: 4500 });
      return;
    }
    const nuevo: DetalleVisual = { producto, cantidad: cantidadTotal, subtotal: producto.precio * cantidadTotal };
    this.detalles.update((items) => [...items.filter((item) => item.producto.id !== producto.id), nuevo]);
    this.form.controls.cantidad.setValue(1);
  }

  quitar(productoId: number): void { this.detalles.update((items) => items.filter((item) => item.producto.id !== productoId)); }

  registrar(): void {
    if (!this.detalles().length || this.guardando()) return;
    const request: VentaRequest = {
      detalles: this.detalles().map((item) => ({ productoId: item.producto.id, cantidad: item.cantidad })),
    };
    this.guardando.set(true);
    this.ventaService.registrar(request).pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: (venta) => {
        this.productoService.listar().subscribe({
          next: () => this.dialogRef.close(venta),
          error: () => this.dialogRef.close(venta),
        });
      },
      error: (error) => {
        console.error('Error al registrar venta', error);
        this.snackBar.open(obtenerMensajeError(error, 'No se pudo registrar la venta'), 'Cerrar', { duration: 6000 });
      },
    });
  }
}
