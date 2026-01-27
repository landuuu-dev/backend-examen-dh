package dh.tour.dto;

import dh.tour.model.Rol;

public class RegisterRequest {

    private String nombre;
    private String correo;
    private String password;

    public RegisterRequest() {}

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
