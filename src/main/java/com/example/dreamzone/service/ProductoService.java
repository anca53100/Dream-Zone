package com.example.dreamzone.service;

import com.example.dreamzone.model.Producto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio de Productos — trabaja en memoria para el MVP.
 * Cuando integren BD, solo cambia este archivo; el controller
 * y las vistas no necesitan modificaciones.
 */
@Service
public class ProductoService {

    private final List<Producto> catalogo = new ArrayList<>();
    private final AtomicLong contadorId   = new AtomicLong(1);

    public ProductoService() {
        // Datos de prueba
        catalogo.add(new Producto(contadorId.getAndIncrement(),
                "DZ Snapback Negro", "Gorras", 85000.0, 20, "SKU-001",
                "Gorra snapback negra con logo DZ bordado"));
        catalogo.add(new Producto(contadorId.getAndIncrement(),
                "Camiseta Logo DZ", "Ropa", 65000.0, 15, "SKU-002",
                "Camiseta de algodón con logo Dream Zone"));
        catalogo.add(new Producto(contadorId.getAndIncrement(),
                "Hoodie DZ Azul", "Ropa", 140000.0, 4, "SKU-003",
                "Hoodie azul con logo DZ estampado"));
        catalogo.add(new Producto(contadorId.getAndIncrement(),
                "Mochila DZ", "Accesorios", 110000.0, 8, "SKU-004",
                "Mochila resistente con parche DZ"));
        catalogo.add(new Producto(contadorId.getAndIncrement(),
                "Guantes DZ", "Accesorios", 45000.0, 0, "SKU-005",
                "Guantes táctiles con logo bordado"));
    }

    // ── LISTAR ─────────────────────────────────────────────────
    public List<Producto> listarTodos() {
        return new ArrayList<>(catalogo);
    }

    // ── BUSCAR POR ID ──────────────────────────────────────────
    public Optional<Producto> buscarPorId(Long id) {
        return catalogo.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    // ── AGREGAR ────────────────────────────────────────────────
    public void agregar(Producto producto) {
        producto.setId(contadorId.getAndIncrement());
        // Generar SKU automático si viene vacío
        if (producto.getSku() == null || producto.getSku().isBlank()) {
            producto.setSku("SKU-" + String.format("%03d", producto.getId()));
        }
        catalogo.add(producto);
    }

    // ── ELIMINAR ───────────────────────────────────────────────
    public boolean eliminar(Long id) {
        return catalogo.removeIf(p -> p.getId().equals(id));
    }

    // ── ESTADÍSTICAS para el dashboard ────────────────────────
    public long totalProductos()   { return catalogo.size(); }

    public long stockBajo() {
        return catalogo.stream()
                .filter(p -> p.getStock() != null && p.getStock() > 0 && p.getStock() <= 5)
                .count();
    }

    public long sinStock() {
        return catalogo.stream()
                .filter(p -> p.getStock() == null || p.getStock() == 0)
                .count();
    }

    public long activos() {
        return catalogo.stream()
                .filter(p -> p.getStock() != null && p.getStock() > 0)
                .count();
    }
}