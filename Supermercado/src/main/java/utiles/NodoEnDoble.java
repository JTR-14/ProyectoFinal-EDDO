/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;

/**
 *
 * @author USUARIO
 */


public class NodoEnDoble<T> {
    private T info;
    private NodoEnDoble<T> sgte;
    private NodoEnDoble<T> ant;

    public NodoEnDoble(T info) {
        this.info = info;
        this.sgte = null;
        this.ant = null;
    }

    public NodoEnDoble(T info, NodoEnDoble<T> sgte, NodoEnDoble<T> ant) {
        this.info = info;
        this.sgte = sgte;
        this.ant = ant;
    }

    public T getInfo() {
        return info;
    }

    public void setInfo(T info) {
        this.info = info;
    }

    public NodoEnDoble<T> getSgte() {
        return sgte;
    }

    public void setSgte(NodoEnDoble<T> sgte) {
        this.sgte = sgte;
    }

    public NodoEnDoble<T> getAnt() {
        return ant;
    }

    public void setAnt(NodoEnDoble<T> ant) {
        this.ant = ant;
    }
}
