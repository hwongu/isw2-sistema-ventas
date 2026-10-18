import { HttpErrorResponse } from '@angular/common/http';

interface ErrorBody {
  error?: string;
  mensaje?: string;
}

export function obtenerMensajeError(error: unknown, mensajeGenerico: string): string {
  if (!(error instanceof HttpErrorResponse)) {
    return mensajeGenerico;
  }

  const body = error.error as ErrorBody | string | null;
  if (typeof body === 'string') {
    return body.trim() || mensajeGenerico;
  }

  return body?.error ?? body?.mensaje ?? mensajeGenerico;
}
