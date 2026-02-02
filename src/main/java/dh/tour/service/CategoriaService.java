package dh.tour.service;

import dh.tour.dto.request.CategoriaRequest;
import dh.tour.dto.request.TourRequest;
import dh.tour.dto.response.CategoriaResponse;
import dh.tour.dto.response.TourResponse;
import dh.tour.model.Categoria;
import dh.tour.model.Tour;
import dh.tour.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CloudinaryService cloudinary;

    public List<CategoriaResponse> findAll() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoriaResponse crear(CategoriaRequest dto, List<MultipartFile> imagenes) throws IOException{
        List<String> urls = subirImagenes(imagenes);
        Categoria categoria = Categoria.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .imagenes(urls)
                .build();
        Categoria guardado = categoriaRepository.save(categoria);
        return mapToResponse(guardado);
    }

    private List<String> subirImagenes(List<MultipartFile> imagenes) throws IOException {
        List<String> urls = new ArrayList<>();
        if (imagenes != null) {
            for (MultipartFile img : imagenes) {
                if (!img.isEmpty()) {
                    urls.add(cloudinary.uploadFile(img));
                }
            }
        }
        return urls;
    }


    public CategoriaResponse actualizarCategoria (String id, CategoriaRequest dto, List<MultipartFile> imagenes) throws IOException {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria con id: "+ id + ", no encontrada"));

        // Actualizamos los campos desde el DTO
        if (dto.getNombre() != null) categoria.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) categoria.setDescripcion(dto.getDescripcion());

        if (imagenes != null && !imagenes.isEmpty()) {
            categoria.setImagenes(subirImagenes(imagenes));
        }

        return mapToResponse(categoriaRepository.save(categoria));
    }

    public void eliminar(String id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: Categoría no encontrada");
        }
        categoriaRepository.deleteById(id);
    }

    private CategoriaResponse mapToResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.getImagenes());
    }

}