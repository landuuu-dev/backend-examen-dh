package dh.tour.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // ¡Súper útil! Si algo es nulo, no lo envía al frontend
public class TourResponse {

    private String id;

    private String nombre;

    private String nombreCategoria;

    private String descripcion;

    private String ubicacion;

    private int precio;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate fechaInicio;

    private int cuposDisponibles;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> imagenes;
}