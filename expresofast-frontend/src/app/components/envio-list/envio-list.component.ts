import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

// TODO 1: importa EnvioService y los tipos Envio / EstadoEnvio que ya existen en
// ../../services/envio.service.ts y ../../models/envio.model.ts

@Component({
  selector: 'app-envio-list',
  imports: [CommonModule],
  templateUrl: './envio-list.component.html',
  styleUrl: './envio-list.component.css',
})
export class EnvioListComponent implements OnInit {
  // TODO 2: inyecta el servicio con inject(EnvioService) (mismo patrón que ya usa
  // EnvioService con HttpClient). No uses constructor(...) clásico, este proyecto
  // usa la función inject() que es el estilo recomendado en Angular Standalone.
  // private envioService = inject(EnvioService);

  // TODO 3: crea la propiedad donde vas a guardar la lista que llega del backend.
  // envios: Envio[] = [];

  // TODO 4: aquí van los 4 valores posibles de estado, para pintar el <select> de la tabla.
  // estados: EstadoEnvio[] = ['PENDIENTE', 'EN_TRANSITO', 'ENTREGADO', 'CANCELADO'];

  ngOnInit(): void {
    // TODO 5: llama a this.envioService.obtenerEnvios().subscribe({...})
    // OJO: HttpClient devuelve un Observable, no una Promise -> no hay "await" aquí.
    // Te suscribís pasando un objeto { next, error }:
    //   this.envioService.obtenerEnvios().subscribe({
    //     next: (data) => (this.envios = data),
    //     error: (err) => console.error('Error cargando envios', err),
    //   });
  }

  // TODO 6: método que se dispara cuando cambian el <select> de estado en una fila.
  // cambiarEstado(envio: Envio, nuevoEstado: EstadoEnvio): void {
  //   this.envioService.actualizarEstado(envio.id, nuevoEstado).subscribe({
  //     next: (actualizado) => (envio.estado = actualizado.estado),
  //     error: (err) => console.error('Error actualizando estado', err),
  //   });
  // }
}
