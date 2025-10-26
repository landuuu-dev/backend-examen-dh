package dh.tour.repository;

import dh.tour.model.Tour;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourRepository extends MongoRepository<Tour, String> {
    // Verificar si existe un tour con este nombre (case insensitive)
    boolean existsByNombreIgnoreCase(String nombre);

    // Verificar si existe un tour con este nombre pero distinto id (para update)
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, String id);
    List<Tour> findByCategoriaId(String categoriaId);

}
