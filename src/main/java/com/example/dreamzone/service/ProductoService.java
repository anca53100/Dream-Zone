package com.example.dreamzone.service;

import com.example.dreamzone.model.Producto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final List<Producto> catalogo = new ArrayList<>();
    private final AtomicLong contadorId   = new AtomicLong(16);

    public ProductoService() {
        catalogo.add(new Producto(1L,  "Taza",       "Otros",      "Taza",      25000.0, "NEW"));
        catalogo.add(new Producto(2L,  "Camiseta",   "Camisas",    "Camisa",    25000.0, "NEW"));
        catalogo.add(new Producto(3L,  "Billetera",  "Accesorios", "Accesorio", 25000.0, "NEW"));
        catalogo.add(new Producto(4L,  "Figura",     "Otros",      "Figura",    25000.0, "NEW"));
        catalogo.add(new Producto(5L,  "Gorra",      "Accesorios", "Accesorio", 25000.0, "NEW"));
        catalogo.add(new Producto(6L,  "Taza 2",     "Otros",      "Taza",      25000.0, null));
        catalogo.add(new Producto(7L,  "Saco",       "Sacos",      "Saco",      25000.0, null));
        catalogo.add(new Producto(8L,  "Pin",        "Accesorios", "Accesorio", 25000.0, null));
        catalogo.add(new Producto(9L,  "Figura 2",   "Otros",      "Figura",    25000.0, null));
        catalogo.add(new Producto(10L, "Aretes",     "Accesorios", "Accesorio", 25000.0, null));
        catalogo.add(new Producto(11L, "Camisa",     "Camisas",    "Camisa",    25000.0, null));
        catalogo.add(new Producto(12L, "Saco largo", "Sacos",      "Saco",      25000.0, null));
        catalogo.add(new Producto(13L, "Collar",     "Accesorios", "Accesorio", 25000.0, null));
        catalogo.add(new Producto(14L, "Figura 3",   "Otros",      "Figura",    25000.0, null));
        catalogo.add(new Producto(15L, "Camisa 2",   "Camisas",    "Camisa",    25000.0, null));
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
        if (producto.getSku() == null || producto.getSku().isBlank()) {
            producto.setSku("SKU-" + String.format("%03d", producto.getId()));
        }
        catalogo.add(producto);
    }

    // ── ELIMINAR ───────────────────────────────────────────────
    public boolean eliminar(Long id) {
        return catalogo.removeIf(p -> p.getId().equals(id));
    }

    // ── ACTUALIZAR ─────────────────────────────────────────────
    public boolean actualizar(Producto editado) {
        for (int i = 0; i < catalogo.size(); i++) {
            if (catalogo.get(i).getId().equals(editado.getId())) {
                if (editado.getSku() == null || editado.getSku().isBlank()) {
                    editado.setSku(catalogo.get(i).getSku());
                }
                catalogo.set(i, editado);
                return true;
            }
        }
        return false;
    }

    // ── ESTADÍSTICAS ───────────────────────────────────────────
    public long totalProductos() { return catalogo.size(); }

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

    // ── MÉTODOS NUEVOS ─────────────────────────────────────────
    public List<Producto> getTodos() {
        return listarTodos();
    }

    public List<Producto> getDestacados() {
        return catalogo.stream()
                .filter(p -> p.getBadge() != null)
                .collect(Collectors.toList());
    }

    public List<Producto> getPorCategoria(String categoria) {
        if (categoria == null || categoria.equals("Todos")) return listarTodos();
        return catalogo.stream()
                .filter(p -> p.getCategoria().equalsIgnoreCase(categoria))
                .collect(Collectors.toList());
    }

    public List<Producto> buscar(String query) {
        String q = query.toLowerCase();
        return catalogo.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(q)
                        || p.getCategoria().toLowerCase().contains(q)
                        || p.getSubcategoria().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }
}