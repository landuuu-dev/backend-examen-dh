package dh.tour.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor  // Reemplaza tu constructor vacío
@AllArgsConstructor // Reemplaza tu constructor con todos los campos
@Builder
@Document(collection = "categorias")
public class Categoria {

    @Id
    private String id;
    private String nombre;
    private String descripcion;
    private List<String> imagenes;


}