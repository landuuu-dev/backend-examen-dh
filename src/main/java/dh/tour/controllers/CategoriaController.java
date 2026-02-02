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
            @RequestParam("imagenes") List<MultipartFile> imagenes) throws IOException {
        categoriaService.crear(categoriaRequest, imagenes);
        return ResponseEntity.ok("Se ha creado la categoria correctamente");
    }


    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable String id,
            @Valid @ModelAttribute CategoriaRequest categoriaRequest, // Usamos el DTO aquí también
            @RequestParam(required = false) List<MultipartFile> imagenes) throws IOException {
       categoriaService.actualizarCategoria(id, categoriaRequest, imagenes);
       return ResponseEntity.ok("Se actualizo correctamente la categoria");

    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> actualizarParcialCategoria(
            @PathVariable String id,
            @ModelAttribute CategoriaRequest categoriaRequest, // Agrupamos todo en el DTO
            @RequestParam(required = false) List<MultipartFile> imagenes) throws IOException {
        categoriaService.actualizarCategoria(id, categoriaRequest, imagenes);
        return ResponseEntity.ok("Se actualizo parcialmente la categoria correctamente");
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}