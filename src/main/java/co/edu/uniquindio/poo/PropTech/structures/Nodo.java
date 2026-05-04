package co.edu.uniquindio.poo.PropTech.structures;

public class Nodo<T> {
    public T dato;
    public Nodo<T> siguiente;

    public Nodo(T dato) {
        this.dato = dato;
        this.siguiente = null;
    }
}