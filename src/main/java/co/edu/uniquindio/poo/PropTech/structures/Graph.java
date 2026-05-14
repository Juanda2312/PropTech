package co.edu.uniquindio.poo.PropTech.structures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *   - HashTable: acceso O(1) por vertice para obtener sus vecinos.
 *   - SimpleLinkedList: almacena los vecinos de cada vertice sin costo
 *     de redimension fija, permitiendo agregar aristas en O(1) al final.
 */
public class Graph<T extends Comparable<T>> {

    // Lista de adyacencia: vertice -> lista de vecinos
    private final HashTable<T, SimpleLinkedList<T>> adjacencyList;
    private final boolean directed;

    public Graph(boolean directed) {
        this.directed      = directed;
        this.adjacencyList = new HashTable<>();
    }

    // ----------------------------------------------------------------
    // Vertices
    // ----------------------------------------------------------------

    public void addVertex(T vertex) {
        if (!adjacencyList.containsKey(vertex)) {
            adjacencyList.put(vertex, new SimpleLinkedList<>());
        }
    }

    public boolean containsVertex(T vertex) {
        return adjacencyList.containsKey(vertex);
    }

    public void removeVertex(T vertex) {
        if (!adjacencyList.containsKey(vertex)) return;
        adjacencyList.remove(vertex);
        // Eliminar el vertice de las listas de vecinos de los demas
        for (SimpleLinkedList<T> vecinos : adjacencyList) {
            int idx = vecinos.getIndex(vertex);
            if (idx >= 0) vecinos.remove(idx);
        }
    }

    // ----------------------------------------------------------------
    // Aristas
    // ----------------------------------------------------------------

    public void addEdge(T origin, T destination) {
        addVertex(origin);
        addVertex(destination);
        adjacencyList.get(origin).addLast(destination);
        if (!directed) {
            adjacencyList.get(destination).addLast(origin);
        }
    }

    public void removeEdge(T origin, T destination) {
        if (!adjacencyList.containsKey(origin)) return;
        SimpleLinkedList<T> vecinos = adjacencyList.get(origin);
        int idx = vecinos.getIndex(destination);
        if (idx >= 0) vecinos.remove(idx);

        if (!directed && adjacencyList.containsKey(destination)) {
            SimpleLinkedList<T> vecinosDestino = adjacencyList.get(destination);
            int idx2 = vecinosDestino.getIndex(origin);
            if (idx2 >= 0) vecinosDestino.remove(idx2);
        }
    }

    public boolean containsEdge(T origin, T destination) {
        if (!adjacencyList.containsKey(origin)) return false;
        return adjacencyList.get(origin).getIndex(destination) >= 0;
    }

    // ----------------------------------------------------------------
    // Vecinos — devuelve List<T> para compatibilidad con el resto del sistema
    // ----------------------------------------------------------------

    public List<T> getNeighbors(T vertex) {
        List<T> resultado = new ArrayList<>();
        if (!adjacencyList.containsKey(vertex)) return resultado;
        SimpleLinkedList<T> vecinos = adjacencyList.get(vertex);
        for (T v : vecinos) resultado.add(v);
        return resultado;
    }

    // ----------------------------------------------------------------
    // Recorridos usando estructuras propias
    // ----------------------------------------------------------------

    /**
     * BFS usando Queue propia.
     * Retorna la lista de nodos visitados en orden de amplitud.
     */
    public List<T> breadthFirstSearch(T start) {
        List<T> resultado = new ArrayList<>();
        if (!adjacencyList.containsKey(start)) return resultado;

        Queue<T> cola       = new Queue<>();
        HashTable<T, Boolean> visitados = new HashTable<>();

        cola.enqueue(start);
        visitados.put(start, true);

        while (!cola.isEmpty()) {
            T actual = cola.dequeue();
            resultado.add(actual);

            SimpleLinkedList<T> vecinos = adjacencyList.get(actual);
            if (vecinos == null) continue;
            for (T vecino : vecinos) {
                if (!visitados.containsKey(vecino)) {
                    visitados.put(vecino, true);
                    cola.enqueue(vecino);
                }
            }
        }
        return resultado;
    }

    /**
     * DFS usando Stack propia (iterativo).
     * Retorna la lista de nodos visitados en orden de profundidad.
     */
    public List<T> depthFirstSearch(T start) {
        List<T> resultado = new ArrayList<>();
        if (!adjacencyList.containsKey(start)) return resultado;

        Stack<T> pila       = new Stack<>();
        HashTable<T, Boolean> visitados = new HashTable<>();

        pila.push(start);

        while (!pila.isEmpty()) {
            T actual = pila.pop();
            if (visitados.containsKey(actual)) continue;
            visitados.put(actual, true);
            resultado.add(actual);

            SimpleLinkedList<T> vecinos = adjacencyList.get(actual);
            if (vecinos == null) continue;
            for (T vecino : vecinos) {
                if (!visitados.containsKey(vecino)) {
                    pila.push(vecino);
                }
            }
        }
        return resultado;
    }

    // ----------------------------------------------------------------
    // Informacion del grafo
    // ----------------------------------------------------------------

    public int getVertexCount() {
        return adjacencyList.getSize();
    }

    public int getEdgeCount() {
        int count = 0;
        for (SimpleLinkedList<T> vecinos : adjacencyList) {
            count += vecinos.getSize();
        }
        return directed ? count : count / 2;
    }

    public boolean isEmpty() {
        return adjacencyList.getSize() == 0;
    }

    public void clear() {
        // Vaciamos cada lista antes de limpiar la tabla
        for (SimpleLinkedList<T> vecinos : adjacencyList) {
            vecinos.removeAll();
        }
    }

    public void print() {
        for (SimpleLinkedList<T> vecinos : adjacencyList) {
            // No podemos imprimir la clave directamente desde el iterador de valores,
            // asi que imprimimos solo los vecinos de cada nodo recorrido
            System.out.print("[ ");
            for (T v : vecinos) System.out.print(v + " ");
            System.out.println("]");
        }
    }
}