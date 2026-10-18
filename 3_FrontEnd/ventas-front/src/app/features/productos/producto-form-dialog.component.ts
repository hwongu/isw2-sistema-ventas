import { Component, Inject, inject, signal } from '@angular/core';
import { AbstractControl, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder } from '@angular/forms';
import { finalize } from 'rxjs';

import { ProductoService } from '../../core/services/producto.service';
import { obtenerMensajeError } from '../../core/utils/http-error.util';
import { Producto, ProductoRequest } from '../../models/producto.model';

interface ProductoFormData { producto?: Producto; }

function textoRequerido(control: AbstractControl): ValidationErrors | null {
  return typeof control.value === 'string' && control.value.trim() ? null : { required: true };
}

@Component({
  selector: 'app-producto-form-dialog',
  standalone: true,
  imports: [MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule, ReactiveFormsModule],
  template: `
    <h2 mat-dialog-title>{{ esEdicion ? 'Editar producto' : 'Nuevo producto' }}</h2>
    <mat-dialog-content>
      <form class="dialog-form form-grid" [formGroup]="form" (ngSubmit)="guardar()" id="producto-form">
        <mat-form-field appearance="outline">
          <mat-label>Código</mat-label><input matInput formControlName="codigo" maxlength="50">
          @if (form.controls.codigo.touched && form.controls.codigo.invalid) { <mat-error>Ingrese un código válido.</mat-error> }
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Nombre</mat-label><input matInput formControlName="nombre" maxlength="120">
          @if (form.controls.nombre.touched && form.controls.nombre.invalid) { <mat-error>Ingrese un nombre válido.</mat-error> }
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Precio</mat-label><input matInput type="number" min="0" step="0.01" formControlName="precio">
          @if (form.controls.precio.touched && form.controls.precio.invalid) { <mat-error>El precio debe ser mayor o igual a 0.</mat-error> }
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Stock</mat-label><input matInput type="number" min="0" step="1" formControlName="stock">
          @if (form.controls.stock.touched && form.controls.stock.invalid) { <mat-error>El stock debe ser un entero mayor o igual a 0.</mat-error> }
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false" [disabled]="guardando()">Cancelar</button>
      <button mat-flat-button class="primary-button" form="producto-form" [disabled]="guardando()">
        @if (guardando()) { <mat-spinner diameter="20"></mat-spinner> } @else { Guardar }
      </button>
    </mat-dialog-actions>
  `,
})
export class ProductoFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProductoService);
  private readonly dialogRef = inject(MatDialogRef<ProductoFormDialogComponent>);
  private readonly snackBar = inject(MatSnackBar);
  readonly guardando = signal(false);
  readonly esEdicion: boolean;

  readonly form = this.fb.nonNullable.group({
    codigo: ['', [Validators.required, textoRequerido]],
    nombre: ['', [Validators.required, textoRequerido]],
    precio: [0, [Validators.required, Validators.min(0)]],
    stock: [0, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]],
  });

  constructor(@Inject(MAT_DIALOG_DATA) readonly data: ProductoFormData) {
    this.esEdicion = Boolean(data.producto);
    if (data.producto) {
      this.form.setValue({
        codigo: data.producto.codigo,
        nombre: data.producto.nombre,
        precio: data.producto.precio,
        stock: data.producto.stock,
      });
    }
  }

  guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    const request: ProductoRequest = {
      codigo: value.codigo.trim(), nombre: value.nombre.trim(), precio: value.precio, stock: value.stock,
    };
    this.guardando.set(true);
    const operacion = this.data.producto
      ? this.service.actualizar(this.data.producto.id, request)
      : this.service.crear(request);
    operacion.pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: (producto) => this.dialogRef.close({ accion: this.esEdicion ? 'actualizado' : 'creado', producto }),
      error: (error) => {
        console.error('Error al guardar producto', error);
        this.snackBar.open(obtenerMensajeError(error, 'No se pudo guardar el producto'), 'Cerrar', { duration: 5000 });
      },
    });
  }
}
