import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EnvioService } from '../../services/envio.service';
import { Envio, EstadoEnvio } from '../../models/envio.model';

const PROGRESO_POR_ESTADO: Record<EstadoEnvio, number> = {
  PENDIENTE: 25,
  EN_TRANSITO: 60,
  ENTREGADO: 100,
  CANCELADO: 0,
};

@Component({
  selector: 'app-envio-tracking',
  imports: [CommonModule, FormsModule],
  templateUrl: './envio-tracking.component.html',
  styleUrl: './envio-tracking.component.css',
})
export class EnvioTrackingComponent {
  private envioService = inject(EnvioService);

  codigo = '';
  envio: Envio | null = null;
  errorMensaje = '';

  buscar(): void {
    this.errorMensaje = '';
    this.envio = null;

    this.envioService.obtenerPorRastreo(this.codigo).subscribe({
      next: (data) => (this.envio = data),
      error: () => {
        this.errorMensaje = 'No se encontró un envío con ese código de rastreo.';
      },
    });
  }

  progreso(): number {
    return this.envio ? PROGRESO_POR_ESTADO[this.envio.estado] : 0;
  }
}
