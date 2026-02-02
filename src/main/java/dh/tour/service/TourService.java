package dh.tour.service;

import dh.tour.dto.request.TourRequest;
import dh.tour.dto.response.TourResponse;
import dh.tour.exceptions.ResourceNotFoundException;
import dh.tour.model.Categoria;
import dh.tour.model.EstadoTour;
import dh.tour.model.Tour;
import dh.tour.repository.CategoriaRepository;
import dh.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourService {

    private final TourRepository tourRepository;
    private final CategoriaRepository categoriaRepository;
    private final CloudinaryService cloudinaryService;


    public List<TourResponse> listarTodos() {
        return tourRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TourResponse crearTour(TourRequest dto, List<MultipartFile> imagenes) throws IOException {
        Categoria cat = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        List<String> urls = subirImagenes(imagenes);

        Tour tour = Tour.builder()
                .nombre(dto.getNombre())
                .categoria(cat)
                .descripcion(dto.getDescripcion())
                .ubicacion(dto.getUbicacion())
                .precio(dto.getPrecio())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .cuposTotales(dto.getCuposTotales())
                .cuposDisponibles(dto.getCuposTotales())
                .estado(EstadoTour.ACTIVO)
                .imagenes(urls)
                .build();

        Tour guardado = tourRepository.save(tour);
        return mapToResponse(guardado);
    }

    private TourResponse mapToResponse(Tour tour) {
        // Lógica automática: Si no hay cupos, el estado para el usuario es AGOTADO
        EstadoTour estadoFinal = tour.getEstado();
        if (tour.getCuposDisponibles() <= 0) {
            estadoFinal = EstadoTour.AGOTADO;
        }

        return TourResponse.builder()
                .id(tour.getId())
                .nombre(tour.getNombre())
                .nombreCategoria(tour.getCategoria() != null ? tour.getCategoria().getNombre() : "Sin categoría")
                .descripcion(tour.getDescripcion())
                .ubicacion(tour.getUbicacion())
                .precio(tour.getPrecio())
                .fechaInicio(tour.getFechaInicio())
                .cuposDisponibles(tour.getCuposDisponibles())
                .estado(estadoFinal) // <--- Enviamos el estado calculado
                .imagenes(tour.getImagenes())
                .build();
    }

    private List<String> subirImagenes(List<MultipartFile> imagenes) throws IOException {
        List<String> urls = new ArrayList<>();
        if (imagenes != null) {
            for (MultipartFile img : imagenes) {
                if (!img.isEmpty()) {
                    urls.add(cloudinaryService.uploadFile(img));
                }
            }
        }
        return urls;
    }

    public void eliminarTour(String id) {
        if (!tourRepository.existsById(id)) throw new ResourceNotFoundException("Tour no encontrado");
        tourRepository.deleteById(id);
    }


    public TourResponse actualizarTour(String id, TourRequest dto, List<MultipartFile> imagenes) throws IOException {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour no encontrado"));

        // Actualizamos los campos desde el DTO
        if (dto.getNombre() != null) tour.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) tour.setDescripcion(dto.getDescripcion());
        if (dto.getUbicacion() != null) tour.setUbicacion(dto.getUbicacion());
        if (dto.getPrecio() > 0) tour.setPrecio(dto.getPrecio());
        if (dto.getFechaInicio() != null) tour.setFechaInicio(dto.getFechaInicio());
        if (dto.getFechaFin() != null) tour.setFechaFin(dto.getFechaFin());

        // Ajuste de cupos
        if (dto.getCuposTotales() > 0) {
            int diferencia = dto.getCuposTotales() - tour.getCuposTotales();
            tour.setCuposTotales(dto.getCuposTotales());
            tour.setCuposDisponibles(tour.getCuposDisponibles() + diferencia);
        }

        if (dto.getCategoriaId() != null) {
            Categoria cat = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            tour.setCategoria(cat);
        }

        if (imagenes != null && !imagenes.isEmpty()) {
            tour.setImagenes(subirImagenes(imagenes));
        }

        if (dto.getEstado() != null) {
            tour.setEstado(dto.getEstado());
        }

        return mapToResponse(tourRepository.save(tour));
    }

    public Page<TourResponse> buscarTours(String nombre, Integer precioMax, Pageable pageable) {
        String nombreFiltro = (nombre != null) ? nombre.trim() : "";
        int precioFiltro = (precioMax != null) ? precioMax : Integer.MAX_VALUE;

        // Usaremos un método del repositorio que solo filtre por nombre y precio
        // Dejando que el 'estado' se muestre sea cual sea
        return tourRepository.findByNombreContainingIgnoreCaseAndPrecioLessThanEqual(
                nombreFiltro,
                precioFiltro,
                pageable
        ).map(this::mapToResponse);
    }

} // Esta es la llave que cierra la CLASE