package com.jodak.repositories;

import com.jodak.entities.Epreuve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;

/**
 * Accès aux données des épreuves. La recherche multicritère passe par les {@code Specification}.
 */
public interface EpreuveRepository
        extends JpaRepository<Epreuve, Long>, JpaSpecificationExecutor<Epreuve> {

    boolean existsByLabelIgnoreCaseAndDiscipline_IdAndEventDate(
            String label, Long disciplineId, LocalDate eventDate);

    boolean existsByLabelIgnoreCaseAndDiscipline_IdAndEventDateAndIdNot(
            String label, Long disciplineId, LocalDate eventDate, Long id);
}
