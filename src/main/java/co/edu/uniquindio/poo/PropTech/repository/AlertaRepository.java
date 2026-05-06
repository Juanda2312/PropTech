package co.edu.uniquindio.poo.PropTech.repository;

import co.edu.uniquindio.poo.PropTech.model.entity.Alerta;
import co.edu.uniquindio.poo.PropTech.model.enums.NivelAtencion;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import co.edu.uniquindio.poo.PropTech.structures.Queue;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AlertaRepository {

    private final HashTable<String, Alerta> tablaPorId  = new HashTable<>();

    // Cola FIFO de alertas pendientes de revisión
    private final Queue<Alerta> colaPendientes = new Queue<>();

    // ----------------------------------------------------------------
    // Escritura
    // ----------------------------------------------------------------

    public Alerta save(Alerta alerta) {
        tablaPorId.put(alerta.getIdAlerta(), alerta);
        if (!alerta.isCerrada()) {
            colaPendientes.enqueue(alerta);
        }
        return alerta;
    }

    public void delete(String id) {
        tablaPorId.remove(id);
    }

    // ----------------------------------------------------------------
    // Lectura
    // ----------------------------------------------------------------

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

    // ----------------------------------------------------------------
    // Cola de pendientes
    // ----------------------------------------------------------------

    public Optional<Alerta> pollPendiente() {
        if (colaPendientes.isEmpty()) return Optional.empty();
        return Optional.of(colaPendientes.dequeue());
    }

    public int sizePendientes() {
        return colaPendientes.getSize();
    }
}