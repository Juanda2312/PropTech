package co.edu.uniquindio.poo.PropTech.repository;

import co.edu.uniquindio.poo.PropTech.model.entity.Visita;
import co.edu.uniquindio.poo.PropTech.model.enums.EstadoVisita;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import co.edu.uniquindio.poo.PropTech.structures.Queue;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class VisitaRepository {

    // Acceso O(1) por id
    private final HashTable<String, Visita> tablaPorId = new HashTable<>();

    // Cola FIFO de visitas pendientes de procesar
    private final Queue<Visita> colaPendientes = new Queue<>();

    // ----------------------------------------------------------------
    // Escritura
    // ----------------------------------------------------------------

    public Visita save(Visita visita) {
        tablaPorId.put(visita.getIdVisita(), visita);
        // Solo encolamos si entra en estado PENDIENTE
        if (visita.getEstado() == EstadoVisita.PENDIENTE) {
            colaPendientes.enqueue(visita);
        }
        return visita;
    }

    public void enqueueParaProcesar(Visita visita) {
        colaPendientes.enqueue(visita);
    }

    public void delete(String id) {
        tablaPorId.remove(id);
    }

    // ----------------------------------------------------------------
    // Lectura
    // ----------------------------------------------------------------

    public Optional<Visita> findById(String id) {
        return Optional.ofNullable(tablaPorId.get(id));
    }

    public boolean existsById(String id) {
        return tablaPorId.containsKey(id);
    }

    public List<Visita> findAll() {
        List<Visita> todas = new ArrayList<>();
        for (Visita v : tablaPorId) todas.add(v);
        return todas;
    }

    public List<Visita> findByEstado(EstadoVisita estado) {
        List<Visita> resultado = new ArrayList<>();
        for (Visita v : tablaPorId) {
            if (v.getEstado() == estado) resultado.add(v);
        }
        return resultado;
    }

    public List<Visita> findByCliente(String idCliente) {
        List<Visita> resultado = new ArrayList<>();
        for (Visita v : tablaPorId) {
            if (v.getCliente().getId().equals(idCliente)) resultado.add(v);
        }
        return resultado;
    }

    public List<Visita> findByInmueble(String codigoInmueble) {
        List<Visita> resultado = new ArrayList<>();
        for (Visita v : tablaPorId) {
            if (v.getInmueble().getCodigo().equals(codigoInmueble)) resultado.add(v);
        }
        return resultado;
    }

    // ----------------------------------------------------------------
    // Cola de pendientes
    // ----------------------------------------------------------------

    public Optional<Visita> pollPendiente() {
        if (colaPendientes.isEmpty()) return Optional.empty();
        return Optional.of(colaPendientes.dequeue());
    }

    public int sizePendientes() {
        return colaPendientes.getSize();
    }

    public boolean hayPendientes() {
        return !colaPendientes.isEmpty();
    }
}