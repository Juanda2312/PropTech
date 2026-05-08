package co.edu.uniquindio.poo.PropTech.structures;

import lombok.Getter;

/**
 * Cola de prioridad propia implementada con un heap máximo.
 * Se usa para: visitas VIP/urgentes, alertas por vencimiento cercano,
 * clientes con alta intención de cierre e inmuebles con mayor demanda.
 * Justificación: permite extraer siempre el elemento de mayor prioridad
 * en O(log n), más eficiente que una cola FIFO para estos casos.
 */
public class PriorityQueue<T extends Comparable<T>> {

    @Getter
    private int size;
    private Object[] heap;
    private static final int DEFAULT_CAPACITY = 16;

    public PriorityQueue() {
        heap = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public void enqueue(T data) {
        if (size >= heap.length - 1) resize();
        heap[++size] = data;
        swimUp(size);
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) throw new RuntimeException("PriorityQueue is empty");
        T max = (T) heap[1];
        swap(1, size--);
        heap[size + 1] = null;
        sinkDown(1);
        return max;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new RuntimeException("PriorityQueue is empty");
        return (T) heap[1];
    }

    public boolean isEmpty() { return size == 0; }

    @SuppressWarnings("unchecked")
    private void swimUp(int k) {
        while (k > 1 && ((T) heap[k / 2]).compareTo((T) heap[k]) < 0) {
            swap(k, k / 2);
            k = k / 2;
        }
    }

    @SuppressWarnings("unchecked")
    private void sinkDown(int k) {
        while (2 * k <= size) {
            int j = 2 * k;
            if (j < size && ((T) heap[j]).compareTo((T) heap[j + 1]) < 0) j++;
            if (((T) heap[k]).compareTo((T) heap[j]) >= 0) break;
            swap(k, j);
            k = j;
        }
    }

    private void swap(int i, int j) {
        Object tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }

    private void resize() {
        Object[] newHeap = new Object[heap.length * 2];
        System.arraycopy(heap, 0, newHeap, 0, heap.length);
        heap = newHeap;
    }
}