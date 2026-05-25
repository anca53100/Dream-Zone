package com.example.dreamzone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "pedidos")
public class Pedido {

    @Id
    private String id;

    private String sessionId;
    private String usuarioId;

    private List<ItemCarrito> items;

    private double total;
    private String estado; // "PENDIENTE", "COMPLETADO", "CANCELADO"

    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
