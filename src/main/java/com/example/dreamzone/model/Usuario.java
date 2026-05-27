package com.example.dreamzone.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String id;

    private String nombre;
    private String email;
    private String password;      // BCrypt
    private String rol;           // "ROLE_USER" o "ROLE_ADMIN"
    private List<String> favoritos; // IDs de productos
    private String tokenRecuperacion;
    private LocalDateTime tokenExpiracion;
// con su getter y setter
}
