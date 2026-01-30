package dh.tour.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "tours")
public class Tour {
    @Id
    private String id;
    private String nombre;
    private Categoria categoria; // <-- embebida correctamente
    private String descripcion;
    private String ubicacion;
    private int precio;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int cuposTotales;
    private int cuposDisponibles;
    private boolean disponible = true;
    private List<String> imagenes;


    // Metodo para verificar disponibilidad antes de guardar o listar
    public boolean checkDisponibilidad() {
        if (fechaFin == null) {
            return this.disponible; // O maneja el error como prefieras
        }
        if (LocalDate.now().isAfter(fechaFin) || cuposDisponibles <= 0) {
            this.disponible = false;
        }
        return this.disponible;
    }


    // Constructor vacío
    public Tour() {
        this.imagenes = new ArrayList<>();
    }

    // Reemplaza tus constructores por estos:

    // Constructor sin imágenes
    public Tour(String nombre, Categoria categoria, String descripcion, String ubicacion,
                int precio, LocalDate fechaInicio, LocalDate fechaFin, int cuposTotales, boolean disponible) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.precio = precio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.cuposTotales = cuposTotales;
        // ASIGNACIÓN AUTOMÁTICA
        this.cuposDisponibles = cuposTotales;
        this.disponible = disponible;
        this.imagenes = new ArrayList<>();
    }

    // Constructor con imágenes
    public Tour(String nombre, Categoria categoria, String descripcion, String ubicacion,
                int precio, LocalDate fechaInicio, LocalDate fechaFin, int cuposTotales,
                boolean disponible, List<String> imagenes) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.precio = precio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.cuposTotales = cuposTotales;
        // ASIGNACIÓN AUTOMÁTICA
        this.cuposDisponibles = cuposTotales;
        this.disponible = disponible;
        this.imagenes = imagenes != null ? imagenes : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getCuposTotales() {
        return cuposTotales;
    }

    public void setCuposTotales(int cuposTotales) {
        this.cuposTotales = cuposTotales;
    }

    public int getCuposDisponibles() {
        return cuposDisponibles;
    }

    public void setCuposDisponibles(int cuposDisponibles) {
        this.cuposDisponibles = cuposDisponibles;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
