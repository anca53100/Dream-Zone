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

    // ─── Clave de búsqueda ───────────────────────────────────────────────────
    // Si hay usuarioId lo usamos; si no, usamos sessionId (usuario anónimo)

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
    /**
     * Llamar desde AuthController justo después del login exitoso.
     * Fusiona el carrito anónimo (sessionId) con el del usuario logueado.
     * Si el usuario ya tiene carrito, suma las cantidades; si no, le asigna
     * el carrito anónimo directamente.
     */
    public void vincularCarritoAlUsuario(String sessionId, String usuarioId) {
        Optional<Carrito> carritoAnonimo  = carritoRepository.findBySessionId(sessionId);
        Optional<Carrito> carritoUsuario  = carritoRepository.findByUsuarioId(usuarioId);

        if (carritoAnonimo.isEmpty()) {
            // Nada que fusionar: si el usuario no tiene carrito, crear uno vacío
            if (carritoUsuario.isEmpty()) {
                Carrito nuevo = new Carrito();
                nuevo.setSessionId(sessionId);
                nuevo.setUsuarioId(usuarioId);
                carritoRepository.save(nuevo);
            } else {
                // Actualizar sessionId del carrito existente para que coincida
                Carrito cu = carritoUsuario.get();
                cu.setSessionId(sessionId);
                carritoRepository.save(cu);
            }
            return;
        }

        Carrito anonimo = carritoAnonimo.get();

        if (carritoUsuario.isEmpty()) {
            // El usuario no tenía carrito: asignarle el anónimo
            anonimo.setUsuarioId(usuarioId);
            carritoRepository.save(anonimo);
        } else {
            // Fusionar: sumar items del carrito anónimo al del usuario
            Carrito usuario = carritoUsuario.get();
            for (ItemCarrito itemAnonimo : anonimo.getItems()) {
                Optional<ItemCarrito> existente = usuario.getItems().stream()
                        .filter(i -> i.getIdProducto().equals(itemAnonimo.getIdProducto()))
                        .findFirst();
                if (existente.isPresent()) {
                    existente.get().setCantidad(
                            existente.get().getCantidad() + itemAnonimo.getCantidad()
                    );
                } else {
                    usuario.getItems().add(itemAnonimo);
                }
            }
            usuario.setSessionId(sessionId);
            usuario.setFechaActualizacion(LocalDateTime.now());
            carritoRepository.save(usuario);
            // Eliminar el carrito anónimo ya fusionado
            carritoRepository.delete(anonimo);
        }
    }

    // ─── Agregar producto ────────────────────────────────────────────────────

    public Carrito agregarItem(String sessionId, ItemCarrito nuevoItem) {
        return agregarItem(null, sessionId, nuevoItem);
    }

    public Carrito agregarItem(String usuarioId, String sessionId, ItemCarrito nuevoItem) {
        Carrito carrito = obtenerCarrito(usuarioId, sessionId);

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
        return eliminarProducto(null, sessionId, idProducto);
    }

    public Carrito eliminarProducto(String usuarioId, String sessionId, String idProducto) {
        Carrito carrito = obtenerCarrito(usuarioId, sessionId);
        carrito.getItems().removeIf(i -> i.getIdProducto().equals(idProducto));
        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    // ─── Actualizar cantidad ─────────────────────────────────────────────────

    public Carrito actualizarCantidad(String sessionId, String idProducto, int cantidad) {
        return actualizarCantidad(null, sessionId, idProducto, cantidad);
    }

    public Carrito actualizarCantidad(String usuarioId, String sessionId, String idProducto, int cantidad) {
        if (cantidad <= 0) {
            return eliminarProducto(usuarioId, sessionId, idProducto);
        }
        Carrito carrito = obtenerCarrito(usuarioId, sessionId);
        carrito.getItems().stream()
                .filter(i -> i.getIdProducto().equals(idProducto))
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

    public boolean finalizarCompra(String usuarioId, String sessionId, List<String> idsSeleccionados) {
        Carrito carrito = obtenerCarrito(usuarioId, sessionId);

        List<ItemCarrito> itemsAProcesar = (idsSeleccionados != null && !idsSeleccionados.isEmpty())
                ? carrito.getItems().stream()
                  .filter(i -> idsSeleccionados.contains(i.getIdProducto()))
                  .toList()
                : List.copyOf(carrito.getItems());

        for (ItemCarrito item : itemsAProcesar) {
            productoRepository.findById(item.getIdProducto()).ifPresent(producto -> {
                int nuevoStock = Math.max(0, producto.getStock() - item.getCantidad());
                producto.setStock(nuevoStock);
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