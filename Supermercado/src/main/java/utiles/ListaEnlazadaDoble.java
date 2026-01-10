/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;
    import javax.swing.DefaultListModel;

public class ListaEnlazadaDoble<T extends Comparable<T>> {
    protected NodoEnDoble<T> primero;
    protected NodoEnDoble<T> ultimo;

    public ListaEnlazadaDoble() {
        primero = null;
        ultimo = null;
    }

    public NodoEnDoble<T> getPrimero() {
        return primero;
    }

    public void setPrimero(NodoEnDoble<T> primero) {
        this.primero = primero;
    }

    public NodoEnDoble<T> getUltimo() {
        return ultimo;
    }

    public void setUltimo(NodoEnDoble<T> ultimo) {
        this.ultimo = ultimo;
    }

    public boolean esVacia() {
        return primero == null && ultimo == null;
    }


    public void insertaAlFinal(T valor) {
        NodoEnDoble<T> nuevo = new NodoEnDoble<>(valor);
        if (primero == null) {
            primero = nuevo;
            ultimo = nuevo;
        } else {
            nuevo.setAnt(ultimo);
            ultimo.setSgte(nuevo);
            ultimo = nuevo;
        }
    }


    public void insertaAlInicio(T valor) {
        NodoEnDoble<T> nuevo = new NodoEnDoble<>(valor);
        if (primero == null) {
            primero = nuevo;
            ultimo = nuevo;
        } else {
            nuevo.setSgte(primero);
            primero.setAnt(nuevo);
            primero = nuevo;
        }
    }


    public int contar() {
        int c = 0;
        NodoEnDoble<T> p = primero;
        while (p != null) {
            c++;
            p = p.getSgte();
        }
        return c;
    }


    public void mostrarHaciaAdelante(DefaultListModel<T> modelo) {
        modelo.removeAllElements();
        NodoEnDoble<T> p = primero;
        while (p != null) {
            modelo.addElement(p.getInfo());
            p = p.getSgte();
        }
    }


    public void mostrarHaciaAtras(DefaultListModel<T> modelo) {
        modelo.removeAllElements();
        NodoEnDoble<T> p = ultimo;
        while (p != null) {
            modelo.addElement(p.getInfo());
            p = p.getAnt();
        }
    }


    public NodoEnDoble<T> buscar(T dato) {
        NodoEnDoble<T> p = primero;
        while (p != null) {
            if (p.getInfo().equals(dato))
                return p;
            p = p.getSgte();
        }
        return null;
    }

    public boolean eliminar(T dato) {
        if (esVacia()) return false;

        if (primero.getInfo().equals(dato)) {
            if (primero == ultimo) {
                primero = ultimo = null;
            } else {
                primero = primero.getSgte();
                primero.setAnt(null);
            }
            return true;
        }

        NodoEnDoble<T> p = primero.getSgte();
        while (p != null && !p.getInfo().equals(dato)) {
            p = p.getSgte();
        }

        if (p == null) return false;

        if (p == ultimo) {
            ultimo = ultimo.getAnt();
            ultimo.setSgte(null);
        } else {
            p.getAnt().setSgte(p.getSgte());
            p.getSgte().setAnt(p.getAnt());
        }
        return true;
    }


    public void ordenar() {
        if (primero == null) return;

        NodoEnDoble<T> p = primero;
        while (p.getSgte() != null) {
            NodoEnDoble<T> q = p.getSgte();
            while (q != null) {
                if (p.getInfo().compareTo(q.getInfo()) > 0) {
                    T aux = p.getInfo();
                    p.setInfo(q.getInfo());
                    q.setInfo(aux);
                }
                q = q.getSgte();
            }
            p = p.getSgte();
        }
    }
    
   public boolean modificar(T datoViejo, T datoNuevo) {
    NodoEnDoble<T> nodo = buscar(datoViejo);
    if (nodo != null) {
        nodo.setInfo(datoNuevo);
        return true;
    }
    return false;
}

}

