package com.example.dreamzone.controller;

import com.example.dreamzone.model.Producto;
import com.example.dreamzone.service.CarritoService;
import com.example.dreamzone.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequestMapping("/tienda")
public class TiendaController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CarritoService carritoService;

    @GetMapping
    public String verTienda(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String cat,
            @RequestParam(required = false) String coleccion,
            Model model,
            HttpSession session) {

        List<Producto> productos;

        // Helper: solo productos Activos (o sin estado definido)
        java.util.function.Predicate<Producto> soloActivos =
                p -> p.getEstado() == null || "Activo".equalsIgnoreCase(p.getEstado());

        if ("nuevo".equals(filter)) {
            productos = productoService.obtenerNovedades().stream()
                    .filter(soloActivos).toList();
        } else if ("oferta".equals(filter)) {
            productos = productoService.obtenerOfertas().stream()
                    .filter(soloActivos).toList();
        } else if (buscar != null && !buscar.isBlank()) {
            productos = productoService.buscar(buscar).stream()
                    .filter(soloActivos).toList();
        } else if (cat != null && !cat.isBlank()) {
            productos = productoService.obtenerPorCategoria(cat).stream()
                    .filter(soloActivos).toList();
        } else if (coleccion != null && !coleccion.isBlank()) {
            productos = productoService.obtenerPorSerie(coleccion).stream()
                    .filter(soloActivos).toList();
        } else {
            productos = productoService.obtenerTodos().stream()
                    .filter(soloActivos).toList();
        }

        int totalItems = carritoService.obtenerCarrito(session.getId()).contarItems();

        model.addAttribute("productos",   productos);
        model.addAttribute("filter",      filter);
        model.addAttribute("buscar",      buscar);
        model.addAttribute("cat",         cat);
        model.addAttribute("coleccion",   coleccion);
        model.addAttribute("totalItems",  totalItems);
        return "tienda";
    }
    @GetMapping("/{id}")
    public String verDetalle(@PathVariable String id, Model model, HttpSession session) {
        Producto producto = productoService.obtenerPorId(id).orElse(null);
        if (producto == null) return "redirect:/tienda";

        boolean esAdmin = session.getAttribute("usuarioLogueado") != null &&
                "ROLE_ADMIN".equals(((com.example.dreamzone.model.Usuario) session.getAttribute("usuarioLogueado")).getRol());

        model.addAttribute("producto", producto);
        model.addAttribute("esAdmin", esAdmin);
        return "producto-detalle";
    }
}

