package dh.tour.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "http://localhost:5173")
public class UploadController {

    // <-- Cambiado a la carpeta que creaste fuera del código fuente
    private static final String UPLOAD_DIR = "C:/Users/Cynthia/Documents/DH/tour/uploads/";

    @PostMapping
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            // Esto es lo que tu frontend usará para mostrar la imagen
            String imageUrl = "/uploads/" + fileName;
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al subir la imagen.");
        }
    }
}
