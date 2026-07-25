package com.jodak.services.interfaces;

import com.jodak.dtos.medaltable.MedalTableRowResponse;

import java.util.List;

/**
 * Calcul du tableau officiel des médailles.
 */
public interface MedalTableService {

    List<MedalTableRowResponse> getMedalTable();
}
