package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.exception.EntidadDuplicadaException;
import co.edu.uniquindio.poo.PropTech.exception.EntidadNoEncontradaException;
import co.edu.uniquindio.poo.PropTech.exception.ReglaNegocioException;
import co.edu.uniquindio.poo.PropTech.model.dto.ClienteDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Recomendacion;
import co.edu.uniquindio.poo.PropTech.repository.ClienteRepository;
import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import org.springframework.stereotype.Service;

import java.util.List;

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
        clienteRepository.update(anterior, actualizado);
    }

    public void eliminar(String id) {
        buscarPorId(id);
        clienteRepository.delete(id);
    }

    // ----------------------------------------------------------------
    // Historial de interacción
    // ----------------------------------------------------------------

    public void registrarInmuebleConsultado(String idCliente, Inmueble inmueble) {
        buscarPorId(idCliente).getInmueblesConsultados().addLast(inmueble);
    }

    public void registrarPropiedadVisitada(String idCliente, Inmueble inmueble) {
        buscarPorId(idCliente).getPropiedadesVisitadas().addLast(inmueble);
    }

    public void registrarInmuebleDescartado(String idCliente, Inmueble inmueble) {
        buscarPorId(idCliente).getInmueblesDescartados().addLast(inmueble);
    }

    public void marcarFavorito(String idCliente, Inmueble inmueble) {
        Cliente cliente = buscarPorId(idCliente);
        // Verificar duplicado
        for (Inmueble fav : cliente.getInmueblesGuardados()) {
            if (fav.getCodigo().equals(inmueble.getCodigo())) {
                throw new ReglaNegocioException(
                        "El inmueble " + inmueble.getCodigo() + " ya está en favoritos.");
            }
        }
        cliente.getInmueblesGuardados().addLast(inmueble);
    }

    public void eliminarFavorito(String idCliente, String codigoInmueble) {
        Cliente cliente = buscarPorId(idCliente);
        SimpleLinkedList<Inmueble> lista = cliente.getInmueblesGuardados();
        int index = -1;
        int i = 0;
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
    // Validación de ID (cédula colombiana: exactamente 10 dígitos)
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

    // ----------------------------------------------------------------
    // Helper privado
    // ----------------------------------------------------------------

    private Cliente mapearDesdeDTO(ClienteDTO dto) {
        return new Cliente(
                dto.getId(), dto.getNombre(), dto.getCorreo(), dto.getTelefono(),
                dto.getTipoCliente(), dto.getPresupuesto(), dto.getZonasInteres(),
                dto.getTipoInmuebleDeseado(), dto.getHabitacionesMinimas(),
                dto.getEstadoBusqueda()
        );
    }
}