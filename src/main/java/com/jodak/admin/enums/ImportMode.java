package com.jodak.admin.enums;

/**
 * Mode d'exécution d'un import : validation seule (aucune écriture) ou import réel.
 */
public enum ImportMode {
    DRY_RUN,
    COMMIT
}
