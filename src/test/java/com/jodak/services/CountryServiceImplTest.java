package com.jodak.services;

import com.jodak.dtos.country.CountryRequest;
import com.jodak.exceptions.ConflictException;
import com.jodak.exceptions.ResourceNotFoundException;
import com.jodak.mappers.CountryMapper;
import com.jodak.repositories.CountryRepository;
import com.jodak.services.implementations.CountryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryServiceImplTest {

    @Mock
    private CountryRepository repository;

    // Mapper réel : sa logique de normalisation est simple et déterministe.
    private final CountryMapper mapper = new CountryMapper();

    private CountryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CountryServiceImpl(repository, mapper);
    }

    @Test
    @DisplayName("create lève un conflit lorsque le code existe déjà")
    void createThrowsConflictWhenCodeExists() {
        when(repository.existsByCodeIgnoreCase("FRA")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CountryRequest("fra", "France")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("FRA");

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("getById lève une exception lorsque la nation est absente")
    void getByIdThrowsWhenMissing() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }
}
