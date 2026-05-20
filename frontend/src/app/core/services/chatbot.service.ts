// src/app/core/services/chatbot.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type RolChat = 'ADMIN' | 'CLIENTE';

export interface MensajeChat {
    rol: 'user' | 'assistant';
    contenido: string;
    timestamp: Date;
}

export interface ContextoChatbot {
    rol: RolChat;
    stats?: {
        inmuebles: number;
        clientes: number;
        asesores: number;
        visitasPendientes: number;
        alertasAbiertas: number;
    };
    alertasCriticas?: { tipoAlerta: string; descripcion: string; nivel: string }[];
    rankingAsesores?: { nombre: string; especialidadZona: string }[];
    rankingZonas?: { zona: string; visitas: number }[];
    visitasPendientes?: { idVisita: string; cliente: { nombre: string }; inmueble: { direccion: string }; fecha: string }[];
    inmueblesRecientes?: { codigo: string; direccion: string; ciudad: string; precio: number; disponibilidad: boolean }[];
    nombreCliente?: string;
    presupuesto?: number;
    tipoInmuebleDeseado?: string;
    estadoBusqueda?: string;
    inmuebles?: { codigo: string; direccion: string; ciudad: string; tipoInmueble: string; finalidad: string; precio: number; habitaciones: number; disponibilidad: boolean }[];
    favoritos?: { codigo: string; direccion: string; ciudad: string; precio: number }[];
    recomendaciones?: { inmueble: { direccion: string; ciudad: string; precio: number; tipoInmueble: string }; puntaje: number; criterio: string }[];
    historial?: { direccion: string; ciudad: string; tipoInmueble: string }[];
}

@Injectable({ providedIn: 'root' })
export class ChatbotService {

    private readonly API_URL = 'https://openrouter.ai/api/v1/chat/completions';
    private readonly API_KEY = 'API_KEY_AQUI';
    private readonly MODEL = 'openrouter/free';

    constructor(private http: HttpClient) {}

    enviarMensaje(
        mensaje: string,
        historial: MensajeChat[],
        contexto: ContextoChatbot
    ): Observable<string> {
        const systemPrompt = this.construirSystemPrompt(contexto);

        const messages = [
            { role: 'system', content: systemPrompt },
            ...historial.slice(-6).map(m => ({
                role: m.rol === 'user' ? 'user' : 'assistant',
                content: m.contenido
            })),
            { role: 'user', content: mensaje }
        ];

        const body = {
            model: this.MODEL,
            messages,
            max_tokens: 512,
            temperature: 0.7
        };

        return new Observable(observer => {
            this.http.post<any>(this.API_URL, body, {
                headers: {
                    'Authorization': `Bearer ${this.API_KEY}`,
                    'Content-Type': 'application/json',
                    'HTTP-Referer': 'http://localhost:4200',
                    'X-Title': 'PropTech Chatbot'
                }
            }).subscribe({
                next: (res) => {
                    const texto = res?.choices?.[0]?.message?.content ?? 'No obtuve respuesta. Intenta de nuevo.';
                    observer.next(texto);
                    observer.complete();
                },
                error: (err) => {
                    console.error('OpenRouter API error:', err);
                    let msg = 'No pude conectarme con el asistente. Intenta de nuevo.';
                    if (err.status === 429) msg = 'Límite de solicitudes alcanzado. Espera unos segundos e intenta de nuevo.';
                    if (err.status === 401) msg = 'API Key inválida. Verifica la configuración.';
                    if (err.status === 402) msg = 'Sin créditos disponibles en la cuenta.';
                    observer.next(msg);
                    observer.complete();
                }
            });
        });
    }

