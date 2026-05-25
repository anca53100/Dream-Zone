package com.example.dreamzone.controller;

import com.example.dreamzone.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    @GetMapping("/producto/{id}")
    public String detalle(@PathVariable String id, Model model) {
        productoService.obtenerPorId(id).ifPresent(p -> model.addAttribute("producto", p));
        return "producto-detalle";
    }
}