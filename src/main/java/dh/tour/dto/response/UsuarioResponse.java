package dh.tour.dto.response;
import dh.tour.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@AllArgsConstructor
@Builder
@Jacksonized
public class UsuarioResponse {

    private String id;
    private String nombre;
    private String correo;
    private Rol rol;

}
