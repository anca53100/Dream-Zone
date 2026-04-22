package com.example.dreamzone.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Producto {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @NotBlank(message = "La subcategoría es obligatoria")
    private String subcategoria;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    private String sku;
    private String descripcion;
    private String badge;

    // ── Constructores ──────────────────────────────────
    public Producto() {}

    public Producto(Long id, String nombre, String categoria, String subcategoria,
                    Double precio, Integer stock, String sku, String descripcion) {
        this.id          = id;
        this.nombre      = nombre;
        this.categoria   = categoria;
        this.subcategoria = subcategoria;
        this.precio      = precio;
        this.stock       = stock;
        this.sku         = sku;
        this.descripcion = descripcion;
    }

    // Helper: estado de stock para la vista
    public String getEstadoStock() {
        if (stock == null || stock == 0) return "Sin stock";
        if (stock <= 5)                  return "Stock bajo";
        return "En stock";
    }

    // ── Getters y Setters ───────────────────────────────
    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }

    public String getNombre()            { return nombre; }
    public void setNombre(String n)      { this.nombre = n; }

    public String getCategoria()         { return categoria; }
    public void setCategoria(String c)   { this.categoria = c; }

    public String getSubcategoria()          { return subcategoria; }
    public void setSubcategoria(String s)    { this.subcategoria = s; }

    public Double getPrecio()            { return precio; }
    public void setPrecio(Double p)      { this.precio = p; }

    public Integer getStock()            { return stock; }
    public void setStock(Integer s)      { this.stock = s; }

    public String getSku()               { return sku; }
    public void setSku(String sku)       { this.sku = sku; }

    public String getDescripcion()       { return descripcion; }
    public void setDescripcion(String d) { this.descripcion = d; }

    public String getBadge()             { return badge; }
    public void setBadge(String badge)   { this.badge = badge; }

    // Constructor para los productos del catálogo (sin sku/descripcion/stock)
    public Producto(Long id, String nombre, String categoria, String subcategoria,
                    Double precio, String badge) {
        this.id           = id;
        this.nombre       = nombre;
        this.categoria    = categoria;
        this.subcategoria = subcategoria;
        this.precio       = precio;
        this.badge        = badge;
    }
}