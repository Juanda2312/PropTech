package co.edu.uniquindio.poo.PropTech.model.enums;

/**
 * Tipos de interacción que un cliente puede tener con la plataforma.
 */
public enum TipoInteraccion {
    /** El cliente agendó una visita a un inmueble */
    VISITA_AGENDADA,
    /** El cliente realizó una compra */
    COMPRA_REALIZADA,
    /** El cliente firmó un contrato de renta */
    RENTA_REALIZADA,
    /** El cliente marcó un inmueble como favorito */
    FAVORITO_MARCADO,
    /** El cliente expresó intención formal de compra */
    INTENCION_COMPRA,
    /** El cliente expresó intención formal de renta */
    INTENCION_RENTA
}