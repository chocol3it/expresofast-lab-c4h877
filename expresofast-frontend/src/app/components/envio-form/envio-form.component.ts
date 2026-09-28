import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// TODO 1: importa EnvioService y CrearEnvioPayload

@Component({
  selector: 'app-envio-form',
  imports: [CommonModule, FormsModule],
  templateUrl: './envio-form.component.html',
  styleUrl: './envio-form.component.css',
})
export class EnvioFormComponent {
  // TODO 2: inyecta EnvioService.
  // private envioService = inject(EnvioService);

  // TODO 3: crea un objeto que represente el formulario y al que le vas a hacer
  // [(ngModel)] desde el HTML. Debe tener la misma forma que CrearEnvioPayload.
  // form: CrearEnvioPayload = { destinatario: '', direccionDestino: '', montoFlete: 0 };

  // TODO 4: bandera simple para mostrar un mensaje de éxito o error después de enviar.
  // mensaje = '';

  // TODO 5: método que dispara el (ngSubmit) del <form>.
  // registrar(): void {
  //   // Validación mínima de campos obligatorios (destinatario y dirección no vacíos).
  //   if (!this.form.destinatario || !this.form.direccionDestino) {
  //     this.mensaje = 'Complete los campos obligatorios.';
  //     return;
  //   }
  //   this.envioService.crearEnvio(this.form).subscribe({
  //     next: () => {
  //       this.mensaje = 'Envío registrado con éxito.';
  //       this.form = { destinatario: '', direccionDestino: '', montoFlete: 0 };
  //     },
  //     error: (err) => (this.mensaje = 'Error al registrar el envío.'),
  //   });
  // }
}
