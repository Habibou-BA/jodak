package com.jodak.soap.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Représentation XML d'une ligne du tableau des médailles (contrat SOAP).
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MedalTableRow", propOrder = {
        "rank", "countryCode", "countryName", "gold", "silver", "bronze", "total"})
public class MedalTableRow {

    private int rank;
    private String countryCode;
    private String countryName;
    private long gold;
    private long silver;
    private long bronze;
    private long total;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public long getGold() {
        return gold;
    }

    public void setGold(long gold) {
        this.gold = gold;
    }

    public long getSilver() {
        return silver;
    }

    public void setSilver(long silver) {
        this.silver = silver;
    }

    public long getBronze() {
        return bronze;
    }

    public void setBronze(long bronze) {
        this.bronze = bronze;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
