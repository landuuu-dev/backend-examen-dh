package dh.tour.controllers;

import dh.tour.dto.request.CategoriaRequest;
import dh.tour.dto.request.TourRequest;
import dh.tour.dto.response.CategoriaResponse;
import dh.tour.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> getAll() {
        return ResponseEntity.ok(categoriaService.findAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<?> crear(
            @ModelAttribute CategoriaRequest categoriaRequest,
            @RequestParam("imagenes") List<MultipartFile> imagenes) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(categoriaService.crear(categoriaRequest, imagenes));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable String id,
            @Valid @ModelAttribute CategoriaRequest categoriaRequest, // Usamos el DTO aquí también
            @RequestParam(required = false) List<MultipartFile> imagenes) {
        try {
            return ResponseEntity.ok(categoriaService.actualizarCategoria(id, categoriaRequest, imagenes));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> actualizarParcialCategoria(
            @PathVariable String id,
            @ModelAttribute CategoriaRequest categoriaRequest, // Agrupamos todo en el DTO
            @RequestParam(required = false) List<MultipartFile> imagenes) {
        try {
            // Usamos el mismo método del service que ya sabe manejar nulos
            return ResponseEntity.ok(categoriaService.actualizarCategoria(id, categoriaRequest, imagenes));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}