package dh.tour.repository;

import com.mongodb.client.MongoIterable;
import dh.tour.model.Tour;
import dh.tour.model.EstadoTour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TourRepository extends MongoRepository<Tour, String> {


    boolean existsByCategoriaId(String categoriaId);

    Page<Tour> findByNombreContainingIgnoreCaseAndPrecioLessThanEqual(
            String nombre, int precio, Pageable pageable);
}