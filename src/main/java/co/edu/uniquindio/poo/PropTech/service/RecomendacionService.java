package co.edu.uniquindio.poo.PropTech.service;

import co.edu.uniquindio.poo.PropTech.model.entity.Cliente;
import co.edu.uniquindio.poo.PropTech.model.entity.Inmueble;
import co.edu.uniquindio.poo.PropTech.model.entity.Recomendacion;
import co.edu.uniquindio.poo.PropTech.model.enums.Zona;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class RecomendacionService {

    private final InmuebleService inmuebleService;
    private final ClienteService  clienteService;

    public RecomendacionService(InmuebleService inmuebleService,
                                ClienteService clienteService) {
        this.inmuebleService = inmuebleService;
        this.clienteService  = clienteService;
    }


    public List<Recomendacion> generarParaCliente(String idCliente) {
        Cliente cliente = clienteService.buscarPorId(idCliente);
        List<Recomendacion> recomendaciones = new ArrayList<>();

        // Construimos el conjunto de barrios que el cliente ya visitó
        // para el criterio 5 (propiedades similares visitadas)
        List<String> barriosVisitados = new ArrayList<>();
        cliente.getPropiedadesVisitadas().forEach(i -> barriosVisitados.add(i.getBarrio()));

        // Construimos el conjunto de barrios que el cliente consultó
        // para el criterio 5 (historial de consultas)
        List<String> barriosConsultados = new ArrayList<>();
        cliente.getInmueblesConsultados().forEach(i -> barriosConsultados.add(i.getBarrio()));

        for (Inmueble inmueble : inmuebleService.filtrarDisponibles()) {
            double puntaje = calcularPuntaje(cliente, inmueble, barriosVisitados, barriosConsultados);

            if (puntaje > 0) {
                String criterio = construirDescripcionCriterio(
                        cliente, inmueble, barriosVisitados, barriosConsultados);

                Recomendacion rec = new Recomendacion(
                        UUID.randomUUID().toString(),
                        inmueble,
                        puntaje,
                        criterio,
                        LocalDate.now()
                );
                recomendaciones.add(rec);
                clienteService.agregarRecomendacion(idCliente, rec);
            }
        }

        recomendaciones.sort(Comparator.reverseOrder());
        return recomendaciones;
    }

    // ----------------------------------------------------------------
    // Cálculo de puntaje con los 6 criterios
    // ----------------------------------------------------------------

    private double calcularPuntaje(Cliente cliente, Inmueble inmueble,
                                   List<String> barriosVisitados,
                                   List<String> barriosConsultados) {
        double puntaje = 0;

        // Criterio 1: presupuesto — 40 pts
        if (inmueble.getPrecio() <= cliente.getPresupuesto()) {
            puntaje += 40;
        }

        // Criterio 2: tipo de inmueble deseado — 20 pts
        if (inmueble.getTipoInmueble() == cliente.getTipoInmuebleDeseado()) {
            puntaje += 20;
        }

        // Criterio 3: habitaciones mínimas requeridas — 15 pts
        if (inmueble.getHabitaciones() >= cliente.getHabitacionesMinimas()) {
            puntaje += 15;
        }

        // Criterio 4: zona de interés del cliente — 15 pts
        // Comparamos el barrio del inmueble con las zonas que le interesan al cliente
        if (coincideConZonasInteres(cliente, inmueble)) {
            puntaje += 15;
        }

        // Criterio 5: historial de consultas — el cliente ya consultó inmuebles del mismo barrio — 5 pts
        if (barriosConsultados.contains(inmueble.getBarrio())) {
            puntaje += 5;
        }

        // Criterio 6: propiedades similares visitadas anteriormente — mismo barrio visitado — 5 pts
        if (barriosVisitados.contains(inmueble.getBarrio())) {
            puntaje += 5;
        }

        return puntaje;
    }

    // Compara las zonas de interés del cliente (NORTE, SUR, ESTE, OESTE)
    // con la ciudad/barrio del inmueble usando coincidencia por nombre
    private boolean coincideConZonasInteres(Cliente cliente, Inmueble inmueble) {
        if (cliente.getZonasInteres() == null || cliente.getZonasInteres().length == 0) {
            return false;
        }
        String barrio = inmueble.getBarrio().toUpperCase();
        String ciudad = inmueble.getCiudad().toUpperCase();

        for (Zona zona : cliente.getZonasInteres()) {
            String nombreZona = zona.name(); // NORTE, SUR, ESTE, OESTE
            if (barrio.contains(nombreZona) || ciudad.contains(nombreZona)) {
                return true;
            }
        }
        // Si el cliente tiene zona NORTE y el barrio es "El Poblado" ,
        // no coincide por nombre, pero sí por asignación conocida.
        // La comparación directa por nombre es la más segura sin una tabla de mapeo.
        return false;
    }

    private String construirDescripcionCriterio(Cliente cliente, Inmueble inmueble,
                                                List<String> barriosVisitados,
                                                List<String> barriosConsultados) {
        List<String> razones = new ArrayList<>();

        if (inmueble.getPrecio() <= cliente.getPresupuesto())
            razones.add("precio dentro del presupuesto");
        if (inmueble.getTipoInmueble() == cliente.getTipoInmuebleDeseado())
            razones.add("tipo de inmueble coincide");
        if (inmueble.getHabitaciones() >= cliente.getHabitacionesMinimas())
            razones.add("habitaciones suficientes");
        if (coincideConZonasInteres(cliente, inmueble))
            razones.add("zona de interés del cliente");
        if (barriosConsultados.contains(inmueble.getBarrio()))
            razones.add("barrio consultado anteriormente");
        if (barriosVisitados.contains(inmueble.getBarrio()))
            razones.add("visitó inmuebles similares en este barrio");

        return String.join(", ", razones);
    }

    // ----------------------------------------------------------------
    // Inmuebles similares a uno dado (mismo tipo, finalidad y precio ±20%)
    // ----------------------------------------------------------------

    public List<Inmueble> sugerirSimilares(String codigoInmueble) {
        Inmueble referencia = inmuebleService.buscarPorCodigo(codigoInmueble);
        List<Inmueble> similares = new ArrayList<>();

        for (Inmueble candidato : inmuebleService.filtrarDisponibles()) {
            if (candidato.getCodigo().equals(codigoInmueble)) continue;

            boolean mismoTipo      = candidato.getTipoInmueble() == referencia.getTipoInmueble();
            boolean mismaFinalidad = candidato.getFinalidad()    == referencia.getFinalidad();
            boolean precioCercano  = Math.abs(candidato.getPrecio() - referencia.getPrecio())
                    <= referencia.getPrecio() * 0.20;

            if (mismoTipo && mismaFinalidad && precioCercano) {
                similares.add(candidato);
            }
        }

        return similares;
    }
}