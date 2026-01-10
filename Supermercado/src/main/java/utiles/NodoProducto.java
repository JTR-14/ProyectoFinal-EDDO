/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utiles;

import modelo.Producto;

public class NodoProducto {
    private Producto producto;
    private NodoProducto izquierda;
    private NodoProducto derecha;
    private int altura;

    public NodoProducto(Producto producto) {
        this.producto = producto;
        this.izquierda = null;
        this.derecha = null;
        this.altura = 1;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public NodoProducto getIzquierda() {
        return izquierda;
    }

    public void setIzquierda(NodoProducto izquierda) {
        this.izquierda = izquierda;
    }

    public NodoProducto getDerecha() {
        return derecha;
    }

    public void setDerecha(NodoProducto derecha) {
        this.derecha = derecha;
    }

    public int getAltura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }
}
