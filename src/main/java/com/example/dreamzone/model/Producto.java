package com.example.dreamzone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "productos")
public class Producto {
    @Id
    private String id;

    private String nombre;
    private String descripcion;
    private String categoria;    // "Figuras", "Mangas", "Ropa", etc.
    private String marca;
    private double precio;
    private int stock;
    private String estado;       // "Activo", "Inactivo"
    private boolean esNuevo;
    private boolean esOferta;
    private double precioAnterior;
    private String serie;          // "Jujutsu Kaisen", "One Piece", etc.

    // Detalles extra
    private String talla;    // Legado para productos sin stockPorTalla
    private String material;
    private String imagen;

    /**
     * Stock por talla — solo para "Camisas Importadas" / "Camisas Nacionales".
     * Clave: talla (XS, S, M, L, XL, XXL). Valor: unidades disponibles.
     * El campo {@code stock} siempre contiene la suma total.
     */
    private Map<String, Integer> stockPorTalla;

    /** Categorías que manejan tallas. */
    public static boolean tieneTallas(String categoria) {
        return "Camisas Importadas".equals(categoria) || "Camisas Nacionales".equals(categoria);
    }

}