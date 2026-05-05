package co.edu.uniquindio.poo.PropTech.structures;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class Graph<T> {

    private Map<T, ArrayList<T>> adjacencyList;
    private boolean directed;

    public Graph(boolean directed) {
        this.directed = directed;
        this.adjacencyList = new HashMap<>();
    }

    public void addVertex(T vertex) {
        adjacencyList.putIfAbsent(vertex, new ArrayList<>());
    }

    public void addEdge(T origin, T destination) {
        addVertex(origin);
        addVertex(destination);

        adjacencyList.get(origin).add(destination);

        if (!directed) {
            adjacencyList.get(destination).add(origin);
        }
    }

    public void removeVertex(T vertex) {
        if (!adjacencyList.containsKey(vertex)) {
            return;
        }

        adjacencyList.remove(vertex);

        for (ArrayList<T> neighbors : adjacencyList.values()) {
            neighbors.remove(vertex);
        }
    }

    public void removeEdge(T origin, T destination) {
        if (!adjacencyList.containsKey(origin)) {
            return;
        }

        adjacencyList.get(origin).remove(destination);

        if (!directed && adjacencyList.containsKey(destination)) {
            adjacencyList.get(destination).remove(origin);
        }
    }

    public boolean containsVertex(T vertex) {
        return adjacencyList.containsKey(vertex);
    }

    public boolean containsEdge(T origin, T destination) {
        if (!adjacencyList.containsKey(origin)) {
            return false;
        }

        return adjacencyList.get(origin).contains(destination);
    }

    public ArrayList<T> getNeighbors(T vertex) {
        if (!adjacencyList.containsKey(vertex)) {
            return new ArrayList<>();
        }

        return adjacencyList.get(vertex);
    }

    public void breadthFirstSearch(T start) {
        if (!adjacencyList.containsKey(start)) {
            return;
        }

        Queue<T> queue = new Queue<>();
        Set<T> visited = new HashSet<>();

        queue.enqueue(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            T current = queue.dequeue();
            System.out.print(current + "\t");

            for (T neighbor : adjacencyList.get(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.enqueue(neighbor);
                }
            }
        }

        System.out.println();
    }

    public void depthFirstSearch(T start) {
        if (!adjacencyList.containsKey(start)) {
            return;
        }

        Set<T> visited = new HashSet<>();
        depthFirstSearch(start, visited);
        System.out.println();
    }

    private void depthFirstSearch(T vertex, Set<T> visited) {
        visited.add(vertex);
        System.out.print(vertex + "\t");

        for (T neighbor : adjacencyList.get(vertex)) {
            if (!visited.contains(neighbor)) {
                depthFirstSearch(neighbor, visited);
            }
        }
    }

    public int getVertexCount() {
        return adjacencyList.size();
    }

    public int getEdgeCount() {
        int count = 0;

        for (ArrayList<T> neighbors : adjacencyList.values()) {
            count += neighbors.size();
        }

        if (!directed) {
            count /= 2;
        }

        return count;
    }

    public boolean isEmpty() {
        return adjacencyList.isEmpty();
    }

    public void clear() {
        adjacencyList.clear();
    }

    public void print() {
        for (T vertex : adjacencyList.keySet()) {
            System.out.print(vertex + " -> ");

            for (T neighbor : adjacencyList.get(vertex)) {
                System.out.print(neighbor + " ");
            }

            System.out.println();
        }
    }

}
