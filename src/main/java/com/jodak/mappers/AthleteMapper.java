package com.jodak.mappers;

import com.jodak.dtos.athlete.AthletePatchRequest;
import com.jodak.dtos.athlete.AthleteRequest;
import com.jodak.dtos.athlete.AthleteResponse;
import com.jodak.entities.Athlete;
import com.jodak.entities.Country;
import com.jodak.entities.Discipline;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Conversion manuelle entre l'entité {@link Athlete} et ses DTO. La résolution des références
 * (nation, discipline) est effectuée par le service ; le mapper reçoit les entités déjà résolues.
 */
@Component
@RequiredArgsConstructor
public class AthleteMapper {

    private final CountryMapper countryMapper;
    private final DisciplineMapper disciplineMapper;

    public Athlete toEntity(AthleteRequest request, Country country, Discipline discipline) {
        return Athlete.builder()
                .lastName(request.lastName().trim())
                .firstName(request.firstName().trim())
                .gender(request.gender())
                .birthDate(request.birthDate())
                .country(country)
                .discipline(discipline)
                .heightCm(request.heightCm())
                .weightKg(request.weightKg())
                .build();
    }

    public void updateEntity(Athlete entity, AthleteRequest request, Country country, Discipline discipline) {
        entity.setLastName(request.lastName().trim());
        entity.setFirstName(request.firstName().trim());
        entity.setGender(request.gender());
        entity.setBirthDate(request.birthDate());
        entity.setCountry(country);
        entity.setDiscipline(discipline);
        entity.setHeightCm(request.heightCm());
        entity.setWeightKg(request.weightKg());
    }

    /**
     * Applique les champs présents d'un PATCH. {@code country} / {@code discipline} sont fournis
     * (déjà résolus) uniquement lorsque leur identifiant est présent dans la requête, sinon nuls.
     */
    public void applyPatch(Athlete entity, AthletePatchRequest request,
                           Country country, Discipline discipline) {
        if (request.lastName() != null) {
            entity.setLastName(request.lastName().trim());
        }
        if (request.firstName() != null) {
            entity.setFirstName(request.firstName().trim());
        }
        if (request.gender() != null) {
            entity.setGender(request.gender());
        }
        if (request.birthDate() != null) {
            entity.setBirthDate(request.birthDate());
        }
        if (country != null) {
            entity.setCountry(country);
        }
        if (discipline != null) {
            entity.setDiscipline(discipline);
        }
        if (request.heightCm() != null) {
            entity.setHeightCm(request.heightCm());
        }
        if (request.weightKg() != null) {
            entity.setWeightKg(request.weightKg());
        }
    }

    public AthleteResponse toResponse(Athlete entity) {
        return new AthleteResponse(
                entity.getId(),
                entity.getLastName(),
                entity.getFirstName(),
                entity.getGender(),
                entity.getBirthDate(),
                entity.ageAt(LocalDate.now()),
                countryMapper.toSummary(entity.getCountry()),
                disciplineMapper.toSummary(entity.getDiscipline()),
                entity.getHeightCm(),
                entity.getWeightKg(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
