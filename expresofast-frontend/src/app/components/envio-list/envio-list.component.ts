import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EnvioService } from '../../services/envio.service';
import { Envio, EstadoEnvio } from '../../models/envio.model';

@Component({
  selector: 'app-envio-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './envio-list.component.html',
  styleUrl: './envio-list.component.css',
})
export class EnvioListComponent implements OnInit {
  private envioService = inject(EnvioService);

  envios = signal<Envio[]>([]);
  estados: EstadoEnvio[] = ['PENDIENTE', 'EN_TRANSITO', 'ENTREGADO', 'CANCELADO'];

  ngOnInit(): void {
    this.envioService.obtenerEnvios().subscribe({
      next: (data) => this.envios.set(data),
      error: (err) => console.error('Error cargando envios', err),
    });
  }

  cambiarEstado(envio: Envio, nuevoEstado: EstadoEnvio): void {
    this.envioService.actualizarEstado(envio.id, nuevoEstado).subscribe({
      next: (actualizado) => {
        this.envios.update((lista) =>
          lista.map((e) => (e.id === envio.id ? { ...e, estado: actualizado.estado } : e)),
        );
      },
      error: (err) => console.error('Error actualizando estado', err),
    });
  }
}
