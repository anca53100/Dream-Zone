package com.example.dreamzone.service;

import com.example.dreamzone.model.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.example.dreamzone.repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    public List<Producto> obtenerNovedades() {
        return productoRepository.findByEsNuevoTrue();
    }

    public List<Producto> obtenerOfertas() {
        return productoRepository.findByEsOfertaTrue();
    }

    public Optional<Producto> obtenerPorId(String id) {
        return productoRepository.findById(id);
    }

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    public void eliminar(String id) {
        productoRepository.deleteById(id);
    }

    public List<Producto> buscar(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public List<Producto> obtenerPorSerie(String serie) {
        return productoRepository.findBySerie(serie);
    }

    // ProductoService.java — agregar
    public Map<String, Long> contarPorCategoria() {
        return obtenerTodos().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getCategoria() != null ? p.getCategoria() : "Sin categoría",
                        java.util.stream.Collectors.counting()
                ));
    }
}