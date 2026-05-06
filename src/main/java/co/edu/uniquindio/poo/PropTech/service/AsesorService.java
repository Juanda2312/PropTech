package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.dto.AsesorDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Operacion;
import co.edu.uniquindio.poo.PropTech.model.entity.Visita;
import co.edu.uniquindio.poo.PropTech.structures.AVLTree;
import co.edu.uniquindio.poo.PropTech.structures.HashTable;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AsesorService {

    private final HashTable<String, Asesor> tablaPorId = new HashTable<>();

    // Ordenados por número de cierres para rankings de efectividad
    @Getter
    private final AVLTree<AsesorWrapper> arbolPorCierres = new AVLTree<>();

    // ----------------------------------------------------------------
    // CRUD
    // ----------------------------------------------------------------

    public Asesor registrar(AsesorDTO dto) {
        if (tablaPorId.containsKey(dto.getId())) {
            throw new RuntimeException("Ya existe un asesor con id: " + dto.getId());
        }

        Asesor asesor = new Asesor(dto.getId(), dto.getNombre(), dto.getContacto(), dto.getEspecialidadZona());
        tablaPorId.put(asesor.getId(), asesor);
        arbolPorCierres.insert(new AsesorWrapper(asesor));
        return asesor;
    }

    public Asesor buscarPorId(String id) {
        Asesor asesor = tablaPorId.get(id);
        if (asesor == null) throw new RuntimeException("Asesor no encontrado: " + id);
        return asesor;
    }

    public void actualizar(String id, AsesorDTO dto) {
        Asesor existente = buscarPorId(id);
        existente.setNombre(dto.getNombre());
        existente.setContacto(dto.getContacto());
        existente.setEspecialidadZona(dto.getEspecialidadZona());
    }

    // ----------------------------------------------------------------
    // Gestión de carga
    // ----------------------------------------------------------------

    public void asignarInmueble(String idAsesor, Inmueble inmueble) {
        buscarPorId(idAsesor).getInmueblesAsignados().addLast(inmueble);
    }

    public void agregarVisitaAgendada(String idAsesor, Visita visita) {
        buscarPorId(idAsesor).getVisitasAgendadas().addLast(visita);
    }

    public void registrarCierre(String idAsesor, Operacion operacion) {
        Asesor asesor = buscarPorId(idAsesor);

        // Actualizamos el árbol porque el criterio de orden cambia
        arbolPorCierres.remove(new AsesorWrapper(asesor));
        asesor.getCierresRealizados().addLast(operacion);
        arbolPorCierres.insert(new AsesorWrapper(asesor));
    }

    public int contarCarga(String idAsesor) {
        Asesor asesor = buscarPorId(idAsesor);
        return asesor.getVisitasAgendadas().getSize() + asesor.getInmueblesAsignados().getSize();
    }

    // ----------------------------------------------------------------
    // Rankings
    // ----------------------------------------------------------------

    public List<Asesor> obtenerRankingPorCierres() {
        List<Asesor> ranking = new ArrayList<>();
        recolectarInOrderDesc(arbolPorCierres.getRoot(), ranking);
        return ranking;
    }

    public List<Asesor> obtenerTodos() {
        List<Asesor> todos = new ArrayList<>();
        recolectarInOrder(arbolPorCierres.getRoot(), todos);
        return todos;
    }

    // Recorrido inverso para ranking descendente (más cierres primero)
    private void recolectarInOrderDesc(AVLTree.AVLNode<AsesorWrapper> nodo, List<Asesor> lista) {
        if (nodo == null) return;
        recolectarInOrderDesc(nodo.getRight(), lista);
        lista.add(nodo.getData().getAsesor());
        recolectarInOrderDesc(nodo.getLeft(), lista);
    }

    private void recolectarInOrder(AVLTree.AVLNode<AsesorWrapper> nodo, List<Asesor> lista) {
        if (nodo == null) return;
        recolectarInOrder(nodo.getLeft(), lista);
        lista.add(nodo.getData().getAsesor());
        recolectarInOrder(nodo.getRight(), lista);
    }

    // ----------------------------------------------------------------
    // Wrapper para AVL ordenado por número de cierres
    // ----------------------------------------------------------------

    @Getter
    public static class AsesorWrapper implements Comparable<AsesorWrapper> {
        private final Asesor asesor;

        public AsesorWrapper(Asesor asesor) {
            this.asesor = asesor;
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