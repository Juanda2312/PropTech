package co.edu.uniquindio.poo.PropTech.structures;

import lombok.Getter;
import lombok.Setter;

@Getter
public class AVLTree<T extends Comparable<T>> {

    private AVLNode<T> root;
    private int size;

    public void insert(T data) {
        root = insert(root, data);
    }

    private AVLNode<T> insert(AVLNode<T> node, T data) {
        if (node == null) {
            size++;
            return new AVLNode<>(data);
        }

        if (data.compareTo(node.getData()) < 0) {
            node.setLeft(insert(node.getLeft(), data));
        } else if (data.compareTo(node.getData()) > 0) {
            node.setRight(insert(node.getRight(), data));
        } else {
            return node;
        }

        updateHeight(node);
        return balance(node);
    }

    public void remove(T data) {
        root = remove(root, data);
    }

    private AVLNode<T> remove(AVLNode<T> node, T data) {
        if (node == null) {
            return null;
        }

        if (data.compareTo(node.getData()) < 0) {
            node.setLeft(remove(node.getLeft(), data));
        } else if (data.compareTo(node.getData()) > 0) {
            node.setRight(remove(node.getRight(), data));
        } else {
            if (node.getLeft() == null || node.getRight() == null) {
                AVLNode<T> temp = node.getLeft() != null ? node.getLeft() : node.getRight();

                if (temp == null) {
                    node = null;
                } else {
                    node = temp;
                }

                size--;
            } else {
                AVLNode<T> successor = getMinNode(node.getRight());
                node.setData(successor.getData());
                node.setRight(remove(node.getRight(), successor.getData()));
            }
        }

        if (node == null) {
            return null;
        }

        updateHeight(node);
        return balance(node);
    }

    public boolean contains(T data) {
        return contains(root, data);
    }

    private boolean contains(AVLNode<T> node, T data) {
        if (node == null) {
            return false;
        }

        if (data.compareTo(node.getData()) == 0) {
            return true;
        }

        if (data.compareTo(node.getData()) < 0) {
            return contains(node.getLeft(), data);
        }

        return contains(node.getRight(), data);
    }

    public T getMin() {
        if (isEmpty()) {
            throw new RuntimeException("The AVL Tree is empty");
        }

        return getMinNode(root).getData();
    }

    private AVLNode<T> getMinNode(AVLNode<T> node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }

        return node;
    }

    public T getMax() {
        if (isEmpty()) {
            throw new RuntimeException("The AVL Tree is empty");
        }

        AVLNode<T> current = root;

        while (current.getRight() != null) {
            current = current.getRight();
        }

        return current.getData();
    }

    private AVLNode<T> balance(AVLNode<T> node) {
        int balanceFactor = getBalanceFactor(node);

        if (balanceFactor > 1) {
            if (getBalanceFactor(node.getLeft()) < 0) {
                node.setLeft(rotateLeft(node.getLeft()));
            }

            return rotateRight(node);
        }

        if (balanceFactor < -1) {
            if (getBalanceFactor(node.getRight()) > 0) {
                node.setRight(rotateRight(node.getRight()));
            }

            return rotateLeft(node);
        }

        return node;
    }

    private AVLNode<T> rotateRight(AVLNode<T> y) {
        AVLNode<T> x = y.getLeft();
        AVLNode<T> temp = x.getRight();

        x.setRight(y);
        y.setLeft(temp);

        updateHeight(y);
        updateHeight(x);

        return x;
    }

    private AVLNode<T> rotateLeft(AVLNode<T> x) {
        AVLNode<T> y = x.getRight();
        AVLNode<T> temp = y.getLeft();

        y.setLeft(x);
        x.setRight(temp);

        updateHeight(x);
        updateHeight(y);

        return y;
    }

    private void updateHeight(AVLNode<T> node) {
        node.setHeight(1 + Math.max(getHeight(node.getLeft()), getHeight(node.getRight())));
    }

    private int getHeight(AVLNode<T> node) {
        return node == null ? 0 : node.getHeight();
    }

    private int getBalanceFactor(AVLNode<T> node) {
        return node == null ? 0 : getHeight(node.getLeft()) - getHeight(node.getRight());
    }

    public void printInOrder() {
        printInOrder(root);
        System.out.println();
    }

    private void printInOrder(AVLNode<T> node) {
        if (node != null) {
            printInOrder(node.getLeft());
            System.out.print(node.getData() + "\t");
            printInOrder(node.getRight());
        }
    }

    public void printPreOrder() {
        printPreOrder(root);
        System.out.println();
    }

    private void printPreOrder(AVLNode<T> node) {
        if (node != null) {
            System.out.print(node.getData() + "\t");
            printPreOrder(node.getLeft());
            printPreOrder(node.getRight());
        }
    }

    public void printPostOrder() {
        printPostOrder(root);
        System.out.println();
    }

    private void printPostOrder(AVLNode<T> node) {
        if (node != null) {
            printPostOrder(node.getLeft());
            printPostOrder(node.getRight());
            System.out.print(node.getData() + "\t");
        }
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    @Setter
    @Getter
    public static class AVLNode<T> {

        private T data;
        private AVLNode<T> left;
        private AVLNode<T> right;
        private int height;

        public AVLNode(T data) {
            this.data = data;
            this.height = 1;
        }

    }
}
