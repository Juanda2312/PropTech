package co.edu.uniquindio.poo.PropTech.structures;

/**
 * Grafo no dirigido / dirigido implementado con lista de adyacencia.
 *
 * Estructuras propias utilizadas:
 *   - HashTable : acceso O(1) por vértice para obtener sus vecinos.
 *   - SimpleLinkedList : almacena los vecinos de cada vértice sin
 *     costo de redimensión fija, permitiendo agregar aristas en O(1) al final.
 *   - Queue : recorrido BFS (propio).
 *   - Stack : recorrido DFS iterativo (propio).
 *
 * NO se importa ninguna clase de java.util.
 */
public class Graph<T extends Comparable<T>> {

    // Lista de adyacencia: vértice -> lista de vecinos
    private final HashTable<T, SimpleLinkedList<T>> adjacencyList;
    private final boolean directed;

    public Graph(boolean directed) {
        this.directed      = directed;
        this.adjacencyList = new HashTable<>();
    }

    // ----------------------------------------------------------------
    // Vértices
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
        // Eliminar el vértice de las listas de vecinos de los demás
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
    // Vecinos — retorna SimpleLinkedList propia
    // ----------------------------------------------------------------

    public SimpleLinkedList<T> getNeighbors(T vertex) {
        if (!adjacencyList.containsKey(vertex)) return new SimpleLinkedList<>();
        return adjacencyList.get(vertex);
    }

    // ----------------------------------------------------------------
    // Recorridos usando estructuras propias
    // ----------------------------------------------------------------

    /**
     * BFS usando Queue propia.
     * Retorna SimpleLinkedList con los nodos visitados en orden de amplitud.
     */
    public SimpleLinkedList<T> breadthFirstSearch(T start) {
        SimpleLinkedList<T> resultado = new SimpleLinkedList<>();
        if (!adjacencyList.containsKey(start)) return resultado;

        Queue<T> cola                    = new Queue<>();
        HashTable<T, Boolean> visitados  = new HashTable<>();

        cola.enqueue(start);
        visitados.put(start, true);

        while (!cola.isEmpty()) {
            T actual = cola.dequeue();
            resultado.addLast(actual);

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
     * Retorna SimpleLinkedList con los nodos visitados en orden de profundidad.
     */
    public SimpleLinkedList<T> depthFirstSearch(T start) {
        SimpleLinkedList<T> resultado    = new SimpleLinkedList<>();
        if (!adjacencyList.containsKey(start)) return resultado;

        Stack<T> pila                    = new Stack<>();
        HashTable<T, Boolean> visitados  = new HashTable<>();

        pila.push(start);

        while (!pila.isEmpty()) {
            T actual = pila.pop();
            if (visitados.containsKey(actual)) continue;
            visitados.put(actual, true);
            resultado.addLast(actual);

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
    // Información del grafo
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
        for (SimpleLinkedList<T> vecinos : adjacencyList) {
            vecinos.removeAll();
        }
    }

    public void print() {
        for (SimpleLinkedList<T> vecinos : adjacencyList) {
            System.out.print("[ ");
            for (T v : vecinos) System.out.print(v + " ");
            System.out.println("]");
        }
    }
}