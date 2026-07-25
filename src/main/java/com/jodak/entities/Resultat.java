package com.jodak.entities;

import com.jodak.enums.Medal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Résultat d'un athlète dans une épreuve : son rang, dont découle la médaille.
 */
@Entity
@Table(name = "resultat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resultat extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "epreuve_id", nullable = false)
    private Epreuve epreuve;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "medal", length = 10)
    private Medal medal;

    /**
     * Attribue automatiquement la médaille à partir du rang (RM-12). Règle métier intrinsèque.
     */
    public void assignMedalFromRank() {
        this.medal = rankPosition == null ? null : Medal.fromRank(rankPosition);
    }
}
