package com.ada.currencycalc.service;

import com.ada.currencycalc.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CleanupServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private CleanupService cleanupService;

    @Test
    void shouldClearPreviousDayData() {

        ZoneId zoneId = ZoneId.of("UTC");
        Instant instant = Instant.parse("2024-07-15T10:00:00Z");
        when(clock.getZone()).thenReturn(zoneId);
        when(clock.instant()).thenReturn(instant);

        LocalDate today = LocalDate.ofInstant(instant, zoneId);
        LocalDate yesterday = today.minusDays(1);

        cleanupService.clearPreviousDayData();

        verify(exchangeRateRepository).deleteByEffectiveDate(yesterday);
    }
}
