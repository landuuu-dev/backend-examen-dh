package dh.tour.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String token;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
}