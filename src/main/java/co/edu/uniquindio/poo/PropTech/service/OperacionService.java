package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.dto.OperacionDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Operacion;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoOperacion;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OperacionService {

    private final HashTable<String, Operacion>  tablaPorId   = new HashTable<>();

    // ----------------------------------------------------------------
    // Registro
    // ----------------------------------------------------------------

    public Operacion registrar(OperacionDTO dto, Inmueble inmueble, Cliente cliente, Asesor asesor) {
        if (tablaPorId.containsKey(dto.getIdOperacion())) {
            throw new RuntimeException("Ya existe una operación con id: " + dto.getIdOperacion());
        }

        Operacion operacion = new Operacion(
                dto.getIdOperacion(), inmueble, cliente, asesor, dto.getFecha(),
                dto.getTipoOperacion(), dto.getValorAcordado(), dto.getComision(),
                dto.getEstadoProceso()
        );

        tablaPorId.put(operacion.getIdOperacion(), operacion);

        // Si es venta o arriendo cerrado, el inmueble deja de estar disponible
        if (dto.getTipoOperacion() == TipoOperacion.VENTA ||
                dto.getTipoOperacion() == TipoOperacion.ARRIENDO) {
            inmueble.setDisponibilidad(false);
        }

        return operacion;
    }

    public void cancelar(String idOperacion) {
        Operacion operacion = buscarPorId(idOperacion);
        operacion.setEstadoProceso("CANCELADO");
        // El inmueble vuelve a estar disponible
        operacion.getInmueble().setDisponibilidad(true);
    }

    public void cerrar(String idOperacion) {
        buscarPorId(idOperacion).setEstadoProceso("CERRADO");
    }

    // ----------------------------------------------------------------
    // Consultas
    // ----------------------------------------------------------------

    public Operacion buscarPorId(String id) {
        Operacion op = tablaPorId.get(id);
        if (op == null) throw new RuntimeException("Operación no encontrada: " + id);
        return op;
    }

    public List<Operacion> obtenerPorTipo(TipoOperacion tipo) {
        List<Operacion> resultado = new ArrayList<>();
        for (Operacion op : tablaPorId) {
            if (op.getTipoOperacion() == tipo) resultado.add(op);
        }
        return resultado;
    }

    public List<Operacion> obtenerPorCliente(String idCliente) {
        List<Operacion> resultado = new ArrayList<>();
        for (Operacion op : tablaPorId) {
            if (op.getCliente().getId().equals(idCliente)) resultado.add(op);
        }
        return resultado;
    }

    public List<Operacion> obtenerPorAsesor(String idAsesor) {
        List<Operacion> resultado = new ArrayList<>();
        for (Operacion op : tablaPorId) {
            if (op.getAsesor().getId().equals(idAsesor)) resultado.add(op);
        }
        return resultado;
    }

    public List<Operacion> obtenerTodas() {
        List<Operacion> todas = new ArrayList<>();
        for (Operacion op : tablaPorId) todas.add(op);
        return todas;
    }

    public double calcularTotalComisiones(String idAsesor) {
        return obtenerPorAsesor(idAsesor).stream()
                .mapToDouble(Operacion::calcularComision)
                .sum();
    }
}