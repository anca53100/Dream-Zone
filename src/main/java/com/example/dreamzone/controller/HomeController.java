package com.example.dreamzone.controller;

import com.example.dreamzone.model.Producto;
import com.example.dreamzone.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("/")
    public String inicio(Model model) {

        model.addAttribute("productos",
                productoService.obtenerTodos());

        return "index";
    }

    @GetMapping("/tienda")
    public String tienda(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            Model model) {

        List<Producto> productos;
        if (buscar != null && !buscar.isEmpty()) {
            productos = productoService.buscar(buscar);
        } else if (categoria != null) {
            productos = productoService.obtenerPorCategoria(categoria);
        } else {
            productos = productoService.obtenerTodos();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", List.of(
                "Figuras","Mangas","Ropa","Funko Pop!","Pósters","Accesorios"
        ));
        return "tienda";
    }

    @GetMapping("/producto/{id}")
    public String detalle(@PathVariable String id, Model model) {
        productoService.obtenerPorId(id).ifPresent(p -> model.addAttribute("producto", p));
        return "producto-detalle";
    }

    @PostMapping("/carrito/agregar/{id}")
    public String agregarAlCarrito(@PathVariable String id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {

        List<Producto> carrito =
                (List<Producto>) session.getAttribute("carrito");

        if (carrito == null) {
            carrito = new java.util.ArrayList<>();
        }

        productoService.obtenerPorId(id).ifPresent(carrito::add);

        session.setAttribute("carrito", carrito);

        redirectAttributes.addFlashAttribute("success",
                "Producto agregado al carrito");

        return "redirect:/tienda";
    }
}