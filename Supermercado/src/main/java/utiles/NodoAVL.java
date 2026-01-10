/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;


public class NodoAVL<T extends Comparable<T>> {
    private T dato;
    private NodoAVL<T> izquierda;
    private NodoAVL<T> derecha;
    private int altura;

    public NodoAVL(T dato) {
        this.dato = dato;
        this.izquierda = null;
        this.derecha = null;
        this.altura = 1;
    }

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public NodoAVL<T> getIzquierda() {
        return izquierda;
    }

    public void setIzquierda(NodoAVL<T> izquierda) {
        this.izquierda = izquierda;
    }

    public NodoAVL<T> getDerecha() {
        return derecha;
    }

    public void setDerecha(NodoAVL<T> derecha) {
        this.derecha = derecha;
    }

    public int getAltura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }
    
}
