package com.jodak.repositories.projections;

/**
 * Projection d'agrégation : nombre de médailles par nation, issue d'une requête groupée.
 */
public interface CountryMedalCount {

    Long getCountryId();

    String getCountryCode();

    String getCountryName();

    long getGold();

    long getSilver();

    long getBronze();
}
