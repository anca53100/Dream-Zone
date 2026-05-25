package com.example.dreamzone;

import com.example.dreamzone.repository.CarritoRepository;
import com.example.dreamzone.repository.PedidoRepository;
import com.example.dreamzone.repository.ProductoRepository;
import com.example.dreamzone.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Test de carga de contexto.
 * Usa @MockBean en todos los repositorios para que Spring pueda construir
 * el contexto completo sin necesitar conexión real a MongoDB Atlas.
 */
@SpringBootTest
class DreamZoneApplicationTests {

    @MockBean ProductoRepository productoRepository;
    @MockBean CarritoRepository   carritoRepository;
    @MockBean UsuarioRepository   usuarioRepository;
    @MockBean PedidoRepository    pedidoRepository;

    @Test
    void contextLoads() {
        // El contexto de Spring debe iniciar correctamente con todos los beans.
    }
}
