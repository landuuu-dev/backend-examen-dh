package dh.tour.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CategoriaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 20, message = "La descripción debe tener al menos 20 caracteres para ser informativa")
    private String descripcion;
}
