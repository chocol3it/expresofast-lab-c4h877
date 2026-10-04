export type EstadoEnvio = 'PENDIENTE' | 'EN_TRANSITO' | 'ENTREGADO' | 'CANCELADO';

export interface Paquete {
  id?: number;
  descripcion: string;
  pesoKg: number;
}

export interface Envio {
  id: number;
  codigoRastreo: string;
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
  estado: EstadoEnvio;
  fechaCreacion: string;
  paquetes: Paquete[];
}

// Lab 11: el envio avanzado ahora viaja con su numero de rastreo (lo escribe
// el operador, validado en tiempo real) y la lista de paquetes asociados.
export interface CrearEnvioPayload {
  codigoRastreo: string;
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
  paquetes: Paquete[];
}
