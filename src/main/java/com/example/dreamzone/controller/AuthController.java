package com.example.dreamzone.controller;

import com.example.dreamzone.model.Usuario;
import com.example.dreamzone.service.CarritoService;
import com.example.dreamzone.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UsuarioService  usuarioService;
    @Autowired private CarritoService  carritoService;
    @Autowired private JavaMailSender  mailSender;

    // ─── Login ────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String mostrarLogin() { return "auth/login"; }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String email,
                                @RequestParam String password,
                                HttpSession session, Model model) {
        Optional<Usuario> opt = usuarioService.findByEmail(email);
        if (opt.isEmpty() || !usuarioService.validarPassword(password, opt.get().getPassword())) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
            return "auth/login";
        }
        Usuario usuario = opt.get();
        session.setAttribute("usuarioLogueado", usuario);
        carritoService.vincularCarritoAlUsuario(session.getId(), usuario.getId());
        return "ROLE_ADMIN".equals(usuario.getRol()) ? "redirect:/admin" : "redirect:/";
    }

    // ─── Registro ─────────────────────────────────────────────────────────
    @GetMapping("/register")
    public String mostrarRegistro() { return "auth/register"; }

    @PostMapping("/register")
    public String procesarRegistro(@RequestParam String nombre,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   Model model) {
        if (!usuarioService.registrar(nombre, email, password)) {
            model.addAttribute("error", "Ya existe una cuenta con ese correo");
            return "auth/register";
        }
        return "redirect:/auth/login?registrado=true";
    }

    // ─── Logout ───────────────────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    // ─── Recuperar contraseña: solicitar enlace ───────────────────────────
    @GetMapping("/recuperar")
    public String mostrarRecuperar() { return "auth/recuperar"; }

    @PostMapping("/recuperar")
    public String procesarRecuperar(@RequestParam String email,
                                    Model model) {
        String token = java.util.UUID.randomUUID().toString();
        boolean ok = usuarioService.guardarTokenRecuperacion(email, token);
        if (!ok) {
            model.addAttribute("error", "No existe una cuenta con ese correo.");
            return "auth/recuperar";
        }

        // Enviar enlace por email
        String enlace = "http://localhost:8080/auth/nueva-contrasena?token=" + token;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Restablecer contraseña — Dream Zone");
        msg.setText(
            "Hola,\n\n" +
            "Haz clic en el siguiente enlace para restablecer tu contraseña:\n\n" +
            enlace + "\n\n" +
            "El enlace es válido durante 60 minutos.\n" +
            "Si no solicitaste esto, ignora este mensaje.\n\n" +
            "— El equipo de Dream Zone"
        );
        mailSender.send(msg);

        model.addAttribute("emailEnviado", email);
        return "auth/recuperar";
    }

    // ─── Nueva contraseña (desde enlace) ─────────────────────────────────
    @GetMapping("/nueva-contrasena")
    public String mostrarNuevaContrasena(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/nueva-contrasena";
    }

    @PostMapping("/nueva-contrasena")
    public String procesarNuevaContrasena(@RequestParam String token,
                                          @RequestParam String password,
                                          Model model) {
        if (!usuarioService.cambiarPassword(token, password)) {
            model.addAttribute("error", "El enlace no es válido o ya fue usado.");
            model.addAttribute("token", token);
            return "auth/nueva-contrasena";
        }
        return "redirect:/auth/login?registrado=true";
    }
}
