package com.jodak.admin.enums;

/**
 * Type d'entité ciblée par un import. (Discipline et Athlète implémentés ; les autres sont prévus.)
 */
public enum ImportJobType {
    COUNTRY,
    DISCIPLINE,
    ATHLETE,
    EPREUVE,
    RESULTAT,
    /** Import « système » : un classeur XLSX multi-feuilles chargé en une passe (Nations → Disciplines → Épreuves → Athlètes). */
    SYSTEME
}
