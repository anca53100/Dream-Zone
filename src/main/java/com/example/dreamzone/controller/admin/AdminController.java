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

    // ── Dashboard ──
    @GetMapping
    public String dashboard(Model model) {
        List<Producto> todos = productoService.obtenerTodos();

        long activos  = todos.stream().filter(p -> "Activo".equals(p.getEstado())).count();
        long agotados = todos.stream().filter(p -> p.getStock() == 0).count();
        long stockBajo= todos.stream().filter(p -> p.getStock() > 0 && p.getStock() < 5).count();

        // Top 5 por stock (mayor a menor)
        List<Producto> topStock = todos.stream()
                .sorted((a, b) -> b.getStock() - a.getStock())
                .limit(5)
                .toList();

        // Stock crítico (agotados + bajo)
        List<Producto> stockCritico = todos.stream()
                .filter(p -> p.getStock() < 5)
                .sorted((a, b) -> a.getStock() - b.getStock())
                .limit(5)
                .toList();

        model.addAttribute("totalProductos", todos.size());
        model.addAttribute("productosActivos", activos);
        model.addAttribute("agotados", agotados);
        model.addAttribute("stockBajo", stockBajo + agotados);
        model.addAttribute("topStock", topStock);
        model.addAttribute("stockCritico", stockCritico);
        model.addAttribute("porCategoria", productoService.contarPorCategoria());
        return "admin/index";
    }

    // ── Listar con filtros ──
    @GetMapping("/productos")
    public String listarProductos(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Boolean sinStock,
            Model model) {

        List<Producto> todos = productoService.obtenerTodos();
        List<Producto> resultado = todos;

        if (buscar != null && !buscar.isBlank())
            resultado = resultado.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(buscar.toLowerCase()))
                    .toList();

        if (categoria != null && !categoria.isBlank())
            resultado = resultado.stream()
                    .filter(p -> categoria.equals(p.getCategoria()))
                    .toList();

        if (estado != null && !estado.isBlank())
            resultado = resultado.stream()
                    .filter(p -> estado.equals(p.getEstado()))
                    .toList();

        if (Boolean.TRUE.equals(sinStock))
            resultado = resultado.stream()
                    .filter(p -> p.getStock() == 0)
                    .toList();

        long agotados = todos.stream().filter(p -> p.getStock() == 0).count();

        model.addAttribute("productos",      resultado);
        model.addAttribute("totalProductos", todos.size());
        model.addAttribute("agotados",       agotados);
        return "admin/productos";
    }

    // ── Nuevo ──
    @GetMapping("/productos/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias",
                List.of("Figuras","Mangas","Ropa","Funko Pop!","Pósters","Accesorios"));
        return "admin/formulario-producto";
    }

    // ── Guardar (crear) ──
    @PostMapping("/productos/guardar")
    public String guardar(@ModelAttribute Producto producto,
                          @RequestParam("archivo") MultipartFile archivo) throws IOException {

        // Si tiene id → es edición, conservar imágenes si no sube archivo nuevo
        if (producto.getId() != null && !producto.getId().isBlank() && archivo.isEmpty()) {
            productoService.obtenerPorId(producto.getId())
                    .ifPresent(p -> producto.setImagenes(p.getImagenes()));
        }

        // Si sube archivo → guardar imagen
        if (!archivo.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
            Path dir = Paths.get("src/main/resources/static/images/productos/");
            Files.createDirectories(dir);
            Files.copy(archivo.getInputStream(), dir.resolve(filename));
            producto.setImagenes(List.of("/images/productos/" + filename));
        }

        productoService.guardar(producto);
        return "redirect:/admin/productos";
    }

    // ── Editar (GET) ──
    @GetMapping("/productos/editar/{id}")
    public String editar(@PathVariable String id, Model model) {
        productoService.obtenerPorId(id).ifPresent(p -> model.addAttribute("producto", p));
        model.addAttribute("categorias",
                List.of("Figuras","Mangas","Ropa","Funko Pop!","Pósters","Accesorios"));
        return "admin/formulario-producto";   // reutiliza el mismo formulario
    }


    // ── Eliminar ──
    @GetMapping("/productos/eliminar/{id}")
    public String eliminar(@PathVariable String id) {
        productoService.eliminar(id);
        return "redirect:/admin/productos?success=eliminado";
    }

    // ── Ver detalle producto ──
    @GetMapping("/productos/ver/{id}")
    public String verProducto(@PathVariable String id, Model model) {

        Producto producto = productoService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        model.addAttribute("producto", producto);

        return "admin/ver-producto";
    }
}