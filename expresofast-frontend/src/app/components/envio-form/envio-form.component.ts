import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EnvioService } from '../../services/envio.service';
import { CrearEnvioPayload } from '../../models/envio.model';

@Component({
  selector: 'app-envio-form',
  imports: [CommonModule, FormsModule],
  templateUrl: './envio-form.component.html',
  styleUrl: './envio-form.component.css',
})
export class EnvioFormComponent {
  private envioService = inject(EnvioService);

  form: CrearEnvioPayload = { destinatario: '', direccionDestino: '', montoFlete: 0 };
  mensaje = '';

  registrar(): void {
    if (!this.form.destinatario || !this.form.direccionDestino) {
      this.mensaje = 'Complete los campos obligatorios.';
      return;
    }

    this.envioService.crearEnvio(this.form).subscribe({
      next: () => {
        this.mensaje = 'Envío registrado con éxito.';
        this.form = { destinatario: '', direccionDestino: '', montoFlete: 0 };
      },
      error: (err) => {
        console.error('Error al registrar el envío', err);
        this.mensaje = 'Error al registrar el envío.';
      },
    });
  }
}
