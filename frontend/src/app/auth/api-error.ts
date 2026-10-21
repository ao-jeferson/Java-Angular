import { HttpErrorResponse } from '@angular/common/http';
export function apiError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) return 'Não foi possível concluir a operação.';
  if (error.status === 0)
    return 'Não foi possível conectar à API. Confira se o backend está em execução.';
  const fields = error.error?.errors as Record<string, string> | undefined;
  return fields
    ? Object.values(fields).join(' ')
    : (error.error?.detail ?? 'Não foi possível concluir a operação.');
}
