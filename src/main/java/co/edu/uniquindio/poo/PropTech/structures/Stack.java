package co.edu.uniquindio.poo.PropTech.structures;

import lombok.Getter;

/**
 *
 * @param <T>
 */
@Getter
public class Stack<T> {

    /**
     * -- GETTER --
     *
     */
    private Node<T> top;
    /**
     * -- GETTER --
     *
     */
    private int size;

	/**
	 * Checks if the Stack is empty.
	 * @return true if it is empty
	 */
	public boolean isEmpty() {
		return top == null;
	}

	/**
	 * Inserts an element into the Stack - push.
	 * @param data element to store in the stack
	 */
	public void push(T data) {

		Node<T> node = new Node<>(data);
		node.setNext(top);
		top = node;

		size++;
	}

	/**
	 * Returns and removes the element at the top of the Stack - pop.
	 * @return top element
	 */
	public T pop() {

		if (isEmpty()) {
			throw new RuntimeException("The Stack is empty");
		}

		T data = top.getData();
		top = top.getNext();
		size--;

		return data;
	}

	/**
	 * Completely clears the Stack.
	 */
	public void clearStack() {
		top = null;
		size = 0;
	}

	/**
	 * @return the top element - peek
	 */
	public T peek() {
		return top.getData();
	}

    /**
	 * Adds another Stack to this Stack.
	 * @param stack stack to add
	 */
	public void add(Stack<T> stack) {

		Stack<T> clone = stack.clone();
		Stack<T> aux = new Stack<>();

		while (!clone.isEmpty()) {
			aux.push(clone.pop());
		}

		while (!aux.isEmpty()) {
			push(aux.pop());
		}
	}

	/**
	 * Prints the Stack to the console.
	 */
	public void print() {
		Node<T> aux = top;

		while (aux != null) {
			System.out.print(aux.getData() + "\t");
			aux = aux.getNext();
		}

		System.out.println();
	}

	@Override
	protected Stack<T> clone() {

		Stack<T> finalStack = new Stack<>();
		Node<T> topNode = null;

		for (Node<T> aux = top; aux != null; aux = aux.getNext()) {

			Node<T> newNode = new Node<>(aux.getData());

			if (finalStack.isEmpty()) {
				finalStack.top = newNode;
				topNode = newNode;
			} else {
				finalStack.top.setNext(newNode);
				finalStack.top = newNode;
			}

			finalStack.size++;
		}

		finalStack.top = topNode;

		return finalStack;
	}
}

