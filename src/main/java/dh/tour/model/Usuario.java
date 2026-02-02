package dh.tour.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;
    private String nombre;
    private String correo;
    @JsonIgnore // Evita que el password se incluya en cualquier respuesta JSON accidentalmente
    private String password;
    private Rol rol;
    private List<String> favoritos = new ArrayList<>();
    private String resetToken;
    private java.time.LocalDateTime resetTokenExpiration;


}