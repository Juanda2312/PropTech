// src/app/shared/chatbot/chatbot.component.ts
import { Component, Input, OnChanges, SimpleChanges, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService, MensajeChat, ContextoChatbot, RolChat } from '../../core/services/chatbot.service';

@Component({
    selector: 'app-chatbot',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './chatbot.component.html',
    styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent implements OnChanges, AfterViewChecked {
    @Input() contexto!: ContextoChatbot;
    @ViewChild('messagesContainer') messagesContainer!: ElementRef;

    abierto = false;
    mensajes: MensajeChat[] = [];
    inputTexto = '';
    cargando = false;
    private shouldScroll = false;

    sugerencias: string[] = [];

    constructor(private chatbotService: ChatbotService) {}

    ngOnChanges(changes: SimpleChanges) {
        if (changes['contexto'] && this.contexto) {
            this.actualizarSugerencias();
            if (this.mensajes.length === 0) {
                this.agregarBienvenida();
            }
        }
    }

    ngAfterViewChecked() {
        if (this.shouldScroll) {
            this.scrollAlFinal();
            this.shouldScroll = false;
        }
    }

    private actualizarSugerencias() {
        if (this.contexto.rol === 'ADMIN') {
            this.sugerencias = [
                '¿Cómo está el rendimiento de los asesores?',
                '¿Cuáles son las alertas más críticas?',
                '¿Qué zona tiene más actividad?',
                'Resumen del dashboard',
            ];
        } else {
            this.sugerencias = [
                '¿Qué inmuebles se ajustan a mi presupuesto?',
                '¿Cómo agendo una visita?',
                'Muéstrame mis recomendaciones',
                '¿Qué secciones tengo disponibles?',
            ];
        }
    }

    private agregarBienvenida() {
        const bienvenida = this.contexto.rol === 'ADMIN'
            ? `¡Hola! 👋 Soy **PropBot**, tu asistente de administración.\n\nTengo acceso a los datos actuales del dashboard: **${this.contexto.stats?.inmuebles ?? 0} inmuebles**, **${this.contexto.stats?.clientes ?? 0} clientes**, **${this.contexto.stats?.alertasAbiertas ?? 0} alertas abiertas**.\n\n¿En qué puedo ayudarte hoy?`
            : `¡Hola, **${this.contexto.nombreCliente ?? 'bienvenido/a'}**! 👋 Soy **PropBot**, tu asistente inmobiliario.\n\nConozco tu perfil de búsqueda y los inmuebles disponibles. Puedo ayudarte a encontrar tu próximo hogar, explicarte las secciones de la plataforma o responder dudas sobre el proceso.\n\n¿En qué puedo ayudarte?`;

        this.mensajes.push({
            rol: 'assistant',
            contenido: bienvenida,
            timestamp: new Date()
        });
        this.shouldScroll = true;
    }

    toggleChat() {
        this.abierto = !this.abierto;
        if (this.abierto) {
            this.shouldScroll = true;
            setTimeout(() => {
                const input = document.querySelector('.chat-input') as HTMLInputElement;
                input?.focus();
            }, 150);
        }
    }

    usarSugerencia(sugerencia: string) {
        this.inputTexto = sugerencia;
        this.enviar();
    }

    enviar() {
        const texto = this.inputTexto.trim();
        if (!texto || this.cargando) return;

        this.mensajes.push({ rol: 'user', contenido: texto, timestamp: new Date() });
        this.inputTexto = '';
        this.cargando = true;
        this.shouldScroll = true;

        this.chatbotService.enviarMensaje(texto, this.mensajes.slice(0, -1), this.contexto).subscribe({
            next: (respuesta) => {
                this.mensajes.push({ rol: 'assistant', contenido: respuesta, timestamp: new Date() });
                this.cargando = false;
                this.shouldScroll = true;
            },
            error: () => {
                this.mensajes.push({
                    rol: 'assistant',
                    contenido: 'Ocurrió un error. Por favor intenta de nuevo.',
                    timestamp: new Date()
                });
                this.cargando = false;
                this.shouldScroll = true;
            }
        });
    }

    limpiarChat() {
        this.mensajes = [];
        this.agregarBienvenida();
    }

    private scrollAlFinal() {
        try {
            const el = this.messagesContainer?.nativeElement;
            if (el) el.scrollTop = el.scrollHeight;
        } catch {}
    }

    formatearHora(date: Date): string {
        return date.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
    }

    // Convierte **negrita** y saltos de línea básicos a HTML seguro
    formatearMarkdown(texto: string): string {
        return texto
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
            .replace(/\n/g, '<br>');
    }
}