// src/app/core/services/toast.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'warn' | 'info';
  icon: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private id = 0;
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastsSubject.asObservable();

  private show(message: string, type: Toast['type'], icon: string) {
    const toast: Toast = { id: ++this.id, message, type, icon };
    this.toastsSubject.next([...this.toastsSubject.value, toast]);
    setTimeout(() => this.remove(toast.id), 3500);
  }

  success(message: string) { this.show(message, 'success', '✓'); }
  error(message: string)   { this.show(message, 'error',   '✕'); }
  warn(message: string)    { this.show(message, 'warn',    '⚠'); }
  info(message: string)    { this.show(message, 'info',    'ℹ'); }

  private remove(id: number) {
    this.toastsSubject.next(this.toastsSubject.value.filter(t => t.id !== id));
  }
}
