package co.edu.uniquindio.poo.PropTech.structures;

public class Node<T> {

	private Node<T> next;
	private T data;
	
	
	/**
	 * Constructor de la clase Node
	 * @param data Elemento que se guarda en el Node
	 */
	public Node(T data) {
		this.data = data;
	}
	
	
	/**
	 * Constructor de la clase Node
	 * @param data Elemento que se guarda en el Node
	 * @param next Enlace al siguiente Node
	 */
	public Node(T data, Node<T> next) {
		super();
		this.data = data;
		this.next = next;
	}
	

	//Metodos get y set de la clase Node
	
	public Node<T> getNext() {
		return next;
	}


	public void setNext(Node<T> next) {
		this.next = next;
	}


	public T getData() {
		return data;
	}


	public void setData(T data) {
		this.data = data;
	}
}
