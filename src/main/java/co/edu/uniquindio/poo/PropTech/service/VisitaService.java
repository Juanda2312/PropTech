package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.dto.VisitaDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Visita;
import co.edu.uniquindio.poo.PropTech.model.enums.EstadoVisita;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import co.edu.uniquindio.poo.PropTech.structures.Queue;
import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VisitaService {

    private final HashTable<String, Visita> tablaPorId    = new HashTable<>();
    private final Queue<Visita> colaVisitasPendientes     = new Queue<>();

    public Visita programar(VisitaDTO dto, Cliente cliente, Inmueble inmueble, Asesor asesor) {
        if (tablaPorId.containsKey(dto.getIdVisita())) {
            throw new RuntimeException("Ya existe una visita con id: " + dto.getIdVisita());
        }
        if (!inmueble.isDisponibilidad()) {
            throw new RuntimeException("El inmueble no está disponible para visitas");
        }

        Visita visita = new Visita(
                dto.getIdVisita(), cliente, inmueble, dto.getFecha(),
                dto.getHora(), asesor, EstadoVisita.PENDIENTE, dto.getObservaciones()
        );

        tablaPorId.put(visita.getIdVisita(), visita);
        inmueble.getListaVisitas().addLast(visita);
        colaVisitasPendientes.enqueue(visita);
        return visita;
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
        colaVisitasPendientes.enqueue(visita);
    }

    public void marcarRealizada(String idVisita, String observaciones) {
        Visita visita = buscarPorId(idVisita);
        visita.setEstado(EstadoVisita.REALIZADA);
        visita.setObservaciones(observaciones);
    }

    public Visita procesarSiguientePendiente() {
        if (colaVisitasPendientes.isEmpty()) {
            throw new RuntimeException("No hay visitas pendientes en la cola");
        }
        return colaVisitasPendientes.dequeue();
    }

    public int totalPendientes() {
        return colaVisitasPendientes.getSize();
    }

    public Visita buscarPorId(String id) {
        Visita v = tablaPorId.get(id);
        if (v == null) throw new RuntimeException("Visita no encontrada: " + id);
        return v;
    }

    public List<Visita> obtenerPorInmueble(String codigoInmueble) {
        List<Visita> resultado = new ArrayList<>();
        for (Visita v : tablaPorId) {
            if (v.getInmueble().getCodigo().equals(codigoInmueble)) resultado.add(v);
        }
        return resultado;
    }

    public List<Visita> obtenerPorCliente(String idCliente) {
        List<Visita> resultado = new ArrayList<>();
        for (Visita v : tablaPorId) {
            if (v.getCliente().getId().equals(idCliente)) resultado.add(v);
        }
        return resultado;
    }

    public List<Visita> obtenerPorEstado(EstadoVisita estado) {
        List<Visita> resultado = new ArrayList<>();
        for (Visita v : tablaPorId) {
            if (v.getEstado() == estado) resultado.add(v);
        }
        return resultado;
    }

    public List<Visita> obtenerTodas() {
        List<Visita> todas = new ArrayList<>();
        for (Visita v : tablaPorId) todas.add(v);
        return todas;
    }
}