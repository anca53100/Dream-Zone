package com.example.dreamzone.controller;

import com.example.dreamzone.model.Producto;
import com.example.dreamzone.service.CarritoService;
import com.example.dreamzone.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/")
    public String inicio(Model model, HttpSession session) {

        // Criterio: activos, primero los marcados "Nuevo", luego mayor precio → máx 5
        model.addAttribute("productos",
                productoService.obtenerTodos().stream()
                        .filter(p -> p.getEstado() == null || "Activo".equalsIgnoreCase(p.getEstado()))
                        .sorted(java.util.Comparator
                                .<Producto, Integer>comparing(p -> p.isEsNuevo() ? 0 : 1)
                                .thenComparing(java.util.Comparator.comparingDouble(Producto::getPrecio).reversed()))
                        .limit(5)
                        .toList());
        model.addAttribute("categoryCounts", productoService.contarPorCategoria());

        int totalItems = carritoService.obtenerCarrito(session.getId()).contarItems();
        model.addAttribute("totalItems", totalItems);

        return "index";
    }

    @GetMapping("/producto/{id}")
    public String detalle(@PathVariable String id, Model model) {
        productoService.obtenerPorId(id).ifPresent(p -> model.addAttribute("producto", p));
        return "producto-detalle";
    }
}