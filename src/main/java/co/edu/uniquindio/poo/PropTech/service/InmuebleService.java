package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.dto.InmuebleDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.FinalidadInmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInmueble;
import co.edu.uniquindio.poo.PropTech.structures.AVLTree;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import co.edu.uniquindio.poo.PropTech.structures.Stack;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InmuebleService {

    // Acceso O(1) por código
    private final HashTable<String, Inmueble> tablaPorCodigo = new HashTable<>();

    // Ordenamiento y búsqueda por rango de precio
    @Getter
    private final AVLTree<Inmueble> arbolPorPrecio = new AVLTree<>();

    // Historial de cambios para deshacer (pila por inmueble sería ideal,
    // pero usamos una pila global de snapshots simplificada)
    private final Stack<Inmueble> historialCambios = new Stack<>();

    // ----------------------------------------------------------------
    // CRUD
    // ----------------------------------------------------------------

    public Inmueble registrar(InmuebleDTO dto, Asesor asesor) {
        if (tablaPorCodigo.containsKey(dto.getCodigo())) {
            throw new RuntimeException("Ya existe un inmueble con el código: " + dto.getCodigo());
        }

        Inmueble inmueble = mapearDesdeDTO(dto, asesor);
        tablaPorCodigo.put(inmueble.getCodigo(), inmueble);
        arbolPorPrecio.insert(inmueble);
        return inmueble;
    }

    public Inmueble buscarPorCodigo(String codigo) {
        Inmueble inmueble = tablaPorCodigo.get(codigo);
        if (inmueble == null) {
            throw new RuntimeException("Inmueble no encontrado: " + codigo);
        }
        return inmueble;
    }

    public void actualizar(String codigo, InmuebleDTO dto, Asesor asesor) {
        Inmueble existente = buscarPorCodigo(codigo);

        // Guardamos snapshot antes de modificar para poder deshacer
        historialCambios.push(copiarSnapshot(existente));

        // Sacamos del árbol antes de modificar el precio (la clave de ordenamiento)
        arbolPorPrecio.remove(existente);

        existente.setDireccion(dto.getDireccion());
        existente.setCiudad(dto.getCiudad());
        existente.setBarrio(dto.getBarrio());
        existente.setTipoInmueble(dto.getTipoInmueble());
        existente.setFinalidad(dto.getFinalidad());
        existente.setPrecio(dto.getPrecio());
        existente.setArea(dto.getArea());
        existente.setHabitaciones(dto.getHabitaciones());
        existente.setBanos(dto.getBanos());
        existente.setEstado(dto.getEstado());
        existente.setDisponibilidad(dto.isDisponibilidad());
        existente.setAsesor(asesor);

        arbolPorPrecio.insert(existente);
    }

    public void eliminar(String codigo) {
        Inmueble inmueble = buscarPorCodigo(codigo);
        tablaPorCodigo.remove(codigo);
        arbolPorPrecio.remove(inmueble);
    }

    // ----------------------------------------------------------------
    // Deshacer último cambio (Stack)
    // ----------------------------------------------------------------

    public void deshacerUltimoCambio() {
        if (historialCambios.isEmpty()) {
            throw new RuntimeException("No hay cambios que deshacer");
        }

        Inmueble snapshot = historialCambios.pop();
        Inmueble actual = buscarPorCodigo(snapshot.getCodigo());

        arbolPorPrecio.remove(actual);

        actual.setDireccion(snapshot.getDireccion());
        actual.setCiudad(snapshot.getCiudad());
        actual.setBarrio(snapshot.getBarrio());
        actual.setTipoInmueble(snapshot.getTipoInmueble());
        actual.setFinalidad(snapshot.getFinalidad());
        actual.setPrecio(snapshot.getPrecio());
        actual.setArea(snapshot.getArea());
        actual.setHabitaciones(snapshot.getHabitaciones());
        actual.setBanos(snapshot.getBanos());
        actual.setEstado(snapshot.getEstado());
        actual.setDisponibilidad(snapshot.isDisponibilidad());
        actual.setAsesor(snapshot.getAsesor());

        arbolPorPrecio.insert(actual);
    }

    // ----------------------------------------------------------------
    // Filtros
    // ----------------------------------------------------------------

    public List<Inmueble> filtrarPorTipo(TipoInmueble tipo) {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : obtenerTodos()) {
            if (i.getTipoInmueble() == tipo) resultado.add(i);
        }
        return resultado;
    }

    public List<Inmueble> filtrarPorFinalidad(FinalidadInmueble finalidad) {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : obtenerTodos()) {
            if (i.getFinalidad() == finalidad) resultado.add(i);
        }
        return resultado;
    }

    public List<Inmueble> filtrarPorCiudad(String ciudad) {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : obtenerTodos()) {
            if (i.getCiudad().equalsIgnoreCase(ciudad)) resultado.add(i);
        }
        return resultado;
    }

    public List<Inmueble> filtrarDisponibles() {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : obtenerTodos()) {
            if (i.isDisponibilidad()) resultado.add(i);
        }
        return resultado;
    }

    public List<Inmueble> filtrarPorRangoPrecio(double min, double max) {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : obtenerTodos()) {
            if (i.getPrecio() >= min && i.getPrecio() <= max) resultado.add(i);
        }
        return resultado;
    }

    // Filtro combinado general
    public List<Inmueble> filtrarCombinado(TipoInmueble tipo, FinalidadInmueble finalidad,
                                           String ciudad, double precioMax, int habitacionesMin) {
        List<Inmueble> resultado = new ArrayList<>();
        for (Inmueble i : obtenerTodos()) {
            boolean cumple = true;
            if (tipo != null && i.getTipoInmueble() != tipo) cumple = false;
            if (finalidad != null && i.getFinalidad() != finalidad) cumple = false;
            if (ciudad != null && !i.getCiudad().equalsIgnoreCase(ciudad)) cumple = false;
            if (precioMax > 0 && i.getPrecio() > precioMax) cumple = false;
            if (habitacionesMin > 0 && i.getHabitaciones() < habitacionesMin) cumple = false;
            if (cumple) resultado.add(i);
        }
        return resultado;
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    public List<Inmueble> obtenerTodos() {
        // Recorremos la HashTable a través del árbol (que tiene todos los inmuebles)
        // Usamos una lista temporal de Java para retornar la colección
        List<Inmueble> todos = new ArrayList<>();
        recolectarInOrder(arbolPorPrecio.getRoot(), todos);
        return todos;
    }

    private void recolectarInOrder(AVLTree.AVLNode<Inmueble> nodo, List<Inmueble> lista) {
        if (nodo == null) return;
        recolectarInOrder(nodo.getLeft(), lista);
        lista.add(nodo.getData());
        recolectarInOrder(nodo.getRight(), lista);
    }

    private Inmueble mapearDesdeDTO(InmuebleDTO dto, Asesor asesor) {
        Inmueble i = new Inmueble();
        i.setCodigo(dto.getCodigo());
        i.setDireccion(dto.getDireccion());
        i.setCiudad(dto.getCiudad());
        i.setBarrio(dto.getBarrio());
        i.setTipoInmueble(dto.getTipoInmueble());
        i.setFinalidad(dto.getFinalidad());
        i.setPrecio(dto.getPrecio());
        i.setArea(dto.getArea());
        i.setHabitaciones(dto.getHabitaciones());
        i.setBanos(dto.getBanos());
        i.setEstado(dto.getEstado());
        i.setDisponibilidad(dto.isDisponibilidad());
        i.setAsesor(asesor);
        return i;
    }

    // Copia superficial para snapshot (no copiamos la listaVisitas)
    private Inmueble copiarSnapshot(Inmueble original) {
        Inmueble snap = new Inmueble();
        snap.setCodigo(original.getCodigo());
        snap.setDireccion(original.getDireccion());
        snap.setCiudad(original.getCiudad());
        snap.setBarrio(original.getBarrio());
        snap.setTipoInmueble(original.getTipoInmueble());
        snap.setFinalidad(original.getFinalidad());
        snap.setPrecio(original.getPrecio());
        snap.setArea(original.getArea());
        snap.setHabitaciones(original.getHabitaciones());
        snap.setBanos(original.getBanos());
        snap.setEstado(original.getEstado());
        snap.setDisponibilidad(original.isDisponibilidad());
        snap.setAsesor(original.getAsesor());
        return snap;
    }
}