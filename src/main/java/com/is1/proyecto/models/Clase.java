package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("clases")
public class Clase extends Model {

    // Getter y Setter para 'id'
    public Integer getIdClase() {
        return getInteger("id");
    }

    // Getter y Setter para 'nombre'
    public String getNombre() {
        return getString("nombre");
    }

    public void setNombre(String nombre) {
        set("nombre", nombre);
    }

    // Getter y Setter para 'descripcion'
    public String getDescripcion() {
        return getString("descripcion");
    }

    public void setDescripcion(String descripcion) {
        set("descripcion", descripcion);
    }

    // Getter y Setter para 'profesor_id' (la clave foránea que lo une al profesor)
    public Integer getProfesorId() {
        return getInteger("profesor_id");
    }

    public void setProfesorId(Integer profesorId) {
        set("profesor_id", profesorId);
    }
}