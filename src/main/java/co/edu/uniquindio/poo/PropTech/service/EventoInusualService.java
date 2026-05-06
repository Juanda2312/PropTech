package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.entity.Alerta;
import co.edu.uniquindio.poo.PropTech.model.entity.EventoInusual;
import co.edu.uniquindio.poo.PropTech.model.enums.EstadoEvento;
import co.edu.uniquindio.poo.PropTech.model.enums.NivelAtencion;
import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventoInusualService {

    private final SimpleLinkedList<EventoInusual> listaEventos = new SimpleLinkedList<>();
    private final AlertaService alertaService;

    public EventoInusualService(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    // ----------------------------------------------------------------
    // Detección y registro
    // ----------------------------------------------------------------

    public EventoInusual registrar(String idEvento, String tipo, String descripcion,
                                   NivelAtencion nivel) {
        EventoInusual evento = new EventoInusual(
                idEvento, tipo, descripcion, LocalDate.now(), nivel, EstadoEvento.ACTIVO
        );

        listaEventos.addLast(evento);

        // Todo evento inusual genera automáticamente una alerta
        alertaService.generar(
                "ALT-" + idEvento,
                "EVENTO_INUSUAL",
                "Detectado: " + tipo + " — " + descripcion,
                nivel
        );

        return evento;
    }

    public void cerrar(String idEvento) {
        for (EventoInusual e : listaEventos) {
            if (e.getIdEvento().equals(idEvento)) {
                e.setEstadoEvento(EstadoEvento.CERRADO);
                return;
            }
        }
        throw new RuntimeException("Evento no encontrado: " + idEvento);
    }

    // ----------------------------------------------------------------
    // Consultas
    // ----------------------------------------------------------------

    public List<EventoInusual> obtenerActivos() {
        List<EventoInusual> resultado = new ArrayList<>();
        for (EventoInusual e : listaEventos) {
            if (e.getEstadoEvento() == EstadoEvento.ACTIVO) resultado.add(e);
        }
        return resultado;
    }

    public List<EventoInusual> obtenerPorNivel(NivelAtencion nivel) {
        List<EventoInusual> resultado = new ArrayList<>();
        for (EventoInusual e : listaEventos) {
            if (e.getNivelAtencion() == nivel) resultado.add(e);
        }
        return resultado;
    }

    public List<EventoInusual> obtenerTodos() {
        List<EventoInusual> todos = new ArrayList<>();
        for (EventoInusual e : listaEventos) todos.add(e);
        return todos;
    }
}