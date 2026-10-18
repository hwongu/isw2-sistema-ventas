import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { filter, finalize } from 'rxjs';

import { PagoService } from '../../core/services/pago.service';
import { Pago } from '../../models/pago.model';
import { PagoDetailDialogComponent } from './pago-detail-dialog.component';
import { PagoFormDialogComponent } from './pago-form-dialog.component';
import { obtenerMensajeError } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-pagos', standalone: true,
  imports: [CurrencyPipe, DatePipe, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule, MatTableModule, ReactiveFormsModule],
  templateUrl: './pagos.component.html', styleUrl: './pagos.component.scss',
})
export class PagosComponent implements OnInit {
  private readonly service = inject(PagoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  readonly pagos = signal<Pago[]>([]);
  readonly cargando = signal(true);
  readonly error = signal(false);
  readonly buscando = signal(false);
  readonly ventaId = new FormControl<number | null>(null, [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)]);
  readonly columnas = ['id', 'venta', 'fecha', 'monto', 'metodo', 'estado', 'codigo', 'acciones'];

  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando.set(true); this.error.set(false);
    this.service.listar().subscribe({
      next: (pagos) => { this.pagos.set(pagos); this.cargando.set(false); },
      error: (error) => {
        console.error('Error al obtener pagos', error);
        this.error.set(true); this.cargando.set(false);
        this.snackBar.open('No se pudo obtener la lista de pagos', 'Cerrar', { duration: 5000 });
      },
    });
  }

  nuevo(): void {
    this.dialog.open(PagoFormDialogComponent, { width: '600px', maxWidth: '95vw' })
      .afterClosed().pipe(filter(Boolean)).subscribe((resultado) => {
        const mensaje = resultado.tipo === 'rechazado'
          ? resultado.mensaje
          : `${resultado.respuesta.mensaje} · Estado: ${resultado.respuesta.estado} · Código: ${resultado.respuesta.codigoOperacion}`;
        this.snackBar.open(mensaje, 'Cerrar', { duration: 6500 });
        this.cargar();
      });
  }

  buscar(): void {
    if (this.ventaId.invalid || this.ventaId.value === null) { this.ventaId.markAsTouched(); return; }
    this.buscando.set(true); this.error.set(false);
    this.service.buscarPorVenta(this.ventaId.value).pipe(finalize(() => this.buscando.set(false))).subscribe({
      next: (pagos) => this.pagos.set(pagos),
      error: (error) => {
        console.error('Error al buscar pagos por venta', error);
        this.snackBar.open(obtenerMensajeError(error, 'No se pudieron buscar los pagos'), 'Cerrar', { duration: 5000 });
      },
    });
  }

  limpiar(): void { this.ventaId.reset(); this.cargar(); }
  verDetalle(id: number): void {
    this.service.obtenerPorId(id).subscribe({
      next: (pago) => this.dialog.open(PagoDetailDialogComponent, { data: pago, width: '480px' }),
      error: (error) => {
        console.error('Error al obtener el pago', error);
        this.snackBar.open(obtenerMensajeError(error, 'No se pudo obtener el detalle del pago'), 'Cerrar', { duration: 5000 });
      },
    });
  }
}
