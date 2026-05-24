package com.example.dreamzone.service;

import com.example.dreamzone.model.Usuario;
import com.example.dreamzone.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // BCryptPasswordEncoder es accesible porque spring-security-crypto
    // ya está en el classpath vía thymeleaf-extras-springsecurity6.
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** Busca un usuario por correo electrónico. */
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Registra un nuevo usuario.
     * @return false si el correo ya existe.
     */
    public boolean registrar(String nombre, String email, String rawPassword) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return false;
        }
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
}
