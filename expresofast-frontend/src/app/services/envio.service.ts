import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Envio, CrearEnvioPayload, EstadoEnvio } from '../models/envio.model';

@Injectable({
  providedIn: 'root',
})
export class EnvioService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.API_URL}envios`;

  obtenerEnvios(): Observable<Envio[]> {
    return this.http.get<Envio[]>(this.baseUrl);
  }

  obtenerPorRastreo(codigo: string): Observable<Envio> {
    return this.http.get<Envio>(`${this.baseUrl}/rastreo/${codigo}`);
  }

  crearEnvio(payload: CrearEnvioPayload): Observable<Envio> {
    return this.http.post<Envio>(this.baseUrl, payload);
  }

  actualizarEstado(id: number, nuevoEstado: EstadoEnvio): Observable<Envio> {
    return this.http.patch<Envio>(`${this.baseUrl}/${id}/estado`, { nuevoEstado });
  }

  // Lab 11: validador asíncrono del número de rastreo (true = ya está en uso).
  checkTracking(codigo: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/check-tracking/${codigo}`);
  }
}
