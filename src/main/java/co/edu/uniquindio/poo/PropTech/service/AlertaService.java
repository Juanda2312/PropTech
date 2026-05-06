package co.edu.uniquindio.poo.PropTech.service;

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
        Alerta alerta = new Alerta(idAlerta, tipo, descripcion, LocalDate.now(), nivel, false);
        return alertaRepository.save(alerta);
    }

    public void cerrar(String idAlerta) {
        Alerta alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada: " + idAlerta));
        alerta.setCerrada(true);
    }

    public Alerta procesarSiguiente() {
        return alertaRepository.pollPendiente()
                .orElseThrow(() -> new RuntimeException("No hay alertas pendientes"));
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
        return alertaRepository.sizePendientes();
    }
}