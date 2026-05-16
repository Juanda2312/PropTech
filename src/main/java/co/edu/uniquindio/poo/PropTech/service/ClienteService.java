package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.exception.EntidadDuplicadaException;
import co.edu.uniquindio.poo.PropTech.exception.EntidadNoEncontradaException;
import co.edu.uniquindio.poo.PropTech.exception.ReglaNegocioException;
import co.edu.uniquindio.poo.PropTech.model.dto.ClienteDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Interaccion;
import co.edu.uniquindio.poo.PropTech.model.entity.Recomendacion;
import co.edu.uniquindio.poo.PropTech.model.enums.TipoInteraccion;
import co.edu.uniquindio.poo.PropTech.repository.ClienteRepository;
import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // ----------------------------------------------------------------
    // CRUD
    // ----------------------------------------------------------------

    public Cliente registrar(ClienteDTO dto) {
        validarId(dto.getId());
        if (clienteRepository.existsById(dto.getId())) {
            throw new EntidadDuplicadaException("Cliente", dto.getId());
        }
        return clienteRepository.save(mapearDesdeDTO(dto));
    }

    public Cliente buscarPorId(String id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Cliente", id));
    }

    public void actualizar(String id, ClienteDTO dto) {
        Cliente anterior    = buscarPorId(id);
        Cliente actualizado = mapearDesdeDTO(dto);
        actualizado.setId(id);
        actualizado.setInmueblesConsultados(anterior.getInmueblesConsultados());
        actualizado.setPropiedadesVisitadas(anterior.getPropiedadesVisitadas());
        actualizado.setInmueblesDescartados(anterior.getInmueblesDescartados());
        actualizado.setInmueblesGuardados(anterior.getInmueblesGuardados());
        actualizado.setInmueblesNegociados(anterior.getInmueblesNegociados());
        actualizado.setListaRecomendaciones(anterior.getListaRecomendaciones());
        actualizado.setHistorialInteracciones(anterior.getHistorialInteracciones());
        clienteRepository.update(anterior, actualizado);
    }

    public void eliminar(String id) {
        buscarPorId(id);
        clienteRepository.delete(id);
    }

    // ----------------------------------------------------------------
    // Interacciones (historial unificado)
    // ----------------------------------------------------------------

    public Interaccion registrarInteraccion(String idCliente, TipoInteraccion tipo,
                                            Inmueble inmueble, String detalle) {
        Cliente cliente = buscarPorId(idCliente);
        Interaccion interaccion = new Interaccion(
                UUID.randomUUID().toString(),
                LocalDate.now(),
                tipo,
                detalle,
                inmueble
        );
        // Insertar al frente: la más reciente queda primera
        cliente.getHistorialInteracciones().addFirst(interaccion);
        return interaccion;
    }

    public List<Interaccion> obtenerHistorial(String idCliente) {
        Cliente cliente = buscarPorId(idCliente);
        List<Interaccion> lista = new ArrayList<>();
        for (Interaccion i : cliente.getHistorialInteracciones()) lista.add(i);
        return lista;
    }

    public List<Interaccion> obtenerHistorialPorTipo(String idCliente, TipoInteraccion tipo) {
        Cliente cliente = buscarPorId(idCliente);
        List<Interaccion> resultado = new ArrayList<>();
        for (Interaccion i : cliente.getHistorialInteracciones()) {
            if (i.getTipoInteraccion() == tipo) resultado.add(i);
        }
        return resultado;
    }

    // ----------------------------------------------------------------
    // Historial de interacción (listas específicas)
    // ----------------------------------------------------------------

    public void registrarInmuebleConsultado(String idCliente, Inmueble inmueble) {
        Cliente cliente = buscarPorId(idCliente);
        cliente.getInmueblesConsultados().addLast(inmueble);
        registrarInteraccion(idCliente, TipoInteraccion.INMUEBLE_CONSULTADO, inmueble,
                "Consulta de " + inmueble.getDireccion() + ", " + inmueble.getCiudad());
    }

    public void registrarPropiedadVisitada(String idCliente, Inmueble inmueble) {
        Cliente cliente = buscarPorId(idCliente);
        cliente.getPropiedadesVisitadas().addLast(inmueble);
    }

    public void registrarInmuebleDescartado(String idCliente, Inmueble inmueble) {
        Cliente cliente = buscarPorId(idCliente);
        cliente.getInmueblesDescartados().addLast(inmueble);
        registrarInteraccion(idCliente, TipoInteraccion.INMUEBLE_DESCARTADO, inmueble,
                "Inmueble descartado: " + inmueble.getDireccion());
    }

    public void marcarFavorito(String idCliente, Inmueble inmueble) {
        Cliente cliente = buscarPorId(idCliente);
        for (Inmueble fav : cliente.getInmueblesGuardados()) {
            if (fav.getCodigo().equals(inmueble.getCodigo())) {
                throw new ReglaNegocioException(
                        "El inmueble " + inmueble.getCodigo() + " ya está en favoritos.");
            }
        }
        cliente.getInmueblesGuardados().addLast(inmueble);
        registrarInteraccion(idCliente, TipoInteraccion.FAVORITO_GUARDADO, inmueble,
                "Guardado como favorito: " + inmueble.getDireccion() + ", " + inmueble.getCiudad());
    }

    public void eliminarFavorito(String idCliente, String codigoInmueble) {
        Cliente cliente = buscarPorId(idCliente);
        SimpleLinkedList<Inmueble> lista = cliente.getInmueblesGuardados();
        int index = -1, i = 0;
        for (Inmueble fav : lista) {
            if (fav.getCodigo().equals(codigoInmueble)) { index = i; break; }
            i++;
        }
        if (index == -1) throw new EntidadNoEncontradaException("Favorito", codigoInmueble);
        lista.remove(index);
    }

    public void registrarInmuebleNegociado(String idCliente, Inmueble inmueble) {
        buscarPorId(idCliente).getInmueblesNegociados().addLast(inmueble);
    }

    public void agregarRecomendacion(String idCliente, Recomendacion recomendacion) {
        buscarPorId(idCliente).getListaRecomendaciones().addLast(recomendacion);
    }

    // ----------------------------------------------------------------
    // Consultas
    // ----------------------------------------------------------------

    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    public List<Cliente> obtenerOrdenadosPorPresupuesto() {
        return clienteRepository.findAllOrdenadosPorPresupuesto();
    }

    public List<Cliente> obtenerPorPresupuestoMaximo(double max) {
        return clienteRepository.findByPresupuestoMaximo(max);
    }

    // ----------------------------------------------------------------
    // Validación de ID
    // ----------------------------------------------------------------

    private void validarId(String id) {
        if (id == null || id.isBlank()) {
            throw new ReglaNegocioException("El ID del cliente no puede estar vacío.");
        }
        if (!id.matches("\\d{10}")) {
            throw new ReglaNegocioException(
                    "El ID del cliente debe ser una cédula de exactamente 10 dígitos numéricos. Valor recibido: '" + id + "'.");
        }
    }

    private Cliente mapearDesdeDTO(ClienteDTO dto) {
        return new Cliente(
                dto.getId(), dto.getNombre(), dto.getCorreo(), dto.getTelefono(),
                dto.getTipoCliente(), dto.getPresupuesto(), dto.getZonasInteres(),
                dto.getTipoInmuebleDeseado(), dto.getHabitacionesMinimas(),
                dto.getEstadoBusqueda()
        );
    }
}