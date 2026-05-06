package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.entity.EventoInusual;
import co.edu.uniquindio.poo.PropTech.model.enums.EstadoEvento;
import co.edu.uniquindio.poo.PropTech.model.enums.NivelAtencion;
import co.edu.uniquindio.poo.PropTech.repository.EventoInusualRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventoInusualService {

    private final EventoInusualRepository eventoRepository;
    private final AlertaService alertaService;

    public EventoInusualService(EventoInusualRepository eventoRepository,
                                AlertaService alertaService) {
        this.eventoRepository = eventoRepository;
        this.alertaService    = alertaService;
    }

    // ----------------------------------------------------------------
    // Detección y registro
    // ----------------------------------------------------------------

    public EventoInusual registrar(String idEvento, String tipo,
                                   String descripcion, NivelAtencion nivel) {
        EventoInusual evento = new EventoInusual(
                idEvento, tipo, descripcion, LocalDate.now(), nivel, EstadoEvento.ACTIVO
        );
        eventoRepository.save(evento);

        // Todo evento inusual genera automáticamente una alerta
        alertaService.generar("ALT-" + idEvento, "EVENTO_INUSUAL",
                "Detectado: " + tipo + " — " + descripcion, nivel);

        return evento;
    }

    public void cerrar(String idEvento) {
        EventoInusual evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + idEvento));
        evento.setEstadoEvento(EstadoEvento.CERRADO);
    }

    // ----------------------------------------------------------------
    // Consultas
    // ----------------------------------------------------------------

    public List<EventoInusual> obtenerTodos() {
        return eventoRepository.findAll();
    }

    public List<EventoInusual> obtenerActivos() {
        return eventoRepository.findByEstado(EstadoEvento.ACTIVO);
    }

    public List<EventoInusual> obtenerPorNivel(NivelAtencion nivel) {
        return eventoRepository.findByNivel(nivel);
    }
}