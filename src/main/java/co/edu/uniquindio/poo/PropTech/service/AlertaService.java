package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.dto.AlertaDTO;
import co.edu.uniquindio.poo.PropTech.model.entity.Alerta;
import co.edu.uniquindio.poo.PropTech.model.enums.NivelAtencion;
import co.edu.uniquindio.poo.PropTech.structures.Queue;
import co.edu.uniquindio.poo.PropTech.structures.SimpleLinkedList;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertaService {

    // Lista histórica de todas las alertas generadas
    private final SimpleLinkedList<Alerta> listaAlertas = new SimpleLinkedList<>();

    // Cola para alertas pendientes de revisión (FIFO)
    private final Queue<Alerta> colaPendientes = new Queue<>();

    // ----------------------------------------------------------------
    // Generación
    // ----------------------------------------------------------------

    public Alerta generar(String idAlerta, String tipo, String descripcion, NivelAtencion nivel) {
        Alerta alerta = new Alerta(idAlerta, tipo, descripcion, LocalDate.now(), nivel, false);
        listaAlertas.addLast(alerta);
        colaPendientes.enqueue(alerta);
        return alerta;
    }

    public void cerrar(String idAlerta) {
        for (Alerta a : listaAlertas) {
            if (a.getIdAlerta().equals(idAlerta)) {
                a.setCerrada(true);
                return;
            }
        }
        throw new RuntimeException("Alerta no encontrada: " + idAlerta);
    }

    public Alerta procesarSiguiente() {
        if (colaPendientes.isEmpty()) throw new RuntimeException("No hay alertas pendientes");
        return colaPendientes.dequeue();
    }

    // ----------------------------------------------------------------
    // Consultas
    // ----------------------------------------------------------------

    public List<Alerta> obtenerPorNivel(NivelAtencion nivel) {
        List<Alerta> resultado = new ArrayList<>();
        for (Alerta a : listaAlertas) {
            if (a.getNivel() == nivel) resultado.add(a);
        }
        return resultado;
    }

    public List<Alerta> obtenerAbiertas() {
        List<Alerta> resultado = new ArrayList<>();
        for (Alerta a : listaAlertas) {
            if (!a.isCerrada()) resultado.add(a);
        }
        return resultado;
    }

    public List<Alerta> obtenerTodas() {
        List<Alerta> todas = new ArrayList<>();
        for (Alerta a : listaAlertas) todas.add(a);
        return todas;
    }

    public int totalPendientes() {
        return colaPendientes.getSize();
    }
}