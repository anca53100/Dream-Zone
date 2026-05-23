package com.example.dreamzone;

import com.example.dreamzone.model.Producto;
import com.example.dreamzone.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DreamZoneApplication {

    public static void main(String[] args) {
        SpringApplication.run(DreamZoneApplication.class, args);
    }

    @Bean
    CommandLineRunner testMongo(ProductoRepository repo) {
        return args -> {
            Producto p = new Producto();
            p.setNombre("Figura Satoru Gojo");
            p.setCategoria("Figuras");
            p.setPrecio(899.00);
            p.setStock(15);
            p.setEstado("Activo");
            p.setEsNuevo(true);
            repo.save(p);
            System.out.println(" MongoDB conectado — productos: " + repo.count());
        };
    }
}