package dh.tour.dto.request;

import dh.tour.model.EstadoTour;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TourRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 5, max = 100, message = "El nombre debe tener entre 5 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "Debes seleccionar una categoría")
    private String categoriaId;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 20, message = "La descripción debe tener al menos 20 caracteres para ser informativa")
    private String descripcion; // <--- AGREGADO

    @NotBlank(message = "La ubicación es obligatoria")
    private String ubicacion;

    @NotNull(message = "El estado es obligatorio y debe ser ACTIVO, AGOTADO o CANCELADO")
    private EstadoTour estado;

    @Positive(message = "El precio debe ser mayor a cero")
    private int precio;

    @Future(message = "La fecha de inicio debe ser en el futuro")
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @Min(value = 1, message = "Debe haber al menos 1 cupo disponible")
    private int cuposTotales;
}