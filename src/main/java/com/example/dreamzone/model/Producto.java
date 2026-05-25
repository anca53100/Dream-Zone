package com.example.dreamzone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

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
    private String talla;
    private String material;
    private String imagen;

}