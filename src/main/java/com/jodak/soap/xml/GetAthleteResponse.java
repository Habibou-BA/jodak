package com.jodak.soap.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Réponse SOAP contenant un athlète.
 */
@XmlRootElement(name = "GetAthleteResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetAthleteResponse {

    private Athlete athlete;

    public Athlete getAthlete() {
        return athlete;
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
    }
}
