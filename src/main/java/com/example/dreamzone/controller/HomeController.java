package com.example.dreamzone.controller;

import com.example.dreamzone.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final ProductoService productoService;

    public HomeController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // ── HOME ──────────────────────────────────────
    @GetMapping("/")
    public String index(@RequestParam(required = false) String categoria,
                        @RequestParam(required = false) String q,
                        Model model) {
        model.addAttribute("categorias", new String[]{"Todos","Camisas","Sacos","Accesorios","Otros"});
        model.addAttribute("destacados", productoService.getDestacados());
        model.addAttribute("activeCat",  categoria != null ? categoria : "Todos");
        if (q != null && !q.isBlank()) {
            model.addAttribute("productos", productoService.buscar(q));
            model.addAttribute("busqueda",  q);
        } else {
            model.addAttribute("productos", productoService.getPorCategoria(categoria));
        }
        return "admin/index";
    }

    // ── LOGIN ─────────────────────────────────────
    @GetMapping("/login")
    public String loginPage() { return "admin/login"; }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes ra) {
        if (email != null && email.contains("@") && password != null && !password.isBlank()) {
            session.setAttribute("usuario", email.split("@")[0]);
            return "redirect:/";
        }
        ra.addFlashAttribute("error", "Correo o contraseña incorrectos.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "admin/redirect:/";
    }

    // ── REGISTER ──────────────────────────────────
    @GetMapping("/register")
    public String registerPage() { return "admin/register"; }

    @PostMapping("/register")
    public String doRegister(@RequestParam String nombre,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String password2,
                             RedirectAttributes ra) {
        if (!password.equals(password2)) {
            ra.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/register";
        }
        if (password.length() < 6) {
            ra.addFlashAttribute("error", "La contraseña debe tener mínimo 6 caracteres.");
            return "redirect:/register";
        }
        ra.addFlashAttribute("exito", "¡Cuenta creada! Ahora inicia sesión.");
        return "redirect:/login";
    }

    // ── FORGOT PASSWORD ───────────────────────────
    @GetMapping("/forgot")
    public String forgotPage() { return "admin/forgot"; }

    @PostMapping("/forgot")
    public String doForgot(@RequestParam String email, RedirectAttributes ra) {
        ra.addFlashAttribute("exito", "Enlace enviado a " + email);
        return "redirect:/login";
    }

    @GetMapping("/categoria/{categoria}")
    public String filtrar(@PathVariable String categoria, Model model) {
        model.addAttribute("categorias", new String[]{"Todos","Camisas","Sacos","Accesorios","Otros"});
        model.addAttribute("activeCat", categoria);
        model.addAttribute("productos", productoService.getPorCategoria(categoria));
        model.addAttribute("destacados", productoService.getDestacados());
        return "admin/index";
    }
}