package co.edu.uniquindio.poo.PropTech.repository;

import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.FinalidadInmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInmueble;
import co.edu.uniquindio.poo.PropTech.structures.AVLTree;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import co.edu.uniquindio.poo.PropTech.structures.Stack;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InmuebleRepository {

    // Acceso O(1) por código
    private final HashTable<String, Inmueble> tablaPorCodigo = new HashTable<>();

    // Ordenamiento por precio para rangos y reportes
    @Getter
    private final AVLTree<Inmueble> arbolPorPrecio = new AVLTree<>();

    // Historial de snapshots para deshacer cambios
    private final Stack<Inmueble> historialCambios = new Stack<>();

    // ----------------------------------------------------------------
    // Escritura
    // ----------------------------------------------------------------

    public Inmueble save(Inmueble inmueble) {
        tablaPorCodigo.put(inmueble.getCodigo(), inmueble);
        arbolPorPrecio.insert(inmueble);
        return inmueble;
    }

    public void update(Inmueble snapshot, Inmueble actualizado) {
        // Antes de actualizar guardamos el snapshot para poder deshacer
        historialCambios.push(snapshot);
        // El precio puede haber cambiado, así que lo sacamos y reinsertamos en el árbol
        arbolPorPrecio.remove(snapshot);
        arbolPorPrecio.insert(actualizado);
        tablaPorCodigo.put(actualizado.getCodigo(), actualizado);
    }

    public void delete(String codigo) {
        Inmueble inmueble = tablaPorCodigo.get(codigo);
        if (inmueble != null) {
            tablaPorCodigo.remove(codigo);
            arbolPorPrecio.remove(inmueble);
        }
    }

    // ----------------------------------------------------------------
    // Lectura
    // ----------------------------------------------------------------

    public Optional<Inmueble> findById(String codigo) {
        return Optional.ofNullable(tablaPorCodigo.get(codigo));
    }

    public boolean existsById(String codigo) {
        return tablaPorCodigo.containsKey(codigo);
    }

    public List<Inmueble> findAll() {
        List<Inmueble> todos = new ArrayList<>();
        recolectarInOrder(arbolPorPrecio.getRoot(), todos);
        return todos;
    }

    public List<Inmueble> findByTipo(TipoInmueble tipo) {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : tablaPorCodigo) {
            if (i.getTipoInmueble() == tipo) resultado.add(i);
        }
        return resultado;
    }

    public List<Inmueble> findByFinalidad(FinalidadInmueble finalidad) {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : tablaPorCodigo) {
            if (i.getFinalidad() == finalidad) resultado.add(i);
        }
        return resultado;
    }

    public List<Inmueble> findByCiudad(String ciudad) {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : tablaPorCodigo) {
            if (i.getCiudad().equalsIgnoreCase(ciudad)) resultado.add(i);
        }
        return resultado;
    }

    public List<Inmueble> findByDisponibilidad(boolean disponible) {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : tablaPorCodigo) {
            if (i.isDisponibilidad() == disponible) resultado.add(i);
        }
        return resultado;
    }

    public List<Inmueble> findByRangoPrecio(double min, double max) {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : tablaPorCodigo) {
            if (i.getPrecio() >= min && i.getPrecio() <= max) resultado.add(i);
        }
        return resultado;
    }

    public List<Inmueble> findByCombinado(TipoInmueble tipo, FinalidadInmueble finalidad,
                                          String ciudad, double precioMax, int habitacionesMin) {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : tablaPorCodigo) {
            if (tipo != null && i.getTipoInmueble() != tipo) continue;
            if (finalidad != null && i.getFinalidad() != finalidad) continue;
            if (ciudad != null && !i.getCiudad().equalsIgnoreCase(ciudad)) continue;
            if (precioMax > 0 && i.getPrecio() > precioMax) continue;
            if (habitacionesMin > 0 && i.getHabitaciones() < habitacionesMin) continue;
            resultado.add(i);
        }
        return resultado;
    }

    // ----------------------------------------------------------------
    // Historial (Stack)
    // ----------------------------------------------------------------

    public Optional<Inmueble> popSnapshot() {
        if (historialCambios.isEmpty()) return Optional.empty();
        return Optional.of(historialCambios.pop());
    }

    public boolean hasSnapshots() {
        return !historialCambios.isEmpty();
    }

    // ----------------------------------------------------------------
    // Helper de recorrido inOrder del AVL
    // ----------------------------------------------------------------

    private void recolectarInOrder(AVLTree.AVLNode<Inmueble> nodo, List<Inmueble> lista) {
        if (nodo == null) return;
        recolectarInOrder(nodo.getLeft(), lista);
        lista.add(nodo.getData());
        recolectarInOrder(nodo.getRight(), lista);
    }
}