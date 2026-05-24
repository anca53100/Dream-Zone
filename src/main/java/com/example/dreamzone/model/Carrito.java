package com.example.dreamzone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "carritos")
public class Carrito {

    @Id
    private String id;

    /** Identificador de sesión HTTP — clave principal de búsqueda. */
    @Indexed
    private String sessionId;

    /** Para uso futuro cuando se integre autenticación. */
    private String usuarioId;

    private List<ItemCarrito> items = new ArrayList<>();

    private LocalDateTime fechaCreacion   = LocalDateTime.now();
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    // ─── Métodos de utilidad ─────────────────────────────────────────────────

    /** Suma de todos los subtotales. */
    public double calcularTotal() {
        return items.stream().mapToDouble(ItemCarrito::getSubtotal).sum();
    }

    /** Cantidad total de unidades en el carrito (todas las líneas). */
    public int contarItems() {
        return items.stream().mapToInt(ItemCarrito::getCantidad).sum();
    }

    /** Número de líneas (productos distintos). */
    public int contarLineas() {
        return items.size();
    }
}
