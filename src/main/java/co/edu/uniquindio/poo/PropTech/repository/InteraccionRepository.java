package co.edu.uniquindio.poo.PropTech.repository;

import co.edu.uniquindio.poo.PropTech.model.entity.Interaccion;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInteraccion;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de interacciones.
 *
 * Estructuras propias:
 * - HashTable<String, Interaccion>  : acceso O(1) por ID de interacción
 * - HashTable<String, SimpleLinkedList<Interaccion>> : historial por cliente
 *   (clave = idCliente, valor = lista enlazada de sus interacciones)
 */
@Repository
public class InteraccionRepository {

    /** Acceso O(1) por ID de interacción */
    private final HashTable<String, Interaccion> tablaPorId = new HashTable<>();

    /**
     * Historial por cliente: cada entrada es una SimpleLinkedList
     * de interacciones de ese cliente, ordenadas por fecha (más reciente primero).
     */
    private final HashTable<String, SimpleLinkedList<Interaccion>> historialPorCliente = new HashTable<>();

    // ----------------------------------------------------------------
    // Escritura
    // ----------------------------------------------------------------

    public Interaccion save(Interaccion interaccion) {
        tablaPorId.put(interaccion.getIdInteraccion(), interaccion);

        String idCliente = interaccion.getCliente().getId();
        SimpleLinkedList<Interaccion> historial = historialPorCliente.get(idCliente);
        if (historial == null) {
            historial = new SimpleLinkedList<>();
            historialPorCliente.put(idCliente, historial);
        }
        // Insertar al inicio para que la más reciente quede primero
        historial.addFirst(interaccion);

        return interaccion;
    }

    public void delete(String id) {
        Interaccion interaccion = tablaPorId.get(id);
        if (interaccion == null) return;
        tablaPorId.remove(id);

        String idCliente = interaccion.getCliente().getId();
        SimpleLinkedList<Interaccion> historial = historialPorCliente.get(idCliente);
        if (historial != null) {
            int idx = historial.getIndex(interaccion);
            if (idx >= 0) historial.remove(idx);
        }
    }

    // ----------------------------------------------------------------
    // Lectura
    // ----------------------------------------------------------------

    public Optional<Interaccion> findById(String id) {
        return Optional.ofNullable(tablaPorId.get(id));
    }

    public boolean existsById(String id) {
        return tablaPorId.containsKey(id);
    }

    /** Todas las interacciones del sistema */
    public List<Interaccion> findAll() {
        List<Interaccion> todas = new ArrayList<>();
        for (Interaccion i : tablaPorId) todas.add(i);
        return todas;
    }

    /**
     * Historial completo de un cliente, más reciente primero.
     * Usa la SimpleLinkedList propia del cliente.
     */
    public SimpleLinkedList<Interaccion> findByCliente(String idCliente) {
        SimpleLinkedList<Interaccion> historial = historialPorCliente.get(idCliente);
        return historial != null ? historial : new SimpleLinkedList<>();
    }

    /** Interacciones de un cliente filtradas por tipo */
    public List<Interaccion> findByClienteYTipo(String idCliente, TipoInteraccion tipo) {
        List<Interaccion> resultado = new ArrayList<>();
        SimpleLinkedList<Interaccion> historial = findByCliente(idCliente);
        for (Interaccion i : historial) {
            if (i.getTipoInteraccion() == tipo) resultado.add(i);
        }
        return resultado;
    }

    /** Interacciones de un cliente sobre un inmueble específico */
    public List<Interaccion> findByClienteYInmueble(String idCliente, String codigoInmueble) {
        List<Interaccion> resultado = new ArrayList<>();
        SimpleLinkedList<Interaccion> historial = findByCliente(idCliente);
        for (Interaccion i : historial) {
            if (i.getInmueble() != null
                    && i.getInmueble().getCodigo().equals(codigoInmueble)) {
                resultado.add(i);
            }
        }
        return resultado;
    }

    /** Total de interacciones de un cliente */
    public int countByCliente(String idCliente) {
        SimpleLinkedList<Interaccion> historial = historialPorCliente.get(idCliente);
        return historial != null ? historial.getSize() : 0;
    }

    /** Todos los clientes que tienen al menos una interacción registrada */
    public List<String> findClientesConInteracciones() {
        List<String> ids = new ArrayList<>();
        for (SimpleLinkedList<Interaccion> lista : historialPorCliente) {
            if (!lista.isEmpty()) ids.add(lista.getfirst().getCliente().getId());
        }
        return ids;
    }
}