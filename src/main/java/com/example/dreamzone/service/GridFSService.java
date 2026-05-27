package com.example.dreamzone.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Servicio para almacenar y recuperar imágenes en MongoDB GridFS.
 * Las imágenes nunca se guardan localmente: viven en Atlas junto a los demás datos.
 *
 * Formato almacenado en Producto.imagen: "/imagenes/{objectId}"
 * Ejemplo: "/imagenes/6650a3c1f4e21b2d3c8e9f01"
 */
@Service
public class GridFSService {

    private static final String URL_PREFIX = "/imagenes/";

    @Autowired
    private GridFsTemplate gridFsTemplate;

    /**
     * Guarda un archivo en GridFS.
     *
     * @param archivo MultipartFile recibido del formulario
     * @return ID del archivo (ObjectId como String) sin prefijo
     * @throws IOException si hay error de lectura del archivo
     */
    public String guardar(MultipartFile archivo) throws IOException {
        String contentType = archivo.getContentType() != null
                ? archivo.getContentType()
                : "image/jpeg";

        String nombreOriginal = archivo.getOriginalFilename() != null
                ? archivo.getOriginalFilename()
                : "imagen";

        try (InputStream inputStream = archivo.getInputStream()) {
            ObjectId id = gridFsTemplate.store(inputStream, nombreOriginal, contentType);
            return id.toHexString();
        }
    }

    /**
     * Recupera el recurso de GridFS a partir de un ObjectId en hex.
     *
     * @param id ObjectId en formato hexadecimal (24 chars)
     * @return GridFsResource, o null si no existe
     */
    public GridFsResource obtener(String id) {
        try {
            GridFSFile archivo = gridFsTemplate.findOne(
                    new Query(Criteria.where("_id").is(new ObjectId(id)))
            );
            if (archivo == null) return null;
            return gridFsTemplate.getResource(archivo);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Elimina una imagen de GridFS dado el valor almacenado en Producto.imagen.
     * Acepta tanto la URL completa "/imagenes/{id}" como el ID directo.
     * Si la imagen es una ruta local (empieza con "/uploads"), no hace nada.
     *
     * @param imagenUrl valor del campo Producto.imagen
     */
    public void eliminarPorUrl(String imagenUrl) {
        if (imagenUrl == null) return;

        String hexId;
        if (imagenUrl.startsWith(URL_PREFIX)) {
            hexId = imagenUrl.substring(URL_PREFIX.length());
        } else if (imagenUrl.matches("[0-9a-fA-F]{24}")) {
            // Formato antiguo (solo ID sin prefijo)
            hexId = imagenUrl;
        } else {
            // Es una ruta local ("/uploads/...") u otro valor → ignorar
            return;
        }

        try {
            gridFsTemplate.delete(
                    new Query(Criteria.where("_id").is(new ObjectId(hexId)))
            );
        } catch (Exception ignored) {
            // ID inválido o ya eliminado — no lanzar error
        }
    }
}
