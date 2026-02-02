package dh.tour.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder

public class CategoriaResponse {
    private String id;
    private String nombre;
    private String descripcion;
    private List<String> imagenes;
}