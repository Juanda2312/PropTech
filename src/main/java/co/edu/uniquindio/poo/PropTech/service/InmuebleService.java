package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.exception.EntidadDuplicadaException;
import co.edu.uniquindio.poo.PropTech.exception.EntidadNoEncontradaException;
import co.edu.uniquindio.poo.PropTech.exception.ReglaNegocioException;
import co.edu.uniquindio.poo.PropTech.model.dto.InmuebleDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.FinalidadInmueble;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInmueble;
import co.edu.uniquindio.poo.PropTech.repository.InmuebleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InmuebleService {

    private final InmuebleRepository inmuebleRepository;

    public InmuebleService(InmuebleRepository inmuebleRepository) {
        this.inmuebleRepository = inmuebleRepository;
    }

    // ----------------------------------------------------------------
    // CRUD
    // ----------------------------------------------------------------

    public Inmueble registrar(InmuebleDTO dto, Asesor asesor) {
        if (inmuebleRepository.existsById(dto.getCodigo())) {
            throw new EntidadDuplicadaException("Inmueble", dto.getCodigo());
        }
        return inmuebleRepository.save(mapearDesdeDTO(dto, asesor));
    }

    public Inmueble buscarPorCodigo(String codigo) {
        return inmuebleRepository.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("Inmueble", codigo));
    }

    public void actualizar(String codigo, InmuebleDTO dto, Asesor asesor) {
        Inmueble existente = buscarPorCodigo(codigo);
        Inmueble snapshot  = copiarSnapshot(existente);
        aplicarCambios(existente, dto, asesor);
        inmuebleRepository.update(snapshot, existente);
    }

    public void eliminar(String codigo) {
        buscarPorCodigo(codigo);
        inmuebleRepository.delete(codigo);
    }

    public void deshacerUltimoCambio() {
        Inmueble snapshot = inmuebleRepository.popSnapshot()
                .orElseThrow(() -> new ReglaNegocioException(
                        "No hay cambios pendientes para deshacer en el historial de inmuebles."));
        Inmueble actual = buscarPorCodigo(snapshot.getCodigo());
        aplicarCambios(actual, snapshot);
        inmuebleRepository.update(actual, actual);
    }

    // ----------------------------------------------------------------
    // Consultas y filtros
    // ----------------------------------------------------------------

    public List<Inmueble> obtenerTodos() {
        return inmuebleRepository.findAll();
    }

    public List<Inmueble> filtrarDisponibles() {
        return inmuebleRepository.findByDisponibilidad(true);
    }

    public List<Inmueble> filtrarPorTipo(TipoInmueble tipo) {
        return inmuebleRepository.findByTipo(tipo);
    }

    public List<Inmueble> filtrarPorFinalidad(FinalidadInmueble finalidad) {
        return inmuebleRepository.findByFinalidad(finalidad);
    }

    public List<Inmueble> filtrarPorCiudad(String ciudad) {
        return inmuebleRepository.findByCiudad(ciudad);
    }

    public List<Inmueble> filtrarPorRangoPrecio(double min, double max) {
        return inmuebleRepository.findByRangoPrecio(min, max);
    }

    public List<Inmueble> filtrarCombinado(TipoInmueble tipo, FinalidadInmueble finalidad,
                                           String ciudad, double precioMax, int habitacionesMin) {
        return inmuebleRepository.findByCombinado(tipo, finalidad, ciudad, precioMax, habitacionesMin);
    }

    // ----------------------------------------------------------------
    // Helpers privados
    // ----------------------------------------------------------------

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

    private void aplicarCambios(Inmueble destino, InmuebleDTO dto, Asesor asesor) {
        destino.setDireccion(dto.getDireccion());
        destino.setCiudad(dto.getCiudad());
        destino.setBarrio(dto.getBarrio());
        destino.setTipoInmueble(dto.getTipoInmueble());
        destino.setFinalidad(dto.getFinalidad());
        destino.setPrecio(dto.getPrecio());
        destino.setArea(dto.getArea());
        destino.setHabitaciones(dto.getHabitaciones());
        destino.setBanos(dto.getBanos());
        destino.setEstado(dto.getEstado());
        destino.setDisponibilidad(dto.isDisponibilidad());
        destino.setAsesor(asesor);
    }

    private void aplicarCambios(Inmueble destino, Inmueble origen) {
        destino.setDireccion(origen.getDireccion());
        destino.setCiudad(origen.getCiudad());
        destino.setBarrio(origen.getBarrio());
        destino.setTipoInmueble(origen.getTipoInmueble());
        destino.setFinalidad(origen.getFinalidad());
        destino.setPrecio(origen.getPrecio());
        destino.setArea(origen.getArea());
        destino.setHabitaciones(origen.getHabitaciones());
        destino.setBanos(origen.getBanos());
        destino.setEstado(origen.getEstado());
        destino.setDisponibilidad(origen.isDisponibilidad());
        destino.setAsesor(origen.getAsesor());
    }

    private Inmueble copiarSnapshot(Inmueble original) {
        Inmueble snap = new Inmueble();
        snap.setCodigo(original.getCodigo());
        aplicarCambios(snap, original);
        return snap;
    }
}