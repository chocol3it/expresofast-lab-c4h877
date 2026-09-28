import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// TODO 1: importa EnvioService y Envio

@Component({
  selector: 'app-envio-tracking',
  imports: [CommonModule, FormsModule],
  templateUrl: './envio-tracking.component.html',
  styleUrl: './envio-tracking.component.css',
})
export class EnvioTrackingComponent {
  // TODO 2: inyecta EnvioService.
  // private envioService = inject(EnvioService);

  // TODO 3: variable ligada con [(ngModel)] al campo de búsqueda.
  // codigo = '';

  // TODO 4: dónde guardar el envío encontrado (o null si no hay búsqueda / no existe).
  // envio: Envio | null = null;
  // errorMensaje = '';

  // TODO 5: método que dispara el botón "Buscar".
  // buscar(): void {
  //   this.errorMensaje = '';
  //   this.envioService.obtenerPorRastreo(this.codigo).subscribe({
  //     next: (data) => (this.envio = data),
  //     error: () => {
  //       this.envio = null;
  //       this.errorMensaje = 'No se encontró un envío con ese código de rastreo.';
  //     },
  //   });
  // }

  // TODO 6 (reto): función auxiliar que traduzca el estado a un porcentaje de progreso
  // para la barra visual, ej: PENDIENTE=25, EN_TRANSITO=60, ENTREGADO=100, CANCELADO=0.
}
