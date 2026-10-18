import { Component, OnInit, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';

import { PagoService } from '../../core/services/pago.service';
import { ProductoService } from '../../core/services/producto.service';
import { VentaService } from '../../core/services/venta.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [MatCardModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly productoService = inject(ProductoService);
  private readonly ventaService = inject(VentaService);
  private readonly pagoService = inject(PagoService);
  private readonly snackBar = inject(MatSnackBar);

  readonly cargando = signal(true);
  readonly resumen = signal({ productos: 0, ventas: 0, pagos: 0 });

  ngOnInit(): void {
    forkJoin({
      productos: this.productoService.listar(),
      ventas: this.ventaService.listar(),
      pagos: this.pagoService.listar(),
    }).subscribe({
      next: ({ productos, ventas, pagos }) => {
        this.resumen.set({ productos: productos.length, ventas: ventas.length, pagos: pagos.length });
        this.cargando.set(false);
      },
      error: (error) => {
        console.error('No se pudo cargar el resumen del dashboard', error);
        this.cargando.set(false);
        this.snackBar.open('No se pudo obtener el resumen del sistema', 'Cerrar', { duration: 5000 });
      },
    });
  }
}
