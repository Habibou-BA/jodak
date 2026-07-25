package com.jodak.constants;

/**
 * Chemins de base de l'API REST. Toutes les ressources sont préfixées par {@code /api/v1}.
 */
public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String API_V1 = "/api/v1";
    public static final String DISCIPLINES = API_V1 + "/disciplines";
    public static final String COUNTRIES = API_V1 + "/countries";
    public static final String ATHLETES = API_V1 + "/athletes";
    public static final String EPREUVES = API_V1 + "/epreuves";
    public static final String RESULTATS = API_V1 + "/resultats";
    public static final String MEDAL_TABLE = API_V1 + "/tableau-medailles";
}
