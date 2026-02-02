package dh.tour.model;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "inscripciones")
public class Inscripcion {
    @Id
    private String id;
    private String tourId;
    private String usuarioId;
    private String nombreUsuario;
    private String nombreTour;
    private LocalDateTime fechaInscripcion;


}