    private construirSystemPrompt(ctx: ContextoChatbot): string {
        const LIMITE = `Eres PropBot, el asistente inteligente de PropTech. 
REGLA FUNDAMENTAL: Solo responde preguntas relacionadas con bienes raíces, inmuebles, el sistema PropTech, 
y la información del contexto que se te provee. Si te preguntan algo fuera de ese ámbito, 
responde amablemente: "Solo puedo ayudarte con temas inmobiliarios y del sistema PropTech. ¿En qué puedo ayudarte?"
Sé conciso, profesional y útil. Responde en español. Usa emojis ocasionalmente para ser más amigable.`;

        if (ctx.rol === 'ADMIN') {
            return `${LIMITE}

Eres el asistente del panel de ADMINISTRACIÓN de PropTech. Tienes acceso a los siguientes datos del dashboard en tiempo real:

📊 ESTADÍSTICAS ACTUALES:
- Inmuebles registrados: ${ctx.stats?.inmuebles ?? 0}
- Clientes activos: ${ctx.stats?.clientes ?? 0}
- Asesores: ${ctx.stats?.asesores ?? 0}
- Visitas pendientes en cola: ${ctx.stats?.visitasPendientes ?? 0}
- Alertas abiertas: ${ctx.stats?.alertasAbiertas ?? 0}

🚨 ALERTAS RECIENTES (${ctx.alertasCriticas?.length ?? 0}):
${ctx.alertasCriticas?.map(a => `- [${a.nivel}] ${a.tipoAlerta}: ${a.descripcion}`).join('\n') ?? 'Sin alertas críticas'}

🏆 RANKING ASESORES (top):
${ctx.rankingAsesores?.map((a, i) => `${i + 1}. ${a.nombre} — Zona: ${a.especialidadZona}`).join('\n') ?? 'Sin datos'}

📍 ACTIVIDAD POR ZONA:
${ctx.rankingZonas?.map(z => `- ${z.zona}: ${z.visitas} visitas`).join('\n') ?? 'Sin datos'}

📅 VISITAS PENDIENTES PRÓXIMAS:
${ctx.visitasPendientes?.map(v => `- ${v.idVisita}: ${v.cliente?.nombre} → ${v.inmueble?.direccion} (${v.fecha})`).join('\n') ?? 'Sin visitas pendientes'}

🏠 INMUEBLES RECIENTES:
${ctx.inmueblesRecientes?.map(i => `- ${i.codigo}: ${i.direccion}, ${i.ciudad} | $${i.precio.toLocaleString('es-CO')} | ${i.disponibilidad ? 'Disponible' : 'No disponible'}`).join('\n') ?? 'Sin inmuebles'}

Usa estos datos para responder preguntas sobre el estado del negocio, rendimiento de asesores, 
análisis de la plataforma y recomendaciones operativas.`;
        }

        return `${LIMITE}

Estás asistiendo al cliente: ${ctx.nombreCliente ?? 'Cliente'}.

👤 PERFIL DEL CLIENTE:
- Presupuesto: $${ctx.presupuesto?.toLocaleString('es-CO') ?? 'No definido'}
- Tipo de inmueble deseado: ${ctx.tipoInmuebleDeseado ?? 'No definido'}
- Estado de búsqueda: ${ctx.estadoBusqueda ?? 'Desconocido'}

🏠 INMUEBLES DISPONIBLES (${ctx.inmuebles?.length ?? 0}):
${ctx.inmuebles?.slice(0, 10).map(i => `- ${i.codigo}: ${i.direccion}, ${i.ciudad} | ${i.tipoInmueble} | ${i.finalidad} | $${i.precio.toLocaleString('es-CO')} | ${i.habitaciones} hab.`).join('\n') ?? 'Sin inmuebles disponibles'}
${(ctx.inmuebles?.length ?? 0) > 10 ? `... y ${(ctx.inmuebles?.length ?? 0) - 10} más.` : ''}

⭐ FAVORITOS (${ctx.favoritos?.length ?? 0}):
${ctx.favoritos?.map(f => `- ${f.codigo}: ${f.direccion}, ${f.ciudad} | $${f.precio.toLocaleString('es-CO')}`).join('\n') ?? 'Sin favoritos'}

✨ RECOMENDACIONES (${ctx.recomendaciones?.length ?? 0}):
${ctx.recomendaciones?.slice(0, 5).map(r => `- ${r.inmueble?.direccion}, ${r.inmueble?.ciudad} | Puntaje: ${r.puntaje}/100 | Por: ${r.criterio}`).join('\n') ?? 'Sin recomendaciones aún.'}

📋 HISTORIAL RECIENTE:
${ctx.historial?.slice(0, 5).map(h => `- ${h.direccion}, ${h.ciudad} (${h.tipoInmueble})`).join('\n') ?? 'Sin historial'}

SECCIONES DISPONIBLES:
1. 🏠 Inmuebles disponibles
2. ⭐ Favoritos
3. 📋 Consultados
4. 📊 Mis Interacciones
5. ✨ Recomendaciones`;
    }
}