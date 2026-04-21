package com.example.dreamzone.controller;

import com.example.dreamzone.model.Producto;
import com.example.dreamzone.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductoService productoService;

    public AdminController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // ── GET /admin  →  redirige a productos ───────────────────
    @GetMapping
    public String index() {
        return "redirect:/admin/productos";
    }

    // ── GET /admin/productos  →  lista de productos ───────────
    @GetMapping("/productos")
    public String listarProductos(Model model,
                                  @RequestParam(required = false) String buscar,
                                  @RequestParam(required = false) String mensaje) {

        var lista = productoService.listarTodos();

        // Función de búsqueda (filtro en memoria)
        if (buscar != null && !buscar.isBlank()) {
            String termino = buscar.toLowerCase();
            lista = lista.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(termino)
                            || p.getCategoria().toLowerCase().contains(termino)
                            || (p.getSku() != null && p.getSku().toLowerCase().contains(termino)))
                    .toList();
            model.addAttribute("buscar", buscar);
        }

        // Estadísticas para las tarjetas superiores
        model.addAttribute("productos",      lista);
        model.addAttribute("totalProductos", productoService.totalProductos());
        model.addAttribute("stockBajo",      productoService.stockBajo());
        model.addAttribute("sinStock",       productoService.sinStock());
        model.addAttribute("activos",        productoService.activos());

        // Mensaje de éxito/error tras agregar o eliminar
        if (mensaje != null) model.addAttribute("mensaje", mensaje);

        return "admin/productos";
    }

    // ── GET /admin/productos/nuevo  →  formulario vacío ───────
    @GetMapping("/productos/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Agregar Producto");
        model.addAttribute("accion", "/admin/productos/guardar");
        return "admin/formulario-producto";
    }

    // ── POST /admin/productos/guardar  →  procesa el formulario ─
    @PostMapping("/productos/guardar")
    public String guardarProducto(@Valid @ModelAttribute("producto") Producto producto,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttrs) {

        // Si hay errores de validación, vuelve al formulario
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Agregar Producto");
            model.addAttribute("accion", "/admin/productos/guardar");
            return "admin/formulario-producto";
        }

        productoService.agregar(producto);
        redirectAttrs.addFlashAttribute("mensaje", "¡Producto agregado con éxito!");
        return "redirect:/admin/productos";
    }

    // ── GET /admin/productos/eliminar/{id}  →  elimina ────────
    @GetMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id,
                                   RedirectAttributes redirectAttrs) {
        boolean eliminado = productoService.eliminar(id);
        if (eliminado) {
            redirectAttrs.addFlashAttribute("mensaje", "Producto eliminado correctamente.");
        } else {
            redirectAttrs.addFlashAttribute("error", "No se encontró el producto.");
        }
        return "redirect:/admin/productos";
    }
}