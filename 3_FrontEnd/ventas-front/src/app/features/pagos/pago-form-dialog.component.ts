import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';

import { PagoService } from '../../core/services/pago.service';
import { obtenerMensajeError } from '../../core/utils/http-error.util';
import { PagoRequest } from '../../models/pago.model';

@Component({
  selector: 'app-pago-form-dialog',
  standalone: true,
  imports: [MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule, MatSelectModule, ReactiveFormsModule],
  template: `
    <h2 mat-dialog-title>Nuevo pago</h2>
    <mat-dialog-content>
      <form class="dialog-form form-grid" [formGroup]="form" (ngSubmit)="guardar()" id="pago-form">
        <mat-form-field appearance="outline">
          <mat-label>Venta ID</mat-label><input matInput type="number" min="1" step="1" formControlName="ventaId">
          @if (form.controls.ventaId.touched && form.controls.ventaId.invalid) { <mat-error>Ingrese un ID de venta mayor que 0.</mat-error> }
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Monto</mat-label><input matInput type="number" min="0.01" step="0.01" formControlName="monto">
          @if (form.controls.monto.touched && form.controls.monto.invalid) { <mat-error>Ingrese un monto mayor que 0.</mat-error> }
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Método de pago</mat-label>
          <mat-select formControlName="metodoPago" (selectionChange)="cambiarMetodo($event.value)">
            <mat-option value="EFECTIVO">EFECTIVO</mat-option><mat-option value="TARJETA">TARJETA</mat-option>
          </mat-select>
        </mat-form-field>
        @if (form.controls.metodoPago.value === 'TARJETA') {
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Token de tarjeta</mat-label><input matInput formControlName="tokenTarjeta">
            <mat-hint>Para esta simulación académica, el token VALIDA representa una tarjeta aprobada.</mat-hint>
            @if (form.controls.tokenTarjeta.touched && form.controls.tokenTarjeta.invalid) { <mat-error>Ingrese el token de tarjeta.</mat-error> }
          </mat-form-field>
        }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="guardando()">Cancelar</button>
      <button mat-flat-button class="primary-button" form="pago-form" [disabled]="guardando()">
        @if (guardando()) { <mat-spinner diameter="20"></mat-spinner> } @else { Guardar }
      </button>
    </mat-dialog-actions>
  `,
})
export class PagoFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(PagoService);
  private readonly dialogRef = inject(MatDialogRef<PagoFormDialogComponent>);
  private readonly snackBar = inject(MatSnackBar);
  readonly guardando = signal(false);
  readonly form = this.fb.nonNullable.group({
    ventaId: [0, [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)]],
    monto: [0, [Validators.required, Validators.min(0.01)]],
    metodoPago: this.fb.nonNullable.control<'EFECTIVO' | 'TARJETA'>('EFECTIVO', Validators.required),
    tokenTarjeta: [''],
  });

  cambiarMetodo(metodo: 'EFECTIVO' | 'TARJETA'): void {
    const token = this.form.controls.tokenTarjeta;
    if (metodo === 'TARJETA') token.setValidators([Validators.required]);
    else { token.clearValidators(); token.setValue(''); }
    token.updateValueAndValidity();
  }

  guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    const request: PagoRequest = { ventaId: value.ventaId, monto: value.monto, metodoPago: value.metodoPago };
    if (value.metodoPago === 'TARJETA') request.tokenTarjeta = value.tokenTarjeta.trim();
    this.guardando.set(true);
    this.service.crear(request).pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: (respuesta) => this.dialogRef.close({ tipo: 'aprobado', respuesta }),
      error: (error: HttpErrorResponse) => {
        console.error('Error al procesar pago', error);
        const mensaje = obtenerMensajeError(error, 'No se pudo procesar el pago');
        if (error.status === 422) this.dialogRef.close({ tipo: 'rechazado', mensaje, respuesta: error.error });
        else this.snackBar.open(mensaje, 'Cerrar', { duration: 6000 });
      },
    });
  }
}
