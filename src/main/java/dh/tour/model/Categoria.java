package dh.tour.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categorias")
public class Categoria {

    @Id
    private String id;
    private String nombre;
    private String descripcion;
    private String imagen1; // NUEVO
    private String imagen2;
    private String imagen3;

    public Categoria() {
    }

    public Categoria(String nombre, String descripcion, String imagen1, String imagen2, String imagen3) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagen1 = imagen1;
        this.imagen2 = imagen2;
        this.imagen3 = imagen3;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagen1() {
        return imagen1;
    }

    public void setImagen1(String imagen1) {
        this.imagen1 = imagen1;
    }

    public String getImagen2() {
        return imagen2;
    }

    public void setImagen2(String imagen2) {
        this.imagen2 = imagen2;
    }

    public String getImagen3() {
        return imagen3;
    }

    public void setImagen3(String imagen3) {
        this.imagen3 = imagen3;
    }
}