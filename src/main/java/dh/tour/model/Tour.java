package dh.tour.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "tours")
public class Tour {
    @Id
    private String id;
    private String nombre;
    private Categoria categoria;
    private String descripcion;
    private String ubicacion;
    private int precio;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int cuposTotales;
    private int cuposDisponibles;

    @Builder.Default
    private EstadoTour estado = EstadoTour.ACTIVO;

    @Builder.Default
    private List<String> imagenes = new ArrayList<>();
}