import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ApiError } from './models';

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) =>
  next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('[API] Erro HTTP:', {
        method: request.method,
        url: request.urlWithParams,
        status: error.status,
        response: error.error,
      });
      const body = error.error as ApiError | null;
      const message = body?.mensagem ?? 'Não foi possível concluir a operação.';
      return throwError(() => new Error(message));
    }),
  );
