package com.example.dreamzone.repository;

import com.example.dreamzone.model.Carrito;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CarritoRepository extends MongoRepository<Carrito, String> {
    Optional<Carrito> findBySessionId(String sessionId);
}
