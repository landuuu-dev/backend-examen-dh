package dh.tour.repository;

import dh.tour.model.Inscripcion;
import dh.tour.model.Tour;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends MongoRepository<Inscripcion, String> {
    List<Inscripcion> findByTourId(String tourId);
    List<Inscripcion> findByUsuarioId(String usuarioId); // Cambiado de Object a List
    boolean existsByUsuarioIdAndTourId(String id, String id1);
    Optional<Inscripcion> findByTourIdAndUsuarioId(String tourId, String usuarioId);

}