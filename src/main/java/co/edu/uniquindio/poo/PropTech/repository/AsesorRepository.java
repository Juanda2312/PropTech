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
    private final AVLTree<AsesorWrapper> arbolPorCierres = new AVLTree<>();

    public Asesor save(Asesor asesor) {
        tablaPorId.put(asesor.getId(), asesor);
        arbolPorCierres.insert(new AsesorWrapper(asesor));
        return asesor;
    }

    // Corregido: primero removemos con el tamaño ANTERIOR, luego insertamos con el nuevo
    public void updateCierres(Asesor asesor, int cierresAnteriores) {
        arbolPorCierres.remove(new AsesorWrapper(asesor, cierresAnteriores));
        arbolPorCierres.insert(new AsesorWrapper(asesor));
    }

    public void delete(String id) {
        Asesor asesor = tablaPorId.get(id);
        if (asesor != null) {
            tablaPorId.remove(id);
            arbolPorCierres.remove(new AsesorWrapper(asesor));
        }
    }

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

    private void recolectarInOrderDesc(AVLTree.AVLNode<AsesorWrapper> nodo, List<Asesor> lista) {
        if (nodo == null) return;
        recolectarInOrderDesc(nodo.getRight(), lista);
        lista.add(nodo.getData().getAsesor());
        recolectarInOrderDesc(nodo.getLeft(), lista);
    }

    public static class AsesorWrapper implements Comparable<AsesorWrapper> {

        private final Asesor asesor;
        private final int cierresSnapshot;

        public AsesorWrapper(Asesor asesor) {
            this.asesor = asesor;
            this.cierresSnapshot = asesor.getCierresRealizados().getSize();
        }

        // Constructor para buscar con valor anterior de cierres
        public AsesorWrapper(Asesor asesor, int cierresAnteriores) {
            this.asesor = asesor;
            this.cierresSnapshot = cierresAnteriores;
        }

        public Asesor getAsesor() { return asesor; }

        @Override
        public int compareTo(AsesorWrapper otro) {
            int cmp = Integer.compare(this.cierresSnapshot, otro.cierresSnapshot);
            return cmp != 0 ? cmp : this.asesor.getId().compareTo(otro.asesor.getId());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AsesorWrapper)) return false;
            return this.asesor.getId().equals(((AsesorWrapper) o).asesor.getId());
        }

        @Override
        public int hashCode() { return asesor.getId().hashCode(); }
    }
}