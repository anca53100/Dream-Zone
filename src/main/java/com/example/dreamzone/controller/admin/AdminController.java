package com.example.dreamzone.controller.admin;

import com.example.dreamzone.model.Producto;
import com.example.dreamzone.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalProductos", productoService.obtenerTodos().size());
        return "admin/index";
    }

    @GetMapping("/productos")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.obtenerTodos());
        return "admin/productos";
    }

    @GetMapping("/productos/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        return "admin/formulario-producto";
    }

    @PostMapping("/productos/guardar")
    public String guardar(@ModelAttribute Producto producto,
                          @RequestParam("archivo") MultipartFile archivo) throws IOException {
        if (!archivo.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
            Path path = Paths.get("src/main/resources/static/images/productos/" + filename);
            Files.copy(archivo.getInputStream(), path);
            producto.setImagenes(List.of("/images/productos/" + filename));
        }
        productoService.guardar(producto);
        return "redirect:/admin/productos";
    }

    @GetMapping("/productos/editar/{id}")
    public String editar(@PathVariable String id, Model model) {
        productoService.obtenerPorId(id).ifPresent(p -> model.addAttribute("producto", p));
        return "admin/editar-producto";
    }

    @GetMapping("/productos/eliminar/{id}")
    public String eliminar(@PathVariable String id) {
        productoService.eliminar(id);
        return "redirect:/admin/productos";
    }
}