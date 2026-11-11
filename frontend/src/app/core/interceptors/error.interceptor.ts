// src/app/core/interceptors/error.interceptor.ts
import { Injectable } from '@angular/core';
import {
    HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // Solo interceptamos las llamadas a nuestra API:
        //  - rutas relativas (desarrollo con proxy, apiUrl = '')
        //  - URLs absolutas que empiezan por environment.apiUrl (producción)
        // Cualquier otra URL externa se deja pasar sin tocar.
        const esApiPropia = !req.url.startsWith('http')
            || (environment.apiUrl !== '' && req.url.startsWith(environment.apiUrl));

        if (!esApiPropia) {
            return next.handle(req);
        }

        return next.handle(req).pipe(
            catchError((error: HttpErrorResponse) => {
                let msg = 'Error desconocido';
                if (error.status === 0) {
                    // Sin respuesta: backend dormido (Render gratis), caído o sin red
                    msg = 'No se pudo conectar con el servidor. Si estaba inactivo, espera cerca de un minuto y vuelve a intentarlo.';
                } else if (error.error?.message) msg = error.error.message;
                else if (typeof error.error === 'string') msg = error.error;
                else if (error.message) msg = error.message;
                console.error('[PropTech Error]', error.status, msg);
                return throwError(() => ({ status: error.status, message: msg }));
            })
        );
    }
}