package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.dto.OperacionDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Operacion;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoOperacion;
import co.edu.uniquindio.poo.PropTech.repository.OperacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperacionService {

    private final OperacionRepository operacionRepository;

    public OperacionService(OperacionRepository operacionRepository) {
        this.operacionRepository = operacionRepository;
    }

    // ----------------------------------------------------------------
    // Registro
    // ----------------------------------------------------------------

    public Operacion registrar(OperacionDTO dto, Inmueble inmueble,
                               Cliente cliente, Asesor asesor) {
        if (operacionRepository.existsById(dto.getIdOperacion())) {
            throw new RuntimeException("Ya existe una operación con id: " + dto.getIdOperacion());
        }

        Operacion operacion = new Operacion(
                dto.getIdOperacion(), inmueble, cliente, asesor,
                dto.getFecha(), dto.getTipoOperacion(),
                dto.getValorAcordado(), dto.getComision(), dto.getEstadoProceso()
        );

        // El inmueble deja de estar disponible al registrar venta o arriendo
        if (dto.getTipoOperacion() == TipoOperacion.VENTA
                || dto.getTipoOperacion() == TipoOperacion.ARRIENDO) {
            inmueble.setDisponibilidad(false);
        }

        return operacionRepository.save(operacion);
    }

    public void cancelar(String idOperacion) {
        Operacion operacion = buscarPorId(idOperacion);
        operacion.setEstadoProceso("CANCELADO");
        operacion.getInmueble().setDisponibilidad(true);
    }

    public void cerrar(String idOperacion) {
        buscarPorId(idOperacion).setEstadoProceso("CERRADO");
    }

    // ----------------------------------------------------------------
    // Consultas
    // ----------------------------------------------------------------

    public Operacion buscarPorId(String id) {
        return operacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Operación no encontrada: " + id));
    }

    public List<Operacion> obtenerTodas() {
        return operacionRepository.findAll();
    }

    public List<Operacion> obtenerPorTipo(TipoOperacion tipo) {
        return operacionRepository.findByTipo(tipo);
    }

    public List<Operacion> obtenerPorCliente(String idCliente) {
        return operacionRepository.findByCliente(idCliente);
    }

    public List<Operacion> obtenerPorAsesor(String idAsesor) {
        return operacionRepository.findByAsesor(idAsesor);
    }

    public double calcularTotalComisiones(String idAsesor) {
        return obtenerPorAsesor(idAsesor).stream()
                .mapToDouble(Operacion::calcularComision)
                .sum();
    }
}