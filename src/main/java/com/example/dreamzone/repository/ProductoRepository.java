package com.example.dreamzone.repository;

import com.example.dreamzone.model.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductoRepository extends MongoRepository<Producto, String> {
    List<Producto> findByCategoria(String categoria);
    List<Producto> findByEstado(String estado);
    List<Producto> findByEsNuevoTrue();
    List<Producto> findByEsOfertaTrue();
    List<Producto> findBySerie(String serie);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByPrecioBetween(double min, double max);
}