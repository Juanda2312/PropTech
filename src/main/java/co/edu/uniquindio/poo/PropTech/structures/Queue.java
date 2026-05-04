package co.edu.uniquindio.poo.PropTech.structures;

import lombok.Getter;

/**
 *
 * @param <T>
 */
public class Queue<T> {

	public Node<T> firstNode, lastNode;
    /**
     * -- GETTER --
     *
     * @return the size
     */
    @Getter
    public int size;

	/**
	 * Adds an element to the Queue.
	 * @param data element to store in the Queue
	 */
	public void enqueue(T data) {

		Node<T> node = new Node<>(data);

		if (isEmpty()) {
			firstNode = lastNode = node;
		} else {
			lastNode.setNext(node);
			lastNode = node;
		}

		size++;
	}

	/**
	 * Returns and removes the element at the front of the Queue.
	 * @return first element of the Queue
	 */
	public T dequeue() {

		if (isEmpty()) {
			throw new RuntimeException("The Queue is empty");
		}

		T data = firstNode.getData();
		firstNode = firstNode.getNext();

		if (firstNode == null) {
			lastNode = null;
		}

		size--;
		return data;
	}

	/**
	 * Checks if the Queue is empty.
	 * @return true if it is empty
	 */
	public boolean isEmpty() {
		return firstNode == null;
	}

	/**
	 * Completely clears the Queue.
	 */
	public void clearQueue() {
		firstNode = lastNode = null;
		size = 0;
	}

	/**
	 * @return the first node
	 */
	public Node<T> getFirst() {
		return firstNode;
	}

	/**
	 * @return the last node
	 */
	public Node<T> getLast() {
		return lastNode;
	}

    /**
	 * Checks if another Queue is identical to this one.
	 * @param queue Queue to compare
	 * @return true if they are equal
	 */
	public boolean areIdentical(Queue<T> queue) {

		Queue<T> clone1 = clone();
		Queue<T> clone2 = queue.clone();

		if (clone1.getSize() == clone2.getSize()) {

			while (!clone1.isEmpty()) {
				if (!clone1.dequeue().equals(clone2.dequeue())) {
					return false;
				}
			}

		} else {
			return false;
		}

		return true;
	}

	/**
	 * Prints the Queue to the console.
	 */
	public void print() {
		Node<T> aux = firstNode;

		while (aux != null) {
			System.out.print(aux.getData() + "\t");
			aux = aux.getNext();
		}

		System.out.println();
	}

	@Override
	protected Queue<T> clone() {

		Queue<T> newQueue = new Queue<>();
		Node<T> aux = firstNode;

		while (aux != null) {
			newQueue.enqueue(aux.getData());
			aux = aux.getNext();
		}

		return newQueue;
	}

	public void reverse() {
		if (isEmpty()) {
			return;
		} else {
			T data = dequeue();
			reverse();
			enqueue(data);
		}
	}
}