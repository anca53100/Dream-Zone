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
import java.util.UUID;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private JavaMailSender mailSender;

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

        if (usuarioOpt.isEmpty() || !usuarioService.validarPassword(password, usuarioOpt.get().getPassword())) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
            return "auth/login";
        }

        Usuario usuario = usuarioOpt.get();
        session.setAttribute("usuarioLogueado", usuario);
        carritoService.vincularCarritoAlUsuario(session.getId(), usuario.getId());

        return "ROLE_ADMIN".equals(usuario.getRol()) ? "redirect:/admin" : "redirect:/";
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
        String token = UUID.randomUUID().toString();
        boolean existe = usuarioService.guardarTokenRecuperacion(email, token);

        if (!existe) {
            model.addAttribute("error", "No existe una cuenta con ese correo");
            return "auth/recuperar";
        }

        String enlace = "http://localhost:8080/auth/nueva-contrasena?token=" + token;
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Recuperar contraseña - Dream Zone");
        mensaje.setText("Haz clic aquí para cambiar tu contraseña:\n\n" + enlace);
        mailSender.send(mensaje);

        model.addAttribute("exito", "Te enviamos un enlace a tu correo.");
        return "auth/recuperar";
    }

    @GetMapping("/nueva-contrasena")
    public String mostrarNuevaContrasena(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/nueva-contrasena";
    }

    @PostMapping("/nueva-contrasena")
    public String procesarNuevaContrasena(@RequestParam String token,
                                          @RequestParam String password,
                                          Model model) {
        boolean ok = usuarioService.cambiarPassword(token, password);
        if (!ok) {
            model.addAttribute("error", "El enlace no es válido o ya fue usado.");
            model.addAttribute("token", token);
            return "auth/nueva-contrasena";
        }
        return "redirect:/auth/login?registrado=true";
    }
}