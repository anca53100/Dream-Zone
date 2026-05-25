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
            Model model,
            HttpSession session) {

        List<Producto> productos;

        if ("nuevo".equals(filter)) {
            productos = productoService.obtenerNovedades();
        } else if ("oferta".equals(filter)) {
            productos = productoService.obtenerOfertas();
        } else if (buscar != null && !buscar.isBlank()) {
            productos = productoService.buscar(buscar);
        } else {
            productos = productoService.obtenerTodos().stream()
                    .filter(p -> p.getEstado() == null || "Activo".equals(p.getEstado()))
                    .toList();
        }

        int totalItems = carritoService.obtenerCarrito(session.getId()).contarItems();

        model.addAttribute("productos",   productos);
        model.addAttribute("filter",      filter);
        model.addAttribute("buscar",      buscar);
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

