package dh.tour.service;

import dh.tour.model.EstadoTour;
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

    @Transactional
    public String inscribirUsuario(String tourId, String usuarioId, String username) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour no encontrado"));

        if (inscripcionRepository.existsByUsuarioIdAndTourId(usuarioId, tourId)) {
            throw new RuntimeException("Ya estás inscrito en este tour");
        }

        // 1. Validar si el estado NO es ACTIVO (Enum en lugar de booleano)
        if (tour.getEstado() != EstadoTour.ACTIVO) {
            throw new RuntimeException("El tour no está disponible o ha sido cancelado");
        }

        // 2. Validar cupos
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

        // Restar cupo
        tour.setCuposDisponibles(tour.getCuposDisponibles() - 1);

        // 3. Si llega a 0, cambiamos el estado a AGOTADO
        if (tour.getCuposDisponibles() == 0) {
            tour.setEstado(EstadoTour.AGOTADO);
        }

        tourRepository.save(tour);
        return "Inscripción exitosa";
    }

    public void desinscribir(String tourId, String usuarioId) {
        Inscripcion inscripcion = inscripcionRepository.findByTourIdAndUsuarioId(tourId, usuarioId)
                .orElseThrow(() -> new RuntimeException("No estás inscrito"));

        inscripcionRepository.delete(inscripcion);

        // Devolver el cupo y reactivar si estaba agotado
        tourRepository.findById(tourId).ifPresent(t -> {
            t.setCuposDisponibles(t.getCuposDisponibles() + 1);

            // Si estaba AGOTADO y ahora recuperó un cupo, lo ponemos ACTIVO de nuevo
            if (t.getEstado() == EstadoTour.AGOTADO) {
                t.setEstado(EstadoTour.ACTIVO);
            }
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