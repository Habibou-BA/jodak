package com.jodak.soap.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Réponse SOAP contenant les lignes du tableau des médailles.
 */
@XmlRootElement(name = "GetMedalTableResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetMedalTableResponse {

    @XmlElement(name = "row")
    private List<MedalTableRow> row = new ArrayList<>();

    public List<MedalTableRow> getRow() {
        return row;
    }
}
