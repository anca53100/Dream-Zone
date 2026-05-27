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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        long activos  = todos.stream().filter(p -> "Activo".equalsIgnoreCase(
                p.getEstado() != null ? p.getEstado() : ""
        )).count();
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

        long totalCategorias = productoService.contarPorCategoria().size();

        model.addAttribute("totalProductos", todos.size());
        model.addAttribute("productosActivos", activos);
        model.addAttribute("agotados", agotados);
        model.addAttribute("stockBajo", stockBajo + agotados);
        model.addAttribute("topStock", topStock);
        model.addAttribute("stockCritico", stockCritico);
        model.addAttribute("porCategoria", productoService.contarPorCategoria());
        model.addAttribute("actividadReciente",
                todos.stream()
                        .sorted((a, b) -> b.getId().compareTo(a.getId()))
                        .limit(5)
                        .toList());

        model.addAttribute("totalCategorias",
                productoService.contarPorCategoria().size());
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
                List.of("Camisas Importadas","Camisas Nacionales","Gorras","Tulas","Medias","Billeteras"));
        model.addAttribute("colecciones",
                List.of("Death Note","One Piece","Naruto","Demon Slayer"));
        return "admin/formulario-producto";
    }

    // ── Guardar (crear / editar) ──
    @PostMapping("/productos/guardar")
    public String guardar(
            @ModelAttribute Producto formData,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "tallaXS",  required = false, defaultValue = "0") int tallaXS,
            @RequestParam(value = "tallaS",   required = false, defaultValue = "0") int tallaS,
            @RequestParam(value = "tallaM",   required = false, defaultValue = "0") int tallaM,
            @RequestParam(value = "tallaL",   required = false, defaultValue = "0") int tallaL,
            @RequestParam(value = "tallaXL",  required = false, defaultValue = "0") int tallaXL,
            @RequestParam(value = "tallaXXL", required = false, defaultValue = "0") int tallaXXL
    ) throws IOException {

        boolean esEdicion = formData.getId() != null && !formData.getId().isBlank();

        // ── Para edición: partir del producto existente en DB ──
        // Así no se pierden campos que no están en el formulario
        Producto producto = esEdicion
                ? productoService.obtenerPorId(formData.getId()).orElse(new Producto())
                : new Producto();

        // ── Copiar campos del formulario ──
        producto.setId(formData.getId());
        producto.setNombre(formData.getNombre());
        producto.setDescripcion(formData.getDescripcion());
        producto.setCategoria(formData.getCategoria());
        producto.setSerie(formData.getSerie());
        producto.setPrecio(formData.getPrecio());
        producto.setMaterial(formData.getMaterial());
        producto.setEstado((formData.getEstado() == null || formData.getEstado().isBlank())
                ? "Activo" : formData.getEstado());

        // ── Imagen: guardar nueva o conservar la existente ──
        if (archivo != null && !archivo.isEmpty()) {
            Path dir = Paths.get("uploads/productos/");
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
            Files.copy(archivo.getInputStream(), dir.resolve(filename));
            producto.setImagen("/uploads/productos/" + filename);
        }
        // Si no hay archivo nuevo, producto ya trae la imagen del DB (en edición)
        // o null (en creación sin imagen)

        // ── Stock por talla (solo Camisas) ──
        if (Producto.tieneTallas(formData.getCategoria())) {
            Map<String, Integer> tallas = new LinkedHashMap<>();
            tallas.put("XS",  tallaXS);
            tallas.put("S",   tallaS);
            tallas.put("M",   tallaM);
            tallas.put("L",   tallaL);
            tallas.put("XL",  tallaXL);
            tallas.put("XXL", tallaXXL);
            producto.setStockPorTalla(tallas);
            producto.setStock(tallaXS + tallaS + tallaM + tallaL + tallaXL + tallaXXL);
        } else {
            producto.setStockPorTalla(null);
            producto.setStock(formData.getStock());
        }

        productoService.guardar(producto);
        return "redirect:/admin/productos";
    }
    // ── Editar (GET) ──
    @GetMapping("/productos/editar/{id}")
    public String editar(@PathVariable String id, Model model) {
        productoService.obtenerPorId(id).ifPresent(p -> model.addAttribute("producto", p));
        model.addAttribute("categorias",
                List.of("Camisas Importadas","Camisas Nacionales","Gorras","Tulas","Medias","Billeteras"));
        model.addAttribute("colecciones",
                List.of("Death Note","One Piece","Naruto","Demon Slayer"));
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