package co.edu.uniquindio.poo.PropTech.repository;

import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.structures.AVLTree;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ClienteRepository {

    // Acceso O(1) por identificación
    private final HashTable<String, Cliente> tablaPorId = new HashTable<>();

    // Ordenamiento por presupuesto para segmentación y recomendaciones
    private final AVLTree<ClienteWrapper> arbolPorPresupuesto = new AVLTree<>();

    // ----------------------------------------------------------------
    // Escritura
    // ----------------------------------------------------------------

    public Cliente save(Cliente cliente) {
        tablaPorId.put(cliente.getId(), cliente);
        arbolPorPresupuesto.insert(new ClienteWrapper(cliente));
        return cliente;
    }

    public void update(Cliente anterior, Cliente actualizado) {
        arbolPorPresupuesto.remove(new ClienteWrapper(anterior));
        tablaPorId.put(actualizado.getId(), actualizado);
        arbolPorPresupuesto.insert(new ClienteWrapper(actualizado));
    }

    public void delete(String id) {
        Cliente cliente = tablaPorId.get(id);
        if (cliente != null) {
            tablaPorId.remove(id);
            arbolPorPresupuesto.remove(new ClienteWrapper(cliente));
        }
    }

    // ----------------------------------------------------------------
    // Lectura
    // ----------------------------------------------------------------

    public Optional<Cliente> findById(String id) {
        return Optional.ofNullable(tablaPorId.get(id));
    }

    public boolean existsById(String id) {
        return tablaPorId.containsKey(id);
    }

    public List<Cliente> findAll() {
        List<Cliente> todos = new ArrayList<>();
        for (Cliente c : tablaPorId) todos.add(c);
        return todos;
    }

    public List<Cliente> findAllOrdenadosPorPresupuesto() {
        List<Cliente> ordenados = new ArrayList<>();
        recolectarInOrder(arbolPorPresupuesto.getRoot(), ordenados);
        return ordenados;
    }

    public List<Cliente> findByPresupuestoMaximo(double max) {
        List<Cliente> resultado = new ArrayList<>();
        for (Cliente c : tablaPorId) {
            if (c.getPresupuesto() <= max) resultado.add(c);
        }
        return resultado;
    }

    // ----------------------------------------------------------------
    // Helper de recorrido inOrder del AVL
    // ----------------------------------------------------------------

    private void recolectarInOrder(AVLTree.AVLNode<ClienteWrapper> nodo, List<Cliente> lista) {
        if (nodo == null) return;
        recolectarInOrder(nodo.getLeft(), lista);
        lista.add(nodo.getData().getCliente());
        recolectarInOrder(nodo.getRight(), lista);
    }

    // ----------------------------------------------------------------
    // Wrapper para poder insertar Cliente en el AVL por presupuesto
    // ----------------------------------------------------------------

    public static class ClienteWrapper implements Comparable<ClienteWrapper> {

        private final Cliente cliente;

        public ClienteWrapper(Cliente cliente) {
            this.cliente = cliente;
        }

        public Cliente getCliente() {
            return cliente;
        }

        @Override
        public int compareTo(ClienteWrapper otro) {
            int cmp = Double.compare(this.cliente.getPresupuesto(), otro.cliente.getPresupuesto());
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