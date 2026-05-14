package co.edu.uniquindio.poo.PropTech.repository;

import co.edu.uniquindio.poo.PropTech.model.entity.Alerta;
import co.edu.uniquindio.poo.PropTech.model.enums.NivelAtencion;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import co.edu.uniquindio.poo.PropTech.structures.PriorityQueue;
import co.edu.uniquindio.poo.PropTech.structures.Queue;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AlertaRepository {

    private final HashTable<String, Alerta> tablaPorId = new HashTable<>();
    private final Queue<Alerta> colaPendientes = new Queue<>();

    // Cola de prioridad para alertas críticas (contratos por vencer, etc.)
    private final PriorityQueue<Alerta> colaPrioridad = new PriorityQueue<>();

    public Alerta save(Alerta alerta) {
        tablaPorId.put(alerta.getIdAlerta(), alerta);
        if (!alerta.isCerrada()) {
            colaPendientes.enqueue(alerta);
            colaPrioridad.enqueue(alerta);
        }
        return alerta;
    }

    public void delete(String id) {
        tablaPorId.remove(id);
    }

    public Optional<Alerta> findById(String id) {
        return Optional.ofNullable(tablaPorId.get(id));
    }

    public boolean existsById(String id) {
        return tablaPorId.containsKey(id);
    }

    public List<Alerta> findAll() {
        List<Alerta> todas = new ArrayList<>();
        for (Alerta a : tablaPorId) todas.add(a);
        return todas;
    }

    public List<Alerta> findAbiertas() {
        List<Alerta> resultado = new ArrayList<>();
        for (Alerta a : tablaPorId) {
            if (!a.isCerrada()) resultado.add(a);
        }
        return resultado;
    }

    public List<Alerta> findByNivel(NivelAtencion nivel) {
        List<Alerta> resultado = new ArrayList<>();
        for (Alerta a : tablaPorId) {
            if (a.getNivel() == nivel) resultado.add(a);
        }
        return resultado;
    }

    public Optional<Alerta> pollPendiente() {
        if (colaPendientes.isEmpty()) return Optional.empty();
        return Optional.of(colaPendientes.dequeue());
    }

    //extrae la alerta más crítica primero
    public Optional<Alerta> pollPrioridad() {
        if (colaPrioridad.isEmpty()) return Optional.empty();
        return Optional.of(colaPrioridad.dequeue());
    }

    public int sizePendientes() {
        return colaPendientes.getSize();
    }

    public int sizePrioridad() {
        return colaPrioridad.getSize();
    }
}