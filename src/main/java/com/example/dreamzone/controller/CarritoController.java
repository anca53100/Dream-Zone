package com.example.dreamzone.controller;

import com.example.dreamzone.model.Producto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CarritoController {

    @GetMapping("/carrito")
    public String verCarrito(Model model, HttpSession session) {

        List<Producto> carrito =
                (List<Producto>) session.getAttribute("carrito");

        if (carrito == null) {
            carrito = new ArrayList<>();
        }

        model.addAttribute("carrito", carrito);

        return "carrito";
    }
}