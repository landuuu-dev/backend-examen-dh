package dh.tour.controllers;

import dh.tour.model.Categoria;
import dh.tour.repository.CategoriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> getAll() {
        return ResponseEntity.ok(categoriaRepository.findAll());
    }

    // Método correcto para crear categoría con imagen obligatoria
    @PostMapping
    public ResponseEntity<?> crearCategoria(
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam MultipartFile imagen1,
            @RequestParam(required = false) MultipartFile imagen2,
            @RequestParam(required = false) MultipartFile imagen3) {

        if (imagen1.isEmpty()) {
            return ResponseEntity.badRequest().body("La categoría debe tener al menos una imagen.");
        }

        String pathImagen1 = guardarImagen(imagen1);
        String pathImagen2 = (imagen2 != null && !imagen2.isEmpty()) ? guardarImagen(imagen2) : null;
        String pathImagen3 = (imagen3 != null && !imagen3.isEmpty()) ? guardarImagen(imagen3) : null;

        Categoria nueva = new Categoria(nombre, descripcion, pathImagen1, pathImagen2, pathImagen3);
        categoriaRepository.save(nueva);

        return ResponseEntity.ok(nueva);
    }


    private String guardarImagen(MultipartFile imagen) {
        try {
            // Carpeta donde se guardan las imágenes
            String uploadDir = "C:/Users/Cynthia/Documents/DH/tour/uploads/";

            // Nombre único para evitar conflictos
            String fileName = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();

            // Ruta física completa
            Path filePath = Paths.get(uploadDir).resolve(fileName);

            // Guardar el archivo
            Files.write(filePath, imagen.getBytes());

            // URL que el frontend puede usar
            return "/uploads/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Categoria> update(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) MultipartFile imagen1,
            @RequestParam(required = false) MultipartFile imagen2,
            @RequestParam(required = false) MultipartFile imagen3) {

        return categoriaRepository.findById(id).map(c -> {
            c.setNombre(nombre);
            c.setDescripcion(descripcion);

            if (imagen1 != null && !imagen1.isEmpty()) {
                c.setImagen1(guardarImagen(imagen1));
            }
            if (imagen2 != null && !imagen2.isEmpty()) {
                c.setImagen2(guardarImagen(imagen2));
            }
            if (imagen3 != null && !imagen3.isEmpty()) {
                c.setImagen3(guardarImagen(imagen3));
            }

            return ResponseEntity.ok(categoriaRepository.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
            return ResponseEntity.ok("Categoría eliminada correctamente");
        }
        return ResponseEntity.notFound().build();
    }
}
