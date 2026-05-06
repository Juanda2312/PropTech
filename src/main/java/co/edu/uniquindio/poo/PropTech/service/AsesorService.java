package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.exception.EntidadDuplicadaException;
import co.edu.uniquindio.poo.PropTech.exception.EntidadNoEncontradaException;
import co.edu.uniquindio.poo.PropTech.model.dto.AsesorDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Asesor;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Operacion;
import co.edu.uniquindio.poo.PropTech.model.entity.Visita;
import co.edu.uniquindio.poo.PropTech.repository.AsesorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsesorService {

    private final AsesorRepository asesorRepository;

    public AsesorService(AsesorRepository asesorRepository) {
        this.asesorRepository = asesorRepository;
    }

    // ----------------------------------------------------------------
    // CRUD
    // ----------------------------------------------------------------

    public Asesor registrar(AsesorDTO dto) {
        if (asesorRepository.existsById(dto.getId())) {
            throw new EntidadDuplicadaException("Asesor", dto.getId());
        }
        return asesorRepository.save(
                new Asesor(dto.getId(), dto.getNombre(), dto.getContacto(), dto.getEspecialidadZona())
        );
    }

    public Asesor buscarPorId(String id) {
        return asesorRepository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Asesor", id));
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
        asesor.getCierresRealizados().addLast(operacion);
        asesorRepository.updateCierres(asesor);
    }

    public int contarCarga(String idAsesor) {
        Asesor asesor = buscarPorId(idAsesor);
        return asesor.getVisitasAgendadas().getSize()
                + asesor.getInmueblesAsignados().getSize();
    }

    // ----------------------------------------------------------------
    // Consultas y rankings
    // ----------------------------------------------------------------

    public List<Asesor> obtenerTodos() {
        return asesorRepository.findAll();
    }

    public List<Asesor> obtenerRankingPorCierres() {
        return asesorRepository.findAllOrdenadosPorCierres();
    }
}