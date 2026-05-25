package com.example.dreamzone.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarrito {

    private String idProducto;
    private String nombre;
    private String serie;
    private String categoria;
    private double precio;
    private int cantidad;

    /** Calculado dinámicamente; no se persiste como campo extra. */
    public double getSubtotal() {
        return precio * cantidad;
    }
}
