package com.antigaspi.service;

import com.antigaspi.service.dto.StatsAdmin;
import com.antigaspi.service.dto.StatsCommercant;

public interface StatsService {

    /** Lève BusinessException si l'utilisateur n'a pas de commerce. */
    StatsCommercant statsCommercant(Long commercantId);

    StatsAdmin statsAdmin();
}
