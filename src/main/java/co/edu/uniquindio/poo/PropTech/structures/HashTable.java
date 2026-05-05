package co.edu.uniquindio.poo.PropTech.structures;

import lombok.Getter;
import lombok.Setter;

public class HashTable<K, V> {

    private HashNode<K, V>[] table;
    @Getter
    private int size;
    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    public HashTable() {
        table = new HashNode[DEFAULT_CAPACITY];
        size = 0;
    }

    public void put(K key, V value) {
        if ((double) size / table.length >= LOAD_FACTOR) {
            resize();
        }

        int index = getIndex(key);
        HashNode<K, V> current = table[index];

        while (current != null) {
            if (current.getKey().equals(key)) {
                current.setValue(value);
                return;
            }
            current = current.getNext();
        }

        HashNode<K, V> newNode = new HashNode<>(key, value);
        newNode.setNext(table[index]);
        table[index] = newNode;
        size++;
    }

    public V get(K key) {
        int index = getIndex(key);
        HashNode<K, V> current = table[index];

        while (current != null) {
            if (current.getKey().equals(key)) {
                return current.getValue();
            }
            current = current.getNext();
        }

        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public V remove(K key) {
        int index = getIndex(key);
        HashNode<K, V> current = table[index];
        HashNode<K, V> previous = null;

        while (current != null) {
            if (current.getKey().equals(key)) {
                if (previous == null) {
                    table[index] = current.getNext();
                } else {
                    previous.setNext(current.getNext());
                }

                size--;
                return current.getValue();
            }

            previous = current;
            current = current.getNext();
        }

        return null;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        table = new HashNode[DEFAULT_CAPACITY];
        size = 0;
    }

    private int getIndex(K key) {
        return Math.abs(key.hashCode()) % table.length;
    }

    private void resize() {
        HashNode<K, V>[] oldTable = table;
        table = new HashNode[oldTable.length * 2];
        size = 0;

        for (HashNode<K, V> node : oldTable) {
            while (node != null) {
                put(node.getKey(), node.getValue());
                node = node.getNext();
            }
        }
    }

    public void print() {
        for (int i = 0; i < table.length; i++) {
            System.out.print(i + ": ");

            HashNode<K, V> current = table[i];

            while (current != null) {
                System.out.print("[" + current.getKey() + " = " + current.getValue() + "] ");
                current = current.getNext();
            }

            System.out.println();
        }
    }

    @Getter
    private static class HashNode<K, V> {

        private K key;
        @Setter
        private V value;
        @Setter
        private HashNode<K, V> next;

        public HashNode(K key, V value) {
            this.key = key;
            this.value = value;
        }

    }
}
