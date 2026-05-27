package com.example.dreamzone.controller;

import com.example.dreamzone.service.GridFSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * Controlador público que sirve imágenes almacenadas en MongoDB GridFS.
 *
 * URL de acceso: /imagenes/{id}
 * Ejemplo: /imagenes/6650a3c1f4e21b2d3c8e9f01
 *
 * Este endpoint está excluido del interceptor de sesión (ver WebConfig),
 * por lo que funciona sin necesidad de estar autenticado.
 */
@Controller
@RequestMapping("/imagenes")
public class ImagenController {

    @Autowired
    private GridFSService gridFSService;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable String id) {
        try {
            GridFsResource recurso = gridFSService.obtener(id);

            if (recurso == null || !recurso.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Leer los bytes de la imagen
            byte[] datos = StreamUtils.copyToByteArray(recurso.getInputStream());

            // Determinar el tipo de contenido (JPEG, PNG, WEBP, etc.)
            String contentType = recurso.getContentType();
            MediaType mediaType;
            try {
                mediaType = contentType != null
                        ? MediaType.parseMediaType(contentType)
                        : MediaType.IMAGE_JPEG;
            } catch (Exception e) {
                mediaType = MediaType.IMAGE_JPEG;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            // Cache de 7 días en el navegador para evitar recargas innecesarias
            headers.setCacheControl("public, max-age=604800");

            return new ResponseEntity<>(datos, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            // ID con formato inválido (no es un ObjectId de 24 hex)
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
