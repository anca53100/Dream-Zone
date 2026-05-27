package com.example.dreamzone.service;

import com.example.dreamzone.model.Carrito;
import com.example.dreamzone.model.ItemCarrito;
import com.example.dreamzone.repository.CarritoRepository;
import com.example.dreamzone.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // ─── Clave de búsqueda ───────────────────────────────────────────────────
    private Optional<Carrito> buscarCarrito(String usuarioId, String sessionId) {
        if (usuarioId != null) {
            return carritoRepository.findByUsuarioId(usuarioId);
        }
        return carritoRepository.findBySessionId(sessionId);
    }

    // ─── Obtener o crear ─────────────────────────────────────────────────────
    public Carrito obtenerCarrito(String sessionId) {
        return obtenerCarrito(null, sessionId);
    }

    public Carrito obtenerCarrito(String usuarioId, String sessionId) {
        return buscarCarrito(usuarioId, sessionId).orElseGet(() -> {
            Carrito nuevo = new Carrito();
            nuevo.setSessionId(sessionId);
            nuevo.setUsuarioId(usuarioId);
            return carritoRepository.save(nuevo);
        });
    }

    // ─── Vincular sesión anónima al usuario al hacer login ───────────────────
    public void vincularCarritoAlUsuario(String sessionId, String usuarioId) {
        Optional<Carrito> carritoAnonimo = carritoRepository.findBySessionId(sessionId);
        Optional<Carrito> carritoUsuario = carritoRepository.findByUsuarioId(usuarioId);

        if (carritoAnonimo.isEmpty()) {
            if (carritoUsuario.isEmpty()) {
                Carrito nuevo = new Carrito();
                nuevo.setSessionId(sessionId);
                nuevo.setUsuarioId(usuarioId);
                carritoRepository.save(nuevo);
            } else {
                Carrito cu = carritoUsuario.get();
                cu.setSessionId(sessionId);
                carritoRepository.save(cu);
            }
            return;
        }

        Carrito anonimo = carritoAnonimo.get();
        if (carritoUsuario.isEmpty()) {
            anonimo.setUsuarioId(usuarioId);
            carritoRepository.save(anonimo);
        } else {
            Carrito usuario = carritoUsuario.get();
            for (ItemCarrito itemAnonimo : anonimo.getItems()) {
                // Deduplica por (idProducto + talla)
                Optional<ItemCarrito> existente = usuario.getItems().stream()
                        .filter(i -> i.getIdProducto().equals(itemAnonimo.getIdProducto())
                                && Objects.equals(i.getTalla(), itemAnonimo.getTalla()))
                        .findFirst();
                if (existente.isPresent()) {
                    existente.get().setCantidad(
                            existente.get().getCantidad() + itemAnonimo.getCantidad());
                } else {
                    usuario.getItems().add(itemAnonimo);
                }
            }
            usuario.setSessionId(sessionId);
            usuario.setFechaActualizacion(LocalDateTime.now());
            carritoRepository.save(usuario);
            carritoRepository.delete(anonimo);
        }
    }

    // ─── Agregar producto ────────────────────────────────────────────────────
    public Carrito agregarItem(String sessionId, ItemCarrito nuevoItem) {
        return agregarItem(null, sessionId, nuevoItem);
    }

    public Carrito agregarItem(String usuarioId, String sessionId, ItemCarrito nuevoItem) {
        Carrito carrito = obtenerCarrito(usuarioId, sessionId);

        // ── Calcular stock disponible para este producto/talla ──────────────
        final String talla = nuevoItem.getTalla();
        int stockDisponible = productoRepository.findById(nuevoItem.getIdProducto())
                .map(p -> {
                    if (talla != null && !talla.isBlank() && p.getStockPorTalla() != null) {
                        return p.getStockPorTalla().getOrDefault(talla, 0);
                    }
                    return p.getStock();
                })
                .orElse(0);

        // ── Deduplicar por (idProducto + talla) ────────────────────────────
        Optional<ItemCarrito> existente = carrito.getItems().stream()
                .filter(i -> i.getIdProducto().equals(nuevoItem.getIdProducto())
                        && Objects.equals(i.getTalla(), talla))
                .findFirst();

        int yaEnCarrito  = existente.map(ItemCarrito::getCantidad).orElse(0);
        int puedoAgregar = Math.max(0, stockDisponible - yaEnCarrito);
        int cantidadReal = Math.min(nuevoItem.getCantidad(), puedoAgregar);

        if (cantidadReal <= 0) {
            return carrito; // Sin stock adicional disponible
        }

        if (existente.isPresent()) {
            existente.get().setCantidad(existente.get().getCantidad() + cantidadReal);
        } else {
            nuevoItem.setCantidad(cantidadReal);
            carrito.getItems().add(nuevoItem);
        }

        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    // ─── Eliminar producto (talla puede ser null para productos sin talla) ────
    public Carrito eliminarProducto(String sessionId, String idProducto) {
        return eliminarProducto(null, sessionId, idProducto, null);
    }

    public Carrito eliminarProducto(String usuarioId, String sessionId,
                                    String idProducto, String talla) {
        Carrito carrito = obtenerCarrito(usuarioId, sessionId);
        String tallaNorm = (talla != null && !talla.isBlank()) ? talla : null;
        carrito.getItems().removeIf(i ->
                i.getIdProducto().equals(idProducto)
                        && Objects.equals(i.getTalla(), tallaNorm));
        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    // ─── Actualizar cantidad (solo para productos sin talla) ─────────────────
    public Carrito actualizarCantidad(String sessionId, String idProducto, int cantidad) {
        return actualizarCantidad(null, sessionId, idProducto, cantidad);
    }

    public Carrito actualizarCantidad(String usuarioId, String sessionId,
                                      String idProducto, int cantidad) {
        if (cantidad <= 0) {
            return eliminarProducto(usuarioId, sessionId, idProducto, null);
        }
        Carrito carrito = obtenerCarrito(usuarioId, sessionId);
        carrito.getItems().stream()
                .filter(i -> i.getIdProducto().equals(idProducto) && i.getTalla() == null)
                .findFirst()
                .ifPresent(i -> i.setCantidad(cantidad));
        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    // ─── Vaciar carrito ──────────────────────────────────────────────────────
    public Carrito vaciarCarrito(String sessionId) {
        return vaciarCarrito(null, sessionId);
    }

    public Carrito vaciarCarrito(String usuarioId, String sessionId) {
        Carrito carrito = obtenerCarrito(usuarioId, sessionId);
        carrito.getItems().clear();
        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    // ─── Calcular total ──────────────────────────────────────────────────────
    public double calcularTotal(String sessionId) {
        return obtenerCarrito(sessionId).calcularTotal();
    }

    // ─── Finalizar compra ────────────────────────────────────────────────────
    public boolean finalizarCompra(String sessionId, List<String> idsSeleccionados) {
        return finalizarCompra(null, sessionId, idsSeleccionados);
    }

    public boolean finalizarCompra(String usuarioId, String sessionId,
                                   List<String> idsSeleccionados) {
        Carrito carrito = obtenerCarrito(usuarioId, sessionId);

        List<ItemCarrito> itemsAProcesar = (idsSeleccionados != null && !idsSeleccionados.isEmpty())
                ? carrito.getItems().stream()
                  .filter(i -> idsSeleccionados.contains(i.getIdProducto()))
                  .toList()
                : List.copyOf(carrito.getItems());

        for (ItemCarrito item : itemsAProcesar) {
            productoRepository.findById(item.getIdProducto()).ifPresent(producto -> {
                // Descontar stock total
                int nuevoStock = Math.max(0, producto.getStock() - item.getCantidad());
                producto.setStock(nuevoStock);

                // Descontar de stockPorTalla si aplica
                if (item.getTalla() != null && !item.getTalla().isBlank()
                        && producto.getStockPorTalla() != null) {
                    int tallaActual = producto.getStockPorTalla()
                            .getOrDefault(item.getTalla(), 0);
                    producto.getStockPorTalla().put(
                            item.getTalla(),
                            Math.max(0, tallaActual - item.getCantidad()));
                }
                productoRepository.save(producto);
            });
        }

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
