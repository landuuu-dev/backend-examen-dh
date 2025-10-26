package dh.tour.repository;

import dh.tour.model.Categoria;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoriaRepository extends MongoRepository<Categoria, String> {
}
