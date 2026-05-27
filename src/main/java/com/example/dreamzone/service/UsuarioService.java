package com.example.dreamzone.service;

import com.example.dreamzone.model.Usuario;
import com.example.dreamzone.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** Busca un usuario por correo electrónico. */
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /** Registra un nuevo usuario. @return false si el correo ya existe. */
    public boolean registrar(String nombre, String email, String rawPassword) {
        if (usuarioRepository.findByEmail(email).isPresent()) return false;
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setRol("ROLE_USER");
        usuarioRepository.save(u);
        return true;
    }

    /** Valida si la contraseña en texto plano coincide con el hash almacenado. */
    public boolean validarPassword(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    // ─── Recuperación por enlace UUID ──────────────────────────────────────

    /**
     * Cambia la contraseña usando el token UUID del enlace enviado por email.
     */
    public boolean cambiarPassword(String token, String nuevaPassword) {
        Optional<Usuario> opt = usuarioRepository.findByTokenRecuperacion(token);
        if (opt.isEmpty()) return false;

        Usuario u = opt.get();
        if (u.getTokenExpiracion() != null && u.getTokenExpiracion().isBefore(LocalDateTime.now()))
            return false;

        u.setPassword(encoder.encode(nuevaPassword));
        u.setTokenRecuperacion(null);
        u.setTokenExpiracion(null);
        usuarioRepository.save(u);
        return true;
    }

    /**
     * Guarda el token UUID de recuperación con expiración de 60 minutos.
     * @return false si el email no existe.
     */
    public boolean guardarTokenRecuperacion(String email, String token) {
        Optional<Usuario> opt = usuarioRepository.findByEmail(email);
        if (opt.isEmpty()) return false;
        Usuario u = opt.get();
        u.setTokenRecuperacion(token);
        u.setTokenExpiracion(LocalDateTime.now().plusMinutes(60));
        usuarioRepository.save(u);
        return true;
    }
}
