package com.example.dreamzone.controller;

import com.example.dreamzone.model.Carrito;
import com.example.dreamzone.model.ItemCarrito;
import com.example.dreamzone.model.Usuario;
import com.example.dreamzone.repository.ProductoRepository;
import com.example.dreamzone.service.CarritoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private ProductoRepository productoRepository;

    // ─── Helper: obtener usuarioId de la sesión ──────────────────────────────

    private String getUsuarioId(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        return usuario != null ? usuario.getId() : null;
    }

    // ─── Vista ───────────────────────────────────────────────────────────────

    @GetMapping
    public String verCarrito(HttpSession session, Model model) {
        String usuarioId = getUsuarioId(session);
        Carrito carrito  = carritoService.obtenerCarrito(usuarioId, session.getId());

        int totalItems = carrito.contarItems();
        String subtitleText = totalItems > 0
                ? totalItems + (totalItems == 1 ? " artículo" : " artículos")
                : "";

        model.addAttribute("carrito",      carrito);
        model.addAttribute("total",        carrito.calcularTotal());
        model.addAttribute("totalItems",   totalItems);
        model.addAttribute("subtitleText", subtitleText);
        return "carrito";
    }

    // ─── REST: consulta ──────────────────────────────────────────────────────

    @GetMapping("/datos")
    @ResponseBody
    public ResponseEntity<Carrito> obtenerDatos(HttpSession session) {
        String usuarioId = getUsuarioId(session);
        return ResponseEntity.ok(carritoService.obtenerCarrito(usuarioId, session.getId()));
    }

    // ─── REST: agregar (llamada JSON) ────────────────────────────────────────

    @PostMapping("/agregar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregar(
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        String usuarioId  = getUsuarioId(session);
        String idProducto = (String) body.get("idProducto");
        int cantidad = body.containsKey("cantidad")
                ? Integer.parseInt(body.get("cantidad").toString()) : 1;
        String talla = body.containsKey("talla") ? (String) body.get("talla") : null;

        ItemCarrito item = new ItemCarrito();
        item.setIdProducto(idProducto);
        item.setCantidad(cantidad);
        item.setTalla((talla != null && !talla.isBlank()) ? talla : null);

        productoRepository.findById(idProducto).ifPresentOrElse(
                producto -> {
                    item.setNombre(producto.getNombre());
                    item.setSerie(producto.getSerie()         != null ? producto.getSerie()     : "");
                    item.setCategoria(producto.getCategoria() != null ? producto.getCategoria() : "");
                    item.setImagen(producto.getImagen()       != null ? producto.getImagen()    : "");
                    item.setPrecio(producto.getPrecio());
                },
                () -> {
                    item.setNombre(   body.getOrDefault("nombre",    "Producto").toString());
                    item.setSerie(    body.getOrDefault("serie",     "").toString());
                    item.setCategoria(body.getOrDefault("categoria", "").toString());
                    item.setImagen(   body.getOrDefault("imagen",    "").toString());
                    item.setPrecio(Double.parseDouble(body.getOrDefault("precio", "0").toString()));
                }
        );

        Carrito carrito = carritoService.agregarItem(usuarioId, session.getId(), item);
        return ResponseEntity.ok(Map.of(
                "ok",         true,
                "totalItems", carrito.contarItems(),
                "total",      carrito.calcularTotal()
        ));
    }

    // ─── REST: eliminar ──────────────────────────────────────────────────────

    @PostMapping("/eliminar/{idProducto}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminar(
            @PathVariable String idProducto,
            @RequestParam(required = false) String talla,
            HttpSession session) {

        String usuarioId = getUsuarioId(session);
        Carrito carrito  = carritoService.eliminarProducto(usuarioId, session.getId(),
                idProducto, talla);
        return ResponseEntity.ok(Map.of(
                "ok",         true,
                "totalItems", carrito.contarItems(),
                "total",      carrito.calcularTotal()
        ));
    }

    // ─── REST: actualizar cantidad ───────────────────────────────────────────

    @PostMapping("/actualizar/{idProducto}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable String idProducto,
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        String usuarioId = getUsuarioId(session);
        int cantidad     = Integer.parseInt(body.get("cantidad").toString());
        Carrito carrito  = carritoService.actualizarCantidad(usuarioId, session.getId(), idProducto, cantidad);

        double nuevoSubtotal = carrito.getItems().stream()
                .filter(i -> i.getIdProducto().equals(idProducto))
                .mapToDouble(i -> i.getPrecio() * i.getCantidad())
                .sum();

        return ResponseEntity.ok(Map.of(
                "ok",            true,
                "totalItems",    carrito.contarItems(),
                "total",         carrito.calcularTotal(),
                "nuevoSubtotal", nuevoSubtotal
        ));
    }

    // ─── REST: vaciar ────────────────────────────────────────────────────────

    @PostMapping("/vaciar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> vaciar(HttpSession session) {
        String usuarioId = getUsuarioId(session);
        carritoService.vaciarCarrito(usuarioId, session.getId());
        return ResponseEntity.ok(Map.of("ok", true, "totalItems", 0, "total", 0.0));
    }

    // ─── REST: finalizar compra ──────────────────────────────────────────────

    @PostMapping("/finalizar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> finalizar(
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        String usuarioId = getUsuarioId(session);
        @SuppressWarnings("unchecked")
        List<String> ids = body.containsKey("ids") ? (List<String>) body.get("ids") : null;

        boolean ok = carritoService.finalizarCompra(usuarioId, session.getId(), ids);
        return ResponseEntity.ok(Map.of(
                "ok",      ok,
                "mensaje", "¡Compra realizada con éxito! El stock fue descontado."
        ));
    }

    @PostMapping("/agregar/{id}")
    public String agregarDesdeVista(@PathVariable String id,
                                    HttpSession session) {

        String usuarioId = getUsuarioId(session); // Respetar usuario logueado

        ItemCarrito item = new ItemCarrito();
        item.setIdProducto(id);
        item.setCantidad(1);

        productoRepository.findById(id).ifPresent(producto -> {
            item.setNombre(producto.getNombre());
            item.setSerie(producto.getSerie()         != null ? producto.getSerie()     : "");
            item.setCategoria(producto.getCategoria() != null ? producto.getCategoria() : "");
            item.setImagen(producto.getImagen()       != null ? producto.getImagen()    : "");
            item.setPrecio(producto.getPrecio());
        });

        carritoService.agregarItem(usuarioId, session.getId(), item);

        return "redirect:/carrito";
    }
}
