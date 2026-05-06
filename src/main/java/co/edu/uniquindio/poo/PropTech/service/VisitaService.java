package co.edu.uniquindio.poo.PropTech.service;

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
            throw new RuntimeException("Ya existe una visita con id: " + dto.getIdVisita());
        }
        if (!inmueble.isDisponibilidad()) {
            throw new RuntimeException("El inmueble no está disponible para visitas");
        }

        Visita visita = new Visita(
                dto.getIdVisita(), cliente, inmueble,
                dto.getFecha(), dto.getHora(),
                asesor, EstadoVisita.PENDIENTE, dto.getObservaciones()
        );

        return visitaRepository.save(visita);
    }

    public void confirmar(String idVisita) {
        buscarPorId(idVisita).setEstado(EstadoVisita.CONFIRMADA);
    }

    public void cancelar(String idVisita, String observacion) {
        Visita visita = buscarPorId(idVisita);
        visita.setEstado(EstadoVisita.CANCELADA);
        visita.setObservaciones(observacion);
    }

    public void reprogramar(String idVisita, VisitaDTO dto) {
        Visita visita = buscarPorId(idVisita);
        visita.setFecha(dto.getFecha());
        visita.setHora(dto.getHora());
        visita.setEstado(EstadoVisita.REPROGRAMADA);
        visitaRepository.enqueueParaProcesar(visita);
    }

    public void marcarRealizada(String idVisita, String observaciones) {
        Visita visita = buscarPorId(idVisita);
        visita.setEstado(EstadoVisita.REALIZADA);
        visita.setObservaciones(observaciones);
    }

    // ----------------------------------------------------------------
    // Cola de pendientes
    // ----------------------------------------------------------------

    public Visita procesarSiguientePendiente() {
        return visitaRepository.pollPendiente()
                .orElseThrow(() -> new RuntimeException("No hay visitas pendientes en la cola"));
    }

    public int totalPendientes() {
        return visitaRepository.sizePendientes();
    }

    // ----------------------------------------------------------------
    // Consultas
    // ----------------------------------------------------------------

    public Visita buscarPorId(String id) {
        return visitaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visita no encontrada: " + id));
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