package com.jodak.mappers;

import com.jodak.dtos.country.CountryRequest;
import com.jodak.dtos.country.CountryResponse;
import com.jodak.dtos.country.CountrySummary;
import com.jodak.entities.Country;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Conversion manuelle entre l'entité {@link Country} et ses DTO.
 */
@Component
public class CountryMapper {

    public Country toEntity(CountryRequest request) {
        Country country = new Country();
        country.setCode(normalizeCode(request.code()));
        country.setName(normalizeName(request.name()));
        return country;
    }

    public CountryResponse toResponse(Country entity) {
        return new CountryResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public CountrySummary toSummary(Country entity) {
        return new CountrySummary(entity.getId(), entity.getCode(), entity.getName());
    }

    public String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    public String normalizeName(String value) {
        return value == null ? null : value.trim();
    }
}
