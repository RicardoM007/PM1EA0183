package com.uth.agendacontactos.models;

// MODELO: representa cada contacto guardado.
public class Contacto {
    private int id;
    private String pais;
    private String nombre;
    private String telefono;
    private String nota;
    private String imagen;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    // Extraemos el código del país para presentar y marcar el teléfono.
    public String getCodigoPais() {
        int inicio = pais.lastIndexOf('(');
        int fin = pais.lastIndexOf(')');
        if (inicio >= 0 && fin > inicio) {
            return pais.substring(inicio + 1, fin);
        }
        return "504";
    }

    public String getNumeroCompleto() {
        return "+" + getCodigoPais() + " " + telefono;
    }

    @Override
    public String toString() {
        return nombre + "  |  " + getNumeroCompleto();
    }
}
