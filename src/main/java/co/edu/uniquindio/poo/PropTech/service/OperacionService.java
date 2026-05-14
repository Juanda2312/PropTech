package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.exception.EntidadDuplicadaException;
import co.edu.uniquindio.poo.PropTech.exception.EntidadNoEncontradaException;
import co.edu.uniquindio.poo.PropTech.exception.EstadoInvalidoException;
import co.edu.uniquindio.poo.PropTech.exception.ReglaNegocioException;
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
            throw new EntidadDuplicadaException("Operacion", dto.getIdOperacion());
        }
        if (dto.getValorAcordado() <= 0) {
            throw new ReglaNegocioException(
                    "El valor acordado de la operación debe ser mayor a cero.");
        }

        Operacion operacion = new Operacion(
                dto.getIdOperacion(), inmueble, cliente, asesor,
                dto.getFecha(), dto.getTipoOperacion(),
                dto.getValorAcordado(), dto.getComision(), dto.getEstadoProceso(),null
        );

        if (dto.getTipoOperacion() == TipoOperacion.VENTA
                || dto.getTipoOperacion() == TipoOperacion.ARRIENDO) {
            inmueble.setDisponibilidad(false);
        }

        return operacionRepository.save(operacion);
    }

    public void cancelar(String idOperacion) {
        Operacion operacion = buscarPorId(idOperacion);
        if ("CERRADO".equals(operacion.getEstadoProceso())) {
            throw new EstadoInvalidoException("Operacion", operacion.getEstadoProceso(), "CANCELAR");
        }
        if ("CANCELADO".equals(operacion.getEstadoProceso())) {
            throw new EstadoInvalidoException("Operacion", operacion.getEstadoProceso(), "CANCELAR");
        }
        operacion.setEstadoProceso("CANCELADO");
        operacion.getInmueble().setDisponibilidad(true);
    }

    public void cerrar(String idOperacion) {
        Operacion operacion = buscarPorId(idOperacion);
        if ("CERRADO".equals(operacion.getEstadoProceso())) {
            throw new EstadoInvalidoException("Operacion", operacion.getEstadoProceso(), "CERRAR");
        }
        if ("CANCELADO".equals(operacion.getEstadoProceso())) {
            throw new EstadoInvalidoException("Operacion", operacion.getEstadoProceso(), "CERRAR");
        }
        operacion.setEstadoProceso("CERRADO");
    }

    // ----------------------------------------------------------------
    // Consultas
    // ----------------------------------------------------------------

    public Operacion buscarPorId(String id) {
        return operacionRepository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Operacion", id));
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