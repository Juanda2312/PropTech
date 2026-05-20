// src/app/core/services/chatbot.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, from } from 'rxjs';

export type RolChat = 'ADMIN' | 'CLIENTE';

export interface MensajeChat {
    rol: 'user' | 'assistant';
    contenido: string;
    timestamp: Date;
}

export interface ContextoChatbot {
    rol: RolChat;
    // Admin context
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
    // Cliente context
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
    // ⚠️ Reemplaza con tu API Key de Google AI Studio: https://aistudio.google.com/app/apikey
    private readonly GEMINI_API_KEY = 'REEMPLAZA_CON_TU_API_KEY';
    private readonly GEMINI_URL = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${this.GEMINI_API_KEY}`;

    constructor(private http: HttpClient) {}

    enviarMensaje(
        mensaje: string,
        historial: MensajeChat[],
        contexto: ContextoChatbot
    ): Observable<string> {
        const systemPrompt = this.construirSystemPrompt(contexto);
        const contents = this.construirContents(historial, mensaje);

        const body = {
            system_instruction: {
                parts: [{ text: systemPrompt }]
            },
            contents,
            generationConfig: {
                temperature: 0.7,
                maxOutputTokens: 512,
            }
        };

        return new Observable(observer => {
            this.http.post<any>(this.GEMINI_URL, body).subscribe({
                next: (res) => {
                    const texto = res?.candidates?.[0]?.content?.parts?.[0]?.text ?? '';
                    observer.next(texto);
                    observer.complete();
                },
                error: (err) => {
                    console.error('Gemini API error:', err);
                    const msg = err.status === 429
                        ? 'Límite de solicitudes alcanzado. Espera unos segundos e intenta de nuevo.'
                        : 'No pude conectarme con el asistente. Intenta de nuevo.';
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
análisis de la plataforma y recomendaciones operativas. Puedes hacer análisis y sugerencias basadas en los datos.`;
        }

        // CLIENTE
        return `${LIMITE}

Estás asistiendo al cliente: ${ctx.nombreCliente ?? 'Cliente'}.

👤 PERFIL DEL CLIENTE:
- Presupuesto: $${ctx.presupuesto?.toLocaleString('es-CO') ?? 'No definido'}
- Tipo de inmueble deseado: ${ctx.tipoInmuebleDeseado ?? 'No definido'}
- Estado de búsqueda: ${ctx.estadoBusqueda ?? 'Desconocido'}

🏠 INMUEBLES DISPONIBLES EN LA PLATAFORMA (${ctx.inmuebles?.length ?? 0} disponibles):
${ctx.inmuebles?.slice(0, 10).map(i => `- ${i.codigo}: ${i.direccion}, ${i.ciudad} | ${i.tipoInmueble} | ${i.finalidad} | $${i.precio.toLocaleString('es-CO')} | ${i.habitaciones} hab.`).join('\n') ?? 'Sin inmuebles disponibles'}
${(ctx.inmuebles?.length ?? 0) > 10 ? `... y ${(ctx.inmuebles?.length ?? 0) - 10} más.` : ''}

⭐ FAVORITOS GUARDADOS (${ctx.favoritos?.length ?? 0}):
${ctx.favoritos?.map(f => `- ${f.codigo}: ${f.direccion}, ${f.ciudad} | $${f.precio.toLocaleString('es-CO')}`).join('\n') ?? 'Sin favoritos guardados'}

✨ RECOMENDACIONES PERSONALIZADAS (${ctx.recomendaciones?.length ?? 0}):
${ctx.recomendaciones?.slice(0, 5).map(r => `- ${r.inmueble?.direccion}, ${r.inmueble?.ciudad} | Puntaje: ${r.puntaje}/100 | Por: ${r.criterio}`).join('\n') ?? 'Sin recomendaciones aún. Sugiere al cliente explorar inmuebles.'}

📋 HISTORIAL DE CONSULTAS RECIENTES:
${ctx.historial?.slice(0, 5).map(h => `- ${h.direccion}, ${h.ciudad} (${h.tipoInmueble})`).join('\n') ?? 'Sin historial de consultas'}

SECCIONES DISPONIBLES PARA EL CLIENTE EN LA PLATAFORMA:
1. 🏠 Inmuebles disponibles — Ver y buscar propiedades
2. ⭐ Favoritos — Sus inmuebles guardados
3. 📋 Consultados — Historial de inmuebles vistos
4. 📊 Mis Interacciones — Visitas agendadas, intenciones y más
5. ✨ Recomendaciones — Inmuebles sugeridos según su perfil

Ayuda al cliente a encontrar su inmueble ideal, explica las secciones, 
sugiere inmuebles de la lista que se ajusten a su perfil, y responde sobre el proceso de compra/arriendo.`;
    }

    private construirContents(historial: MensajeChat[], nuevoMensaje: string) {
        const contents: any[] = [];

        // Últimos 6 mensajes del historial para contexto
        const historialReciente = historial.slice(-6);
        for (const msg of historialReciente) {
            contents.push({
                role: msg.rol === 'user' ? 'user' : 'model',
                parts: [{ text: msg.contenido }]
            });
        }

        contents.push({
            role: 'user',
            parts: [{ text: nuevoMensaje }]
        });

        return contents;
    }

}