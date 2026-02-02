package dh.tour.service;

import dh.tour.model.Inscripcion;
import dh.tour.model.Tour;
import dh.tour.repository.InscripcionRepository;
import dh.tour.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final TourRepository tourRepository;

    @Transactional // <--- Esto asegura que si algo falla, no se guarde nada a medias
    public String inscribirUsuario(String tourId, String usuarioId, String username) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour no encontrado"));

        if (inscripcionRepository.existsByUsuarioIdAndTourId(usuarioId, tourId)) {
            throw new RuntimeException("Ya estás inscrito en este tour");
        }

        // 1. Validar primero si el tour está marcado como disponible
        if (!tour.isDisponible()) {
            throw new RuntimeException("El tour no está disponible actualmente");
        }

        // 2. Validar cupos (Solo si es menor o igual a 0)
        if (tour.getCuposDisponibles() <= 0) {
            throw new RuntimeException("Lo sentimos, ya no quedan cupos");
        }


        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setTourId(tour.getId());
        inscripcion.setUsuarioId(usuarioId);
        inscripcion.setNombreUsuario(username);
        inscripcion.setNombreTour(tour.getNombre());
        inscripcion.setFechaInscripcion(LocalDateTime.now());

        inscripcionRepository.save(inscripcion);

        tour.setCuposDisponibles(tour.getCuposDisponibles() - 1);
        // Si después de restar queda en 0, lo marcamos como no disponible
        if (tour.getCuposDisponibles() == 0) {
            tour.setDisponible(false);
        }
        tourRepository.save(tour);
        return "Inscripción exitosa";
    }

    public void desinscribir(String tourId, String usuarioId) {
        Inscripcion inscripcion = inscripcionRepository.findByTourIdAndUsuarioId(tourId, usuarioId)
                .orElseThrow(() -> new RuntimeException("No estás inscrito"));

        inscripcionRepository.delete(inscripcion);

        // Opcional: Devolver el cupo al tour
        tourRepository.findById(tourId).ifPresent(t -> {
            t.setCuposDisponibles(t.getCuposDisponibles() + 1);
            t.setDisponible(true);
            tourRepository.save(t);
        });
    }

    public List<Inscripcion> obtenerInscripcionesUsuario(String usuarioId) {
        return inscripcionRepository.findByUsuarioId(usuarioId);
    }
    public  List<Inscripcion> obtenerInscripcionesPorTour(String tourId){
        return inscripcionRepository.findByTourId(tourId);
    }
}