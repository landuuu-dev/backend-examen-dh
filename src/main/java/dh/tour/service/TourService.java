package dh.tour.service;
import dh.tour.model.Categoria;
import dh.tour.model.Tour;
import dh.tour.repository.CategoriaRepository;
import dh.tour.repository.TourRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TourService {

    private final TourRepository tourRepository;
    private final CategoriaRepository categoriaRepository;
    private final CloudinaryService cloudinaryService;

    public TourService(TourRepository tourRepository, CategoriaRepository categoriaRepository, CloudinaryService cloudinaryService) {
        this.tourRepository = tourRepository;
        this.categoriaRepository = categoriaRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public List<Tour> listarTodos() {
        return tourRepository.findAll();
    }

    public List<Tour> listarPorCategoria(String categoriaId) {
        return tourRepository.findByCategoriaId(categoriaId);
    }

    public Tour crearTour(String nombre, String categoriaId, String descripcion, String ubicacion,
                          int precio, LocalDate inicio, LocalDate fin, int cupos,
                          boolean disponible, List<MultipartFile> imagenes) throws IOException {

        Categoria cat = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        List<String> urls = subirImagenes(imagenes);

        Tour tour = new Tour(nombre, cat, descripcion, ubicacion, precio, inicio, fin, cupos, disponible, urls);
        tour.setCuposDisponibles(cupos);
        return tourRepository.save(tour);
    }

    public Tour actualizarTour(String id, String nombre, String descripcion, String ubicacion,
                               Integer precio, LocalDate inicio, LocalDate fin, Integer totales,
                               Integer disponibles, Boolean disponible, String categoriaId,
                               List<MultipartFile> imagenes) throws IOException {

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour no encontrado"));

        if (nombre != null) tour.setNombre(nombre);
        if (descripcion != null) tour.setDescripcion(descripcion);
        if (ubicacion != null) tour.setUbicacion(ubicacion);
        if (precio != null) tour.setPrecio(precio);
        if (inicio != null) tour.setFechaInicio(inicio);
        if (fin != null) tour.setFechaFin(fin);
        if (totales != null) tour.setCuposTotales(totales);
        if (disponibles != null) tour.setCuposDisponibles(disponibles);
        if (disponible != null) tour.setDisponible(disponible);

        // Validación anti-bug de cupos
        if (tour.getCuposDisponibles() > tour.getCuposTotales()) {
            tour.setCuposDisponibles(tour.getCuposTotales());
        }

        if (categoriaId != null) {
            Categoria cat = categoriaRepository.findById(categoriaId).orElse(null);
            if (cat != null) tour.setCategoria(cat);
        }

        if (imagenes != null && !imagenes.isEmpty()) {
            tour.setImagenes(subirImagenes(imagenes));
        }

        return tourRepository.save(tour);
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
        if (!tourRepository.existsById(id)) throw new RuntimeException("Tour no encontrado");
        tourRepository.deleteById(id);
    }
}
