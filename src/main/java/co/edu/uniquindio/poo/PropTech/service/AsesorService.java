package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.exception.EntidadDuplicadaException;
import co.edu.uniquindio.poo.PropTech.exception.EntidadNoEncontradaException;
import co.edu.uniquindio.poo.PropTech.exception.ReglaNegocioException;
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

    public Asesor registrar(AsesorDTO dto) {
        validarId(dto.getId());
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

    public void asignarInmueble(String idAsesor, Inmueble inmueble) {
        buscarPorId(idAsesor).getInmueblesAsignados().addLast(inmueble);
    }

    public void agregarVisitaAgendada(String idAsesor, Visita visita) {
        buscarPorId(idAsesor).getVisitasAgendadas().addLast(visita);
    }

    public void registrarCierre(String idAsesor, Operacion operacion) {
        Asesor asesor = buscarPorId(idAsesor);
        int cierresAnteriores = asesor.getCierresRealizados().getSize();
        asesor.getCierresRealizados().addLast(operacion);
        asesorRepository.updateCierres(asesor, cierresAnteriores);
    }

    public int contarCarga(String idAsesor) {
        Asesor asesor = buscarPorId(idAsesor);
        return asesor.getVisitasAgendadas().getSize()
                + asesor.getInmueblesAsignados().getSize();
    }

    public List<Asesor> obtenerTodos() {
        return asesorRepository.findAll();
    }

    public List<Asesor> obtenerRankingPorCierres() {
        return asesorRepository.findAllOrdenadosPorCierres();
    }

    // ----------------------------------------------------------------
    // Validación de ID (cédula colombiana: exactamente 10 dígitos)
    // ----------------------------------------------------------------

    private void validarId(String id) {
        if (id == null || id.isBlank()) {
            throw new ReglaNegocioException("El ID del asesor no puede estar vacío.");
        }
        if (!id.matches("\\d{10}")) {
            throw new ReglaNegocioException(
                    "El ID del asesor debe ser una cédula de exactamente 10 dígitos numéricos. Valor recibido: '" + id + "'.");
        }
    }
}