// src/app/core/interceptors/error.interceptor.ts
import { Injectable } from '@angular/core';
import {
    HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(req).pipe(
            catchError((error: HttpErrorResponse) => {
                let msg = 'Error desconocido';
                if (error.error?.message) msg = error.error.message;
                else if (typeof error.error === 'string') msg = error.error;
                else if (error.message) msg = error.message;
                console.error('[PropTech Error]', error.status, msg);
                return throwError(() => ({ status: error.status, message: msg }));
            })
        );
    }
}