package dh.tour.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @Email(message = "Formato de correo inválido")
    @NotBlank
    private String correo;
    @ToString.Exclude
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;


}
