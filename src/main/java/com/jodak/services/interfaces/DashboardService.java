package com.jodak.services.interfaces;

import com.jodak.dtos.dashboard.DashboardResponse;
import com.jodak.dtos.dashboard.PointsRankingRow;

import java.util.List;

/**
 * Calcul des statistiques du tableau de bord.
 */
public interface DashboardService {

    DashboardResponse getDashboard();

    List<PointsRankingRow> getPointsRanking();
}
