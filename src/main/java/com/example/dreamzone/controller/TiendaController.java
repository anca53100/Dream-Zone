package com.example.dreamzone.controller;

import com.example.dreamzone.model.Producto;
import com.example.dreamzone.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tienda")
public class TiendaController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("/producto/{id}")
    public String detalleProducto(@PathVariable String id, Model model) {
        productoService.obtenerPorId(id).ifPresent(p -> model.addAttribute("producto", p));
        return "producto-detalle";
    }

    @GetMapping("/producto/demo")
    public String detalleDemo(Model model) {
        Producto p = new Producto();
        p.setNombre("Figura Satoru Gojo");
        p.setSerie("Jujutsu Kaisen");
        p.setPrecio(899.00);
        p.setDescripcion("Figura coleccionable de Satoru Gojo de la serie Jujutsu Kaisen. Detalles increíbles y pintura de alta calidad.");
        p.setAltura("24 cm");
        p.setMaterial("PVC");
        p.setMarca("Banpresto");
        p.setEsNuevo(true);
        p.setStock(15);
        model.addAttribute("producto", p);
        return "producto-detalle";
    }
}