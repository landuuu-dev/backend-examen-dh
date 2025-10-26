package dh.tour.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private List<String> imagenes;

    // Constructor vacío
    public Tour() {
        this.imagenes = new ArrayList<>();
    }

    // Constructor sin imágenes
    public Tour(String nombre, Categoria categoria, String descripcion, String ubicacion, int precio) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.precio = precio;
        this.imagenes = new ArrayList<>();
    }

    // Constructor con imágenes
    public Tour(String nombre, Categoria categoria, String descripcion, String ubicacion, int precio, List<String> imagenes) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.precio = precio;
        this.imagenes = imagenes != null ? imagenes : new ArrayList<>();
    }

    // Getters y setters
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
        this.imagenes = imagenes != null ? imagenes : new ArrayList<>();
    }
}
