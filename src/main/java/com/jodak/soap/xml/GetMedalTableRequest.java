package com.jodak.soap.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Requête SOAP du tableau des médailles (sans paramètre).
 */
@XmlRootElement(name = "GetMedalTableRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetMedalTableRequest {
}
