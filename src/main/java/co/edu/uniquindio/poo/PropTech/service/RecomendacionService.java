package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Recomendacion;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class RecomendacionService {

    private final InmuebleService  inmuebleService;
    private final ClienteService   clienteService;

    public RecomendacionService(InmuebleService inmuebleService, ClienteService clienteService) {
        this.inmuebleService = inmuebleService;
        this.clienteService  = clienteService;
    }

    // ----------------------------------------------------------------
    // Motor de recomendación
    // ----------------------------------------------------------------

    public List<Recomendacion> generarParaCliente(String idCliente) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        List<Inmueble> candidatos = inmuebleService.filtrarDisponibles();
        List<Recomendacion> recomendaciones = new ArrayList<>();

        for (Inmueble inmueble : candidatos) {
            Recomendacion rec = new Recomendacion(
                    UUID.randomUUID().toString(),
                    inmueble,
                    0.0,
                    "AUTO",
                    LocalDate.now()
            );
            double puntaje = rec.calcularCoincidencia(cliente);

            if (puntaje > 0) {
                rec.setPuntaje(puntaje);
                recomendaciones.add(rec);
                clienteService.agregarRecomendacion(idCliente, rec);
            }
        }

        // Ordenamos de mayor a menor puntaje
        recomendaciones.sort(Comparator.reverseOrder());
        return recomendaciones;
    }

    // Recomendación de inmuebles similares a uno dado
    public List<Inmueble> sugerirSimilares(String codigoInmueble) {
        Inmueble referencia = inmuebleService.buscarPorCodigo(codigoInmueble);
        List<Inmueble> similares = new ArrayList<>();

        for (Inmueble candidato : inmuebleService.filtrarDisponibles()) {
            if (candidato.getCodigo().equals(codigoInmueble)) continue;

            boolean mismoTipo     = candidato.getTipoInmueble() == referencia.getTipoInmueble();
            boolean mismaFinalidad = candidato.getFinalidad() == referencia.getFinalidad();
            boolean precioCercano = Math.abs(candidato.getPrecio() - referencia.getPrecio())
                    <= referencia.getPrecio() * 0.20; // margen del 20%

            if (mismoTipo && mismaFinalidad && precioCercano) {
                similares.add(candidato);
            }
        }

        return similares;
    }
}