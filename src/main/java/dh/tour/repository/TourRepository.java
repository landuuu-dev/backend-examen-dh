package dh.tour.repository;

import dh.tour.model.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TourRepository extends MongoRepository<Tour, String> {

    boolean existsByCategoriaId(String categoriaId);

    // Consulta flexible usando Regex de MongoDB
    @Query("{ " +
            "  '$and': [ " +
            "    { '$or': [ { '?0': null }, { 'nombre': { '$regex': ?0, '$options': 'i' } } ] }, " +
            "    { '$or': [ { '?1': null }, { 'precio': { '$lte': ?1 } } ] } " +
            "  ] " +
            "}")
    Page<Tour> buscarTours(String nombre, Integer precioMax, Pageable pageable);
}