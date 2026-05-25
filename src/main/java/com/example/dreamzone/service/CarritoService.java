package com.example.dreamzone.service;

import com.example.dreamzone.model.Carrito;
import com.example.dreamzone.model.ItemCarrito;
import com.example.dreamzone.repository.CarritoRepository;
import com.example.dreamzone.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // ─── Obtener o crear ─────────────────────────────────────────────────────

    /**
     * Devuelve el carrito vinculado a la sesión actual.
     * Si no existe, lo crea y persiste.
     */
    public Carrito obtenerCarrito(String sessionId) {
        return carritoRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Carrito nuevo = new Carrito();
                    nuevo.setSessionId(sessionId);
                    return carritoRepository.save(nuevo);
                });
    }

    // ─── Agregar producto ────────────────────────────────────────────────────

    /**
     * Agrega (o incrementa) un item en el carrito.
     * El item ya viene enriquecido desde el controller; aquí solo persiste.
     */
    public Carrito agregarItem(String sessionId, ItemCarrito nuevoItem) {
        Carrito carrito = obtenerCarrito(sessionId);

        Optional<ItemCarrito> existente = carrito.getItems().stream()
                .filter(i -> i.getIdProducto().equals(nuevoItem.getIdProducto()))
                .findFirst();

        if (existente.isPresent()) {
            existente.get().setCantidad(existente.get().getCantidad() + nuevoItem.getCantidad());
        } else {
            carrito.getItems().add(nuevoItem);
        }

        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    // ─── Eliminar producto ───────────────────────────────────────────────────

    public Carrito eliminarProducto(String sessionId, String idProducto) {
        Carrito carrito = obtenerCarrito(sessionId);
        carrito.getItems().removeIf(i -> i.getIdProducto().equals(idProducto));
        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    // ─── Actualizar cantidad ─────────────────────────────────────────────────

    public Carrito actualizarCantidad(String sessionId, String idProducto, int cantidad) {
        if (cantidad <= 0) {
            return eliminarProducto(sessionId, idProducto);
        }
        Carrito carrito = obtenerCarrito(sessionId);
        carrito.getItems().stream()
                .filter(i -> i.getIdProducto().equals(idProducto))
                .findFirst()
                .ifPresent(i -> i.setCantidad(cantidad));
        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    // ─── Vaciar carrito ──────────────────────────────────────────────────────

    public Carrito vaciarCarrito(String sessionId) {
        Carrito carrito = obtenerCarrito(sessionId);
        carrito.getItems().clear();
        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    // ─── Calcular total ──────────────────────────────────────────────────────

    public double calcularTotal(String sessionId) {
        return obtenerCarrito(sessionId).calcularTotal();
    }

    // ─── Finalizar compra ────────────────────────────────────────────────────

    /**
     * Descuenta stock de los productos seleccionados y los elimina del carrito.
     *
     * @param idsSeleccionados IDs de los productos a procesar.
     *                         Si es nulo o vacío, procesa todo el carrito.
     */
    public boolean finalizarCompra(String sessionId, List<String> idsSeleccionados) {
        Carrito carrito = obtenerCarrito(sessionId);

        // Determinar qué items procesar
        List<ItemCarrito> itemsAProcesar = (idsSeleccionados != null && !idsSeleccionados.isEmpty())
                ? carrito.getItems().stream()
                    .filter(i -> idsSeleccionados.contains(i.getIdProducto()))
                    .toList()
                : List.copyOf(carrito.getItems());

        // Descontar stock en MongoDB para cada producto encontrado
        for (ItemCarrito item : itemsAProcesar) {
            productoRepository.findById(item.getIdProducto()).ifPresent(producto -> {
                int nuevoStock = Math.max(0, producto.getStock() - item.getCantidad());
                producto.setStock(nuevoStock);
                productoRepository.save(producto);
            });
        }

        // Eliminar los items procesados del carrito
        if (idsSeleccionados != null && !idsSeleccionados.isEmpty()) {
            carrito.getItems().removeIf(i -> idsSeleccionados.contains(i.getIdProducto()));
        } else {
            carrito.getItems().clear();
        }

        carrito.setFechaActualizacion(LocalDateTime.now());
        carritoRepository.save(carrito);
        return true;
    }
}
