package dh.tour.controllers;

import dh.tour.model.Categoria;
import dh.tour.repository.CategoriaRepository;
import dh.tour.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final CloudinaryService cloudinaryService;

    public CategoriaController(CategoriaRepository categoriaRepository,
                               CloudinaryService cloudinaryService) {
        this.categoriaRepository = categoriaRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> getAll() {
        return ResponseEntity.ok(categoriaRepository.findAll());
    }

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

        try {
            String pathImagen1 = cloudinaryService.uploadFile(imagen1);
            String pathImagen2 = (imagen2 != null && !imagen2.isEmpty())
                    ? cloudinaryService.uploadFile(imagen2)
                    : null;
            String pathImagen3 = (imagen3 != null && !imagen3.isEmpty())
                    ? cloudinaryService.uploadFile(imagen3)
                    : null;

            Categoria nueva = new Categoria(nombre, descripcion, pathImagen1, pathImagen2, pathImagen3);
            categoriaRepository.save(nueva);

            return ResponseEntity.ok(nueva);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error subiendo imágenes.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) MultipartFile imagen1,
            @RequestParam(required = false) MultipartFile imagen2,
            @RequestParam(required = false) MultipartFile imagen3) {

        return categoriaRepository.findById(id).map(c -> {

            c.setNombre(nombre);
            c.setDescripcion(descripcion);

            try {
                if (imagen1 != null && !imagen1.isEmpty()) {
                    c.setImagen1(cloudinaryService.uploadFile(imagen1));
                }
                if (imagen2 != null && !imagen2.isEmpty()) {
                    c.setImagen2(cloudinaryService.uploadFile(imagen2));
                }
                if (imagen3 != null && !imagen3.isEmpty()) {
                    c.setImagen3(cloudinaryService.uploadFile(imagen3));
                }

                return ResponseEntity.ok(categoriaRepository.save(c));

            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("Error subiendo imágenes.");
            }

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

    @PatchMapping("/{id}")
    public ResponseEntity<?> actualizarParcial(
            @PathVariable String id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) MultipartFile imagen1,
            @RequestParam(required = false) MultipartFile imagen2,
            @RequestParam(required = false) MultipartFile imagen3
    ) {

        return categoriaRepository.findById(id).map(categoria -> {

            try {
                // Actualiza solo lo que viene
                if (nombre != null && !nombre.isEmpty()) {
                    categoria.setNombre(nombre);
                }

                if (descripcion != null && !descripcion.isEmpty()) {
                    categoria.setDescripcion(descripcion);
                }

                if (imagen1 != null && !imagen1.isEmpty()) {
                    categoria.setImagen1(cloudinaryService.uploadFile(imagen1));
                }

                if (imagen2 != null && !imagen2.isEmpty()) {
                    categoria.setImagen2(cloudinaryService.uploadFile(imagen2));
                }

                if (imagen3 != null && !imagen3.isEmpty()) {
                    categoria.setImagen3(cloudinaryService.uploadFile(imagen3));
                }

                categoriaRepository.save(categoria);
                return ResponseEntity.ok(categoria);

            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("Error al actualizar imágenes");
            }

        }).orElse(ResponseEntity.notFound().build());
    }



}
