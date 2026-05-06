package co.edu.uniquindio.poo.PropTech.repository;

import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.structures.AVLTree;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AsesorRepository {

    private final HashTable<String, Asesor> tablaPorId = new HashTable<>();

    // Ordenado por número de cierres para rankings de efectividad
    private final AVLTree<AsesorWrapper> arbolPorCierres = new AVLTree<>();

    // ----------------------------------------------------------------
    // Escritura
    // ----------------------------------------------------------------

    public Asesor save(Asesor asesor) {
        tablaPorId.put(asesor.getId(), asesor);
        arbolPorCierres.insert(new AsesorWrapper(asesor));
        return asesor;
    }

    // Cuando cambia el número de cierres hay que rebalancear el árbol
    public void updateCierres(Asesor asesor) {
        arbolPorCierres.remove(new AsesorWrapper(asesor));
        arbolPorCierres.insert(new AsesorWrapper(asesor));
    }

    public void delete(String id) {
        Asesor asesor = tablaPorId.get(id);
        if (asesor != null) {
            tablaPorId.remove(id);
            arbolPorCierres.remove(new AsesorWrapper(asesor));
        }
    }

    // ----------------------------------------------------------------
    // Lectura
    // ----------------------------------------------------------------

    public Optional<Asesor> findById(String id) {
        return Optional.ofNullable(tablaPorId.get(id));
    }

    public boolean existsById(String id) {
        return tablaPorId.containsKey(id);
    }

    public List<Asesor> findAll() {
        List<Asesor> todos = new ArrayList<>();
        for (Asesor a : tablaPorId) todos.add(a);
        return todos;
    }

    public List<Asesor> findAllOrdenadosPorCierres() {
        List<Asesor> ordenados = new ArrayList<>();
        recolectarInOrderDesc(arbolPorCierres.getRoot(), ordenados);
        return ordenados;
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    // Descendente: más cierres primero
    private void recolectarInOrderDesc(AVLTree.AVLNode<AsesorWrapper> nodo, List<Asesor> lista) {
        if (nodo == null) return;
        recolectarInOrderDesc(nodo.getRight(), lista);
        lista.add(nodo.getData().getAsesor());
        recolectarInOrderDesc(nodo.getLeft(), lista);
    }

    // ----------------------------------------------------------------
    // Wrapper para AVL ordenado por número de cierres
    // ----------------------------------------------------------------

    public static class AsesorWrapper implements Comparable<AsesorWrapper> {

        private final Asesor asesor;

        public AsesorWrapper(Asesor asesor) {
            this.asesor = asesor;
        }

        public Asesor getAsesor() {
            return asesor;
        }

        @Override
        public int compareTo(AsesorWrapper otro) {
            int cmp = Integer.compare(
                    this.asesor.getCierresRealizados().getSize(),
                    otro.asesor.getCierresRealizados().getSize()
            );
            return cmp != 0 ? cmp : this.asesor.getId().compareTo(otro.asesor.getId());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AsesorWrapper)) return false;
            return this.asesor.getId().equals(((AsesorWrapper) o).asesor.getId());
        }

        @Override
        public int hashCode() {
            return asesor.getId().hashCode();
        }
    }
}