/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;

/**
 *
 * @author USER
 */
public class ArbolAVL<T extends Comparable<T>> {
    private NodoAVL<T> raiz;

    public ArbolAVL() {
        this.raiz = null;
    }

    private int altura(NodoAVL<T> N) {
        return (N == null) ? 0 : N.getAltura();
    }

    private int max(int a, int b) {
        return (a > b) ? a : b;
    }

    private int getBalance(NodoAVL<T> N) {
        return (N == null) ? 0 : altura(N.getIzquierda()) - altura(N.getDerecha());
    }

    private NodoAVL<T> rotacionDerecha(NodoAVL<T> y) {
        NodoAVL<T> x = y.getIzquierda();
        NodoAVL<T> T2 = x.getDerecha();

        x.setDerecha(y);
        y.setIzquierda(T2);

        y.setAltura(max(altura(y.getIzquierda()), altura(y.getDerecha())) + 1);
        x.setAltura(max(altura(x.getIzquierda()), altura(x.getDerecha())) + 1);

        return x;
    }

    private NodoAVL<T> rotacionIzquierda(NodoAVL<T> x) {
        NodoAVL<T> y = x.getDerecha();
        NodoAVL<T> T2 = y.getIzquierda();

        y.setIzquierda(x);
        x.setDerecha(T2);

        x.setAltura(max(altura(x.getIzquierda()), altura(x.getDerecha())) + 1);
        y.setAltura(max(altura(y.getIzquierda()), altura(y.getDerecha())) + 1);

        return y;
    }

    public void insertar(T dato) {
        raiz = insertarRec(raiz, dato);
    }

    private NodoAVL<T> insertarRec(NodoAVL<T> nodo, T dato) {
        if (nodo == null) return new NodoAVL<>(dato);

        int comparacion = dato.compareTo(nodo.getDato());

        if (comparacion < 0)
            nodo.setIzquierda(insertarRec(nodo.getIzquierda(), dato));
        else if (comparacion > 0)
            nodo.setDerecha(insertarRec(nodo.getDerecha(), dato));
        else
            return nodo;

        nodo.setAltura(1 + max(altura(nodo.getIzquierda()), altura(nodo.getDerecha())));

        int balance = getBalance(nodo);

        if (balance > 1 && dato.compareTo(nodo.getIzquierda().getDato()) < 0)
            return rotacionDerecha(nodo);

        if (balance < -1 && dato.compareTo(nodo.getDerecha().getDato()) > 0)
            return rotacionIzquierda(nodo);

        if (balance > 1 && dato.compareTo(nodo.getIzquierda().getDato()) > 0) {
            nodo.setIzquierda(rotacionIzquierda(nodo.getIzquierda()));
            return rotacionDerecha(nodo);
        }

        if (balance < -1 && dato.compareTo(nodo.getDerecha().getDato()) < 0) {
            nodo.setDerecha(rotacionDerecha(nodo.getDerecha()));
            return rotacionIzquierda(nodo);
        }

        return nodo;
    }

    public T buscar(T datoBusqueda) {
        NodoAVL<T> res = buscarRec(raiz, datoBusqueda);
        return (res != null) ? res.getDato() : null;
    }

    private NodoAVL<T> buscarRec(NodoAVL<T> nodo, T datoBusqueda) {
        if (nodo == null) return null;
        
        int comparacion = datoBusqueda.compareTo(nodo.getDato());

        if (comparacion == 0) return nodo;
        
        if (comparacion < 0) return buscarRec(nodo.getIzquierda(), datoBusqueda);
        
        return buscarRec(nodo.getDerecha(), datoBusqueda);
    }
    
    // NUEVO: Método eliminar
    public boolean eliminar(T dato) {
        if (buscar(dato) == null) {
            return false;
        }
        raiz = eliminarRec(raiz, dato);
        return true;
    }

    private NodoAVL<T> eliminarRec(NodoAVL<T> raiz, T dato) {
        if (raiz == null) return raiz;

        int comparacion = dato.compareTo(raiz.getDato());

        if (comparacion < 0) {
            raiz.setIzquierda(eliminarRec(raiz.getIzquierda(), dato));
        } else if (comparacion > 0) {
            raiz.setDerecha(eliminarRec(raiz.getDerecha(), dato));
        } else {
            // Nodo con uno o ningún hijo
            if ((raiz.getIzquierda() == null) || (raiz.getDerecha() == null)) {
                NodoAVL<T> temp = (raiz.getIzquierda() != null) ? 
                                  raiz.getIzquierda() : raiz.getDerecha();

                // Sin hijo
                if (temp == null) {
                    temp = raiz;
                    raiz = null;
                } else {
                    // Un hijo
                    raiz = temp;
                }
            } else {
                // Nodo con dos hijos
                NodoAVL<T> temp = minValueNode(raiz.getDerecha());
                raiz.setDato(temp.getDato());
                raiz.setDerecha(eliminarRec(raiz.getDerecha(), temp.getDato()));
            }
        }

        if (raiz == null) return raiz;

        // Actualizar altura
        raiz.setAltura(1 + max(altura(raiz.getIzquierda()), altura(raiz.getDerecha())));

        // Balancear
        int balance = getBalance(raiz);

        // Rotaciones según balance
        if (balance > 1 && getBalance(raiz.getIzquierda()) >= 0)
            return rotacionDerecha(raiz);

        if (balance > 1 && getBalance(raiz.getIzquierda()) < 0) {
            raiz.setIzquierda(rotacionIzquierda(raiz.getIzquierda()));
            return rotacionDerecha(raiz);
        }

        if (balance < -1 && getBalance(raiz.getDerecha()) <= 0)
            return rotacionIzquierda(raiz);

        if (balance < -1 && getBalance(raiz.getDerecha()) > 0) {
            raiz.setDerecha(rotacionDerecha(raiz.getDerecha()));
            return rotacionIzquierda(raiz);
        }

        return raiz;
    }

    private NodoAVL<T> minValueNode(NodoAVL<T> nodo) {
        NodoAVL<T> actual = nodo;
        while (actual.getIzquierda() != null)
            actual = actual.getIzquierda();
        return actual;
    }
    
    public void limpiar() { 
        raiz = null; 
    }
    
    // NUEVO: Método para obtener tamaño
    public int size() {
        return sizeRec(raiz);
    }
    
    private int sizeRec(NodoAVL<T> nodo) {
        if (nodo == null) return 0;
        return 1 + sizeRec(nodo.getIzquierda()) + sizeRec(nodo.getDerecha());
    }
    
    // NUEVO: Método para verificar si está vacío
    public boolean isEmpty() {
        return raiz == null;
    }
}