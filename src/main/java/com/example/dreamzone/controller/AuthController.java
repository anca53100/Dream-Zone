package com.example.dreamzone.controller;

import com.example.dreamzone.model.Usuario;
import com.example.dreamzone.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    // ─── Login ───────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Optional<Usuario> opt = usuarioService.findByEmail(email);

        if (opt.isPresent() && usuarioService.validarPassword(password, opt.get().getPassword())) {
            session.setAttribute("usuario", opt.get());
            session.setAttribute("usuarioNombre", opt.get().getNombre());
            return "redirect:/";
        }

        model.addAttribute("error", "Correo o contraseña incorrectos.");
        return "auth/login";
    }

    // ─── Register ─────────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String password2,
            Model model,
            RedirectAttributes ra) {

        if (!password.equals(password2)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "auth/register";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "auth/register";
        }

        boolean ok = usuarioService.registrar(nombre, email, password);
        if (!ok) {
            model.addAttribute("error", "Este correo ya está registrado.");
            return "auth/register";
        }

        ra.addFlashAttribute("exito", "¡Cuenta creada! Ya puedes iniciar sesión.");
        return "redirect:/login";
    }

    // ─── Logout ──────────────────────────────────────────────────────────────

    @GetMapping("/auth/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
