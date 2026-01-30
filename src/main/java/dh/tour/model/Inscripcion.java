package dh.tour.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "inscripciones")
public class Inscripcion {
    @Id
    private String id;
    private String tourId;
    private String usuarioId;
    private String nombreUsuario; // Para facilitar lectura al Admin
    private String nombreTour;
    private LocalDateTime fechaInscripcion;

    public Inscripcion() {
    }

    public Inscripcion(String id, String tourId, String usuarioId, String nombreUsuario, String nombreTour, LocalDateTime fechaInscripcion) {
        this.id = id;
        this.tourId = tourId;
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.nombreTour = nombreTour;
        this.fechaInscripcion = fechaInscripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTourId() {
        return tourId;
    }

    public void setTourId(String tourId) {
        this.tourId = tourId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombreTour() {
        return nombreTour;
    }

    public void setNombreTour(String nombreTour) {
        this.nombreTour = nombreTour;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDateTime fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }
}