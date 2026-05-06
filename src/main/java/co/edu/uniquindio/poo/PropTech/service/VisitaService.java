package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.exception.EntidadDuplicadaException;
import co.edu.uniquindio.poo.PropTech.exception.EntidadNoEncontradaException;
import co.edu.uniquindio.poo.PropTech.exception.EstadoInvalidoException;
import co.edu.uniquindio.poo.PropTech.exception.InmuebleNoDisponibleException;
import co.edu.uniquindio.poo.PropTech.exception.ReglaNegocioException;
import co.edu.uniquindio.poo.PropTech.model.dto.VisitaDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Visita;
import co.edu.uniquindio.poo.PropTech.model.enums.EstadoVisita;
import co.edu.uniquindio.poo.PropTech.repository.VisitaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitaService {

    private final VisitaRepository visitaRepository;

    public VisitaService(VisitaRepository visitaRepository) {
        this.visitaRepository = visitaRepository;
    }

    // ----------------------------------------------------------------
    // Programación
    // ----------------------------------------------------------------

    public Visita programar(VisitaDTO dto, Cliente cliente, Inmueble inmueble, Asesor asesor) {
        if (visitaRepository.existsById(dto.getIdVisita())) {
            throw new EntidadDuplicadaException("Visita", dto.getIdVisita());
        }
        if (!inmueble.isDisponibilidad()) {
            throw new InmuebleNoDisponibleException(inmueble.getCodigo());
        }

        Visita visita = new Visita(
                dto.getIdVisita(), cliente, inmueble,
                dto.getFecha(), dto.getHora(),
                asesor, EstadoVisita.PENDIENTE, dto.getObservaciones()
        );

        return visitaRepository.save(visita);
    }

    public void confirmar(String idVisita) {
        Visita visita = buscarPorId(idVisita);
        if (visita.getEstado() == EstadoVisita.CANCELADA) {
            throw new EstadoInvalidoException("Visita", visita.getEstado().name(), "CONFIRMAR");
        }
        if (visita.getEstado() == EstadoVisita.REALIZADA) {
            throw new EstadoInvalidoException("Visita", visita.getEstado().name(), "CONFIRMAR");
        }
        visita.setEstado(EstadoVisita.CONFIRMADA);
    }

    public void cancelar(String idVisita, String observacion) {
        Visita visita = buscarPorId(idVisita);
        if (visita.getEstado() == EstadoVisita.REALIZADA) {
            throw new EstadoInvalidoException("Visita", visita.getEstado().name(), "CANCELAR");
        }
        visita.setEstado(EstadoVisita.CANCELADA);
        visita.setObservaciones(observacion);
    }

    public void reprogramar(String idVisita, VisitaDTO dto) {
        Visita visita = buscarPorId(idVisita);
        if (visita.getEstado() == EstadoVisita.CANCELADA) {
            throw new EstadoInvalidoException("Visita", visita.getEstado().name(), "REPROGRAMAR");
        }
        if (visita.getEstado() == EstadoVisita.REALIZADA) {
            throw new EstadoInvalidoException("Visita", visita.getEstado().name(), "REPROGRAMAR");
        }
        visita.setFecha(dto.getFecha());
        visita.setHora(dto.getHora());
        visita.setEstado(EstadoVisita.REPROGRAMADA);
        visitaRepository.enqueueParaProcesar(visita);
    }

    public void marcarRealizada(String idVisita, String observaciones) {
        Visita visita = buscarPorId(idVisita);
        if (visita.getEstado() == EstadoVisita.CANCELADA) {
            throw new EstadoInvalidoException("Visita", visita.getEstado().name(), "MARCAR_REALIZADA");
        }
        visita.setEstado(EstadoVisita.REALIZADA);
        visita.setObservaciones(observaciones);
    }

    // ----------------------------------------------------------------
    // Cola de pendientes
    // ----------------------------------------------------------------

    public Visita procesarSiguientePendiente() {
        return visitaRepository.pollPendiente()
                .orElseThrow(() -> new ReglaNegocioException(
                        "No hay visitas pendientes en la cola para procesar."));
    }

    public int totalPendientes() {
        return visitaRepository.sizePendientes();
    }

    // ----------------------------------------------------------------
    // Consultas
    // ----------------------------------------------------------------

    public Visita buscarPorId(String id) {
        return visitaRepository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Visita", id));
    }

    public List<Visita> obtenerTodas() {
        return visitaRepository.findAll();
    }

    public List<Visita> obtenerPorEstado(EstadoVisita estado) {
        return visitaRepository.findByEstado(estado);
    }

    public List<Visita> obtenerPorCliente(String idCliente) {
        return visitaRepository.findByCliente(idCliente);
    }

    public List<Visita> obtenerPorInmueble(String codigoInmueble) {
        return visitaRepository.findByInmueble(codigoInmueble);
    }
}