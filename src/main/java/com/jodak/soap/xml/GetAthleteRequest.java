package com.jodak.soap.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Requête SOAP de consultation d'un athlète par identifiant.
 */
@XmlRootElement(name = "GetAthleteRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetAthleteRequest {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
