/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;
import modelo.Producto;

public class ArbolProductosAVL {
    
    private NodoProducto raiz;

    public ArbolProductosAVL() {
        this.raiz = null;
    }

    private int altura(NodoProducto N) {
        if (N == null) return 0;
        return N.getAltura();
    }

    private int max(int a, int b) {
        return (a > b) ? a : b;
    }

    private NodoProducto rotacionDerecha(NodoProducto y) {
        NodoProducto x = y.getIzquierda();
        NodoProducto T2 = x.getDerecha();

        x.setDerecha(y);
        y.setIzquierda(T2);

        y.setAltura(max(altura(y.getIzquierda()), altura(y.getDerecha())) + 1);
        x.setAltura(max(altura(x.getIzquierda()), altura(x.getDerecha())) + 1);

        return x;
    }

    private NodoProducto rotacionIzquierda(NodoProducto x) {
        NodoProducto y = x.getDerecha();
        NodoProducto T2 = y.getIzquierda();

        y.setIzquierda(x);
        x.setDerecha(T2);

        x.setAltura(max(altura(x.getIzquierda()), altura(x.getDerecha())) + 1);
        y.setAltura(max(altura(y.getIzquierda()), altura(y.getDerecha())) + 1);

        return y;
    }

    private int getBalance(NodoProducto N) {
        if (N == null) return 0;
        return altura(N.getIzquierda()) - altura(N.getDerecha());
    }

    public void insertar(Producto producto) {
        raiz = insertarRec(raiz, producto);
    }

    private NodoProducto insertarRec(NodoProducto nodo, Producto producto) {
        if (nodo == null) {
            return new NodoProducto(producto);
        }

        String codigoNuevo = producto.getCodigo();
        String codigoActual = nodo.getProducto().getCodigo();

        if (codigoNuevo.compareTo(codigoActual) < 0) {
            nodo.setIzquierda(insertarRec(nodo.getIzquierda(), producto));
        } else if (codigoNuevo.compareTo(codigoActual) > 0) {
            nodo.setDerecha(insertarRec(nodo.getDerecha(), producto));
        } else {
            return nodo;
        }

        nodo.setAltura(1 + max(altura(nodo.getIzquierda()), altura(nodo.getDerecha())));

        int balance = getBalance(nodo);

        if (balance > 1 && codigoNuevo.compareTo(nodo.getIzquierda().getProducto().getCodigo()) < 0)
            return rotacionDerecha(nodo);

        if (balance < -1 && codigoNuevo.compareTo(nodo.getDerecha().getProducto().getCodigo()) > 0)
            return rotacionIzquierda(nodo);

        if (balance > 1 && codigoNuevo.compareTo(nodo.getIzquierda().getProducto().getCodigo()) > 0) {
            nodo.setIzquierda(rotacionIzquierda(nodo.getIzquierda()));
            return rotacionDerecha(nodo);
        }

        if (balance < -1 && codigoNuevo.compareTo(nodo.getDerecha().getProducto().getCodigo()) < 0) {
            nodo.setDerecha(rotacionDerecha(nodo.getDerecha()));
            return rotacionIzquierda(nodo);
        }

        return nodo;
    }

    public Producto buscar(String codigoBarras) {
        NodoProducto resultado = buscarRec(raiz, codigoBarras);
        if (resultado != null) {
            return resultado.getProducto();
        } else {
            return null;
        }
    }

    private NodoProducto buscarRec(NodoProducto nodo, String codigo) {
        if (nodo == null || nodo.getProducto().getCodigo().equals(codigo)) {
            return nodo;
        }

        if (codigo.compareTo(nodo.getProducto().getCodigo()) < 0) {
            return buscarRec(nodo.getIzquierda(), codigo);
        }

        return buscarRec(nodo.getDerecha(), codigo);
    }
    
    public boolean esVacio() {
        return raiz == null;
    }
}

