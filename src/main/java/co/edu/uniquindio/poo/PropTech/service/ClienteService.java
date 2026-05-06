package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.dto.ClienteDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Recomendacion;
import co.edu.uniquindio.poo.PropTech.structures.AVLTree;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClienteService {

    // Acceso O(1) por identificación
    private final HashTable<String, Cliente> tablaPorId = new HashTable<>();

    // Ordenamiento por presupuesto para recomendaciones y segmentación
    @Getter
    private final AVLTree<ClienteWrapper> arbolPorPresupuesto = new AVLTree<>();

    // ----------------------------------------------------------------
    // CRUD
    // ----------------------------------------------------------------

    public Cliente registrar(ClienteDTO dto) {
        if (tablaPorId.containsKey(dto.getId())) {
            throw new RuntimeException("Ya existe un cliente con id: " + dto.getId());
        }

        Cliente cliente = mapearDesdeDTO(dto);
        tablaPorId.put(cliente.getId(), cliente);
        arbolPorPresupuesto.insert(new ClienteWrapper(cliente));
        return cliente;
    }

    public Cliente buscarPorId(String id) {
        Cliente cliente = tablaPorId.get(id);
        if (cliente == null) throw new RuntimeException("Cliente no encontrado: " + id);
        return cliente;
    }

    public void actualizar(String id, ClienteDTO dto) {
        Cliente existente = buscarPorId(id);

        arbolPorPresupuesto.remove(new ClienteWrapper(existente));

        existente.setNombre(dto.getNombre());
        existente.setCorreo(dto.getCorreo());
        existente.setTelefono(dto.getTelefono());
        existente.setTipoCliente(dto.getTipoCliente());
        existente.setPresupuesto(dto.getPresupuesto());
        existente.setZonasInteres(dto.getZonasInteres());
        existente.setTipoInmuebleDeseado(dto.getTipoInmuebleDeseado());
        existente.setHabitacionesMinimas(dto.getHabitacionesMinimas());
        existente.setEstadoBusqueda(dto.getEstadoBusqueda());

        arbolPorPresupuesto.insert(new ClienteWrapper(existente));
    }

    public void eliminar(String id) {
        Cliente cliente = buscarPorId(id);
        tablaPorId.remove(id);
        arbolPorPresupuesto.remove(new ClienteWrapper(cliente));
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
        buscarPorId(idCliente).getInmueblesGuardados().addLast(inmueble);
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
        List<Cliente> todos = new ArrayList<>();
        recolectarInOrder(arbolPorPresupuesto.getRoot(), todos);
        return todos;
    }

    private void recolectarInOrder(AVLTree.AVLNode<ClienteWrapper> nodo, List<Cliente> lista) {
        if (nodo == null) return;
        recolectarInOrder(nodo.getLeft(), lista);
        lista.add(nodo.getData().getCliente());
        recolectarInOrder(nodo.getRight(), lista);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private Cliente mapearDesdeDTO(ClienteDTO dto) {
        return new Cliente(
                dto.getId(), dto.getNombre(), dto.getCorreo(), dto.getTelefono(),
                dto.getTipoCliente(), dto.getPresupuesto(), dto.getZonasInteres(),
                dto.getTipoInmuebleDeseado(), dto.getHabitacionesMinimas(),
                dto.getEstadoBusqueda()
        );
    }

    // Wrapper para poder insertar Cliente en el AVL ordenado por presupuesto
    @Getter
    public static class ClienteWrapper implements Comparable<ClienteWrapper> {
        private final Cliente cliente;

        public ClienteWrapper(Cliente cliente) {
            this.cliente = cliente;
        }

        @Override
        public int compareTo(ClienteWrapper otro) {
            int cmp = Double.compare(this.cliente.getPresupuesto(), otro.cliente.getPresupuesto());
            // Si el presupuesto es igual, desambiguamos por id para no perder nodos
            return cmp != 0 ? cmp : this.cliente.getId().compareTo(otro.cliente.getId());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClienteWrapper)) return false;
            return this.cliente.getId().equals(((ClienteWrapper) o).cliente.getId());
        }

        @Override
        public int hashCode() {
            return cliente.getId().hashCode();
        }
    }
}