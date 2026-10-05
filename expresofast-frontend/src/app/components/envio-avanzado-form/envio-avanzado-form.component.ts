import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  AsyncValidatorFn,
  ReactiveFormsModule,
  NonNullableFormBuilder,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, debounceTime, map, switchMap } from 'rxjs/operators';
import { EnvioService } from '../../services/envio.service';
import { CrearEnvioPayload } from '../../models/envio.model';

// Validador síncrono a nivel de FormGroup (3.3): fechaEntregaEstimada > fechaDespacho.
const fechasValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const despacho = group.get('fechaDespacho')?.value;
  const entrega = group.get('fechaEntregaEstimada')?.value;
  if (!despacho || !entrega) {
    return null;
  }
  return new Date(entrega) > new Date(despacho) ? null : { fechasInvalidas: true };
};

@Component({
  selector: 'app-envio-avanzado-form',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './envio-avanzado-form.component.html',
  styleUrl: './envio-avanzado-form.component.css',
})
export class EnvioAvanzadoFormComponent {
  private fb = inject(NonNullableFormBuilder);
  private envioService = inject(EnvioService);
  private router = inject(Router);

  mensaje = signal('');
  enviando = signal(false);

  form = this.fb.group(
    {
      numeroTracking: this.fb.control('', {
        validators: [Validators.required, Validators.pattern(/^EXP-\d{4}-\d{4}$/)],
        asyncValidators: [this.trackingAsyncValidator()],
      }),
      destinatario: ['', Validators.required],
      direccionDestino: ['', Validators.required],
      montoFlete: [0, [Validators.required, Validators.min(0.01)]],
      fechaDespacho: ['', Validators.required],
      fechaEntregaEstimada: ['', Validators.required],
      paquetes: this.fb.array([this.crearPaqueteGroup()]),
    },
    { validators: fechasValidator },
  );

  get paquetesArray() {
    return this.form.controls.paquetes;
  }

  crearPaqueteGroup() {
    return this.fb.group({
      descripcion: this.fb.control('', Validators.required),
      pesoKg: this.fb.control(0, [Validators.required, Validators.min(0.01)]),
    });
  }

  agregarPaquete(): void {
    this.paquetesArray.push(this.crearPaqueteGroup());
  }

  eliminarPaquete(index: number): void {
    if (this.paquetesArray.length > 1) {
      this.paquetesArray.removeAt(index);
    }
  }

  // Validador asíncrono (3.4): consulta GET /check-tracking/{codigo} en cada cambio
  // del campo. Angular cancela la peticion anterior si el usuario sigue escribiendo.
  private trackingAsyncValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      if (!control.value) {
        return of(null);
      }
      return of(control.value).pipe(
        debounceTime(400),
        switchMap((codigo) => this.envioService.checkTracking(codigo)),
        map((existe) => (existe ? { trackingTomado: true } : null)),
        catchError(() => of(null)),
      );
    };
  }

  registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.mensaje.set('Revise los campos marcados antes de enviar.');
      return;
    }

    const valores = this.form.getRawValue();
    const payload: CrearEnvioPayload = {
      codigoRastreo: valores.numeroTracking,
      destinatario: valores.destinatario,
      direccionDestino: valores.direccionDestino,
      montoFlete: valores.montoFlete,
      paquetes: valores.paquetes,
    };

    this.enviando.set(true);
    this.envioService.crearEnvio(payload).subscribe({
      next: () => {
        this.enviando.set(false);
        this.router.navigate(['/envios']);
      },
      error: (err) => {
        console.error('Error al registrar el envío', err);
        this.mensaje.set(err?.error?.detail ?? 'Error al registrar el envío.');
        this.enviando.set(false);
      },
    });
  }
}
