package co.edu.uniquindio.poo.PropTech.repository;

import co.edu.uniquindio.poo.PropTech.model.entity.Operacion;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoOperacion;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class OperacionRepository {

    private final HashTable<String, Operacion> tablaPorId = new HashTable<>();

    // ----------------------------------------------------------------
    // Escritura
    // ----------------------------------------------------------------

    public Operacion save(Operacion operacion) {
        tablaPorId.put(operacion.getIdOperacion(), operacion);
        return operacion;
    }

    public void delete(String id) {
        tablaPorId.remove(id);
    }

    // ----------------------------------------------------------------
    // Lectura
    // ----------------------------------------------------------------

    public Optional<Operacion> findById(String id) {
        return Optional.ofNullable(tablaPorId.get(id));
    }

    public boolean existsById(String id) {
        return tablaPorId.containsKey(id);
    }

    public List<Operacion> findAll() {
        List<Operacion> todas = new ArrayList<>();
        for (Operacion op : tablaPorId) todas.add(op);
        return todas;
    }

    public List<Operacion> findByTipo(TipoOperacion tipo) {
        List<Operacion> resultado = new ArrayList<>();
        for (Operacion op : tablaPorId) {
            if (op.getTipoOperacion() == tipo) resultado.add(op);
        }
        return resultado;
    }

    public List<Operacion> findByCliente(String idCliente) {
        List<Operacion> resultado = new ArrayList<>();
        for (Operacion op : tablaPorId) {
            if (op.getCliente().getId().equals(idCliente)) resultado.add(op);
        }
        return resultado;
    }

    public List<Operacion> findByAsesor(String idAsesor) {
        List<Operacion> resultado = new ArrayList<>();
        for (Operacion op : tablaPorId) {
            if (op.getAsesor().getId().equals(idAsesor)) resultado.add(op);
        }
        return resultado;
    }

    public List<Operacion> findByInmueble(String codigoInmueble) {
        List<Operacion> resultado = new ArrayList<>();
        for (Operacion op : tablaPorId) {
            if (op.getInmueble().getCodigo().equals(codigoInmueble)) resultado.add(op);
        }
        return resultado;
    }
}