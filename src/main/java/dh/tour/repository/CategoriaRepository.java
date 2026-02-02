package dh.tour.repository;

import dh.tour.model.Categoria;
import dh.tour.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CategoriaRepository extends MongoRepository<Categoria, String> {
    Optional<Categoria> findById(String id);

}
