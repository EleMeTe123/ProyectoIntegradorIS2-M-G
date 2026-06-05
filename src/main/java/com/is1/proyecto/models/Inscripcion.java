package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("inscripciones")
public class Inscripcion extends Model {
    
    //Getter y Setter para 'id'
    public Integer getClaseId() {
        return getInteger("clase_id");
    }

    public void setClaseId(Integer claseId) {
        set("clase_id", claseId);
    }

    //Getter y Setter para 'UserId'
    public Integer getUserId() {
        return getInteger("user_id");
    }

    public void setUserId(Integer userId) {
        set("user_id", userId);
    }
}