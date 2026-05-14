package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.exception.EntidadNoEncontradaException;
import co.edu.uniquindio.poo.PropTech.exception.EstadoInvalidoException;
import co.edu.uniquindio.poo.PropTech.exception.ReglaNegocioException;
import co.edu.uniquindio.poo.PropTech.model.entity.Alerta;
import co.edu.uniquindio.poo.PropTech.model.enums.NivelAtencion;
import co.edu.uniquindio.poo.PropTech.repository.AlertaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    // ----------------------------------------------------------------
    // Generación
    // ----------------------------------------------------------------

    public Alerta generar(String idAlerta, String tipo, String descripcion, NivelAtencion nivel) {
        // Si ya existe (cerrada o abierta) con este ID exacto, no regenerar
        if (alertaRepository.existsById(idAlerta)) {
            return alertaRepository.findById(idAlerta).get();
        }
        Alerta alerta = new Alerta(idAlerta, tipo, descripcion, LocalDate.now(), nivel, false);
        return alertaRepository.save(alerta);
    }

    public void cerrar(String idAlerta) {
        Alerta alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new EntidadNoEncontradaException("Alerta", idAlerta));
        if (alerta.isCerrada()) {
            throw new EstadoInvalidoException("Alerta", "CERRADA", "CERRAR");
        }
        alerta.setCerrada(true);
    }

    public Alerta procesarSiguiente() {
        return alertaRepository.pollPrioridad()
                .orElseThrow(() -> new ReglaNegocioException(
                        "No hay alertas pendientes en la cola para procesar."));
    }

    // ----------------------------------------------------------------
    // Consultas
    // ----------------------------------------------------------------

    public List<Alerta> obtenerTodas() {
        return alertaRepository.findAll();
    }

    public List<Alerta> obtenerAbiertas() {
        return alertaRepository.findAbiertas();
    }

    public List<Alerta> obtenerPorNivel(NivelAtencion nivel) {
        return alertaRepository.findByNivel(nivel);
    }

    public int totalPendientes() {
        return alertaRepository.sizePrioridad();
    }


}