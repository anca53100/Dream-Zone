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
    private String imagen;
    private double precio;
    private int cantidad;

    /**
     * Talla seleccionada (XS/S/M/L/XL/XXL).
     * null o vacío para productos sin talla (gorras, tulas, etc.).
     */
    private String talla;

    /** Calculado dinámicamente; no se persiste como campo extra. */
    public double getSubtotal() {
        return precio * cantidad;
    }

    /** Clave única por línea del carrito: combina producto + talla. */
    public String lineKey() {
        return (talla != null && !talla.isBlank())
                ? idProducto + "|" + talla
                : idProducto;
    }
}
