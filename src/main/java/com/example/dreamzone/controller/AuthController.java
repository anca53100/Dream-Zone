package com.example.dreamzone.controller;

import com.example.dreamzone.model.Usuario;
import com.example.dreamzone.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        return "auth/login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String email,
                                @RequestParam String password,
                                HttpSession session,
                                Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email);
        if (usuarioOpt.isPresent() && !usuarioService.validarPassword(password, usuarioOpt.get().getPassword())) {
            usuarioOpt = Optional.empty();
        }

        if (usuarioOpt.isPresent()) {
            session.setAttribute("usuarioLogueado", usuarioOpt.get());
            if ("ROLE_ADMIN".equals(usuarioOpt.get().getRol())) {
                return "redirect:/admin";
            }
            return "redirect:/";
        }

        model.addAttribute("error", "Correo o contraseña incorrectos");
        return "auth/login";
    }

    @GetMapping("/register")
    public String mostrarRegistro() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String procesarRegistro(@RequestParam String nombre,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   Model model) {
        boolean registrado = usuarioService.registrar(nombre, email, password);
        if (!registrado) {
            model.addAttribute("error", "Ya existe una cuenta con ese correo");
            return "auth/register";
        }
        return "redirect:/auth/login?registrado=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    @GetMapping("/recuperar")
    public String mostrarRecuperar() {
        return "auth/recuperar";
    }

    @PostMapping("/recuperar")
    public String procesarRecuperar(@RequestParam String email, Model model) {
        if (usuarioService.findByEmail(email).isPresent()) {
            model.addAttribute("exito", "Si el correo existe recibirás un enlace en breve");
        } else {
            model.addAttribute("error", "No existe una cuenta con ese correo");
        }
        return "auth/recuperar";
    }
}