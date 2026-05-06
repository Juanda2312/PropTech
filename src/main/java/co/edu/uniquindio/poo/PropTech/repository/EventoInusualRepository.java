package co.edu.uniquindio.poo.PropTech.repository;

import co.edu.uniquindio.poo.PropTech.model.entity.EventoInusual;
import co.edu.uniquindio.poo.PropTech.model.enums.EstadoEvento;
import co.edu.uniquindio.poo.PropTech.model.enums.NivelAtencion;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EventoInusualRepository {

    private final HashTable<String, EventoInusual> tablaPorId = new HashTable<>();

    // ----------------------------------------------------------------
    // Escritura
    // ----------------------------------------------------------------

    public EventoInusual save(EventoInusual evento) {
        tablaPorId.put(evento.getIdEvento(), evento);
        return evento;
    }

    public void delete(String id) {
        tablaPorId.remove(id);
    }

    // ----------------------------------------------------------------
    // Lectura
    // ----------------------------------------------------------------

    public Optional<EventoInusual> findById(String id) {
        return Optional.ofNullable(tablaPorId.get(id));
    }

    public boolean existsById(String id) {
        return tablaPorId.containsKey(id);
    }

    public List<EventoInusual> findAll() {
        List<EventoInusual> todos = new ArrayList<>();
        for (EventoInusual e : tablaPorId) todos.add(e);
        return todos;
    }

    public List<EventoInusual> findByEstado(EstadoEvento estado) {
        List<EventoInusual> resultado = new ArrayList<>();
        for (EventoInusual e : tablaPorId) {
            if (e.getEstadoEvento() == estado) resultado.add(e);
        }
        return resultado;
    }

    public List<EventoInusual> findByNivel(NivelAtencion nivel) {
        List<EventoInusual> resultado = new ArrayList<>();
        for (EventoInusual e : tablaPorId) {
            if (e.getNivelAtencion() == nivel) resultado.add(e);
        }
        return resultado;
    }
}