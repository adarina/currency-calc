package com.ada.currencycalc.service;

import com.ada.currencycalc.model.ExchangeRate;
import com.ada.currencycalc.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {
    @Mock
    ExchangeRateRepository exchangeRateRepository;

    @Mock
    Clock clock;

    @InjectMocks
    CurrencyService currencyService;

    private LocalDate today;

    @Test
    void shouldConvertCurrency() {
        String from = "USD";
        String to = "EUR";
        double amount = 100;
        ZoneId zoneId = ZoneId.of("UTC");
        Instant instant = Instant.parse("2024-07-05T10:00:00Z");

        when(clock.getZone()).thenReturn(zoneId);
        when(clock.instant()).thenReturn(instant);

        today = LocalDate.ofInstant(instant, zoneId);

        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(from, today))
                .thenReturn(Optional.of(new ExchangeRate(from, 1.2, today)));
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(to, today))
                .thenReturn(Optional.of(new ExchangeRate(to, 0.8, today)));

        double result = currencyService.convertCurrency(from, amount, to);
        assertEquals(150.0, result, 0.00001);
    }

    @Test
    void shouldConvertSameCurrency() {
        String currency = "USD";
        double amount = 100;
        ZoneId zoneId = ZoneId.of("UTC");
        Instant instant = Instant.parse("2024-07-05T10:00:00Z");

        when(clock.getZone()).thenReturn(zoneId);
        when(clock.instant()).thenReturn(instant);

        today = LocalDate.ofInstant(instant, zoneId);

        double result = currencyService.convertCurrency(currency, amount, currency);
        assertEquals(amount, result, 0.00001);
    }

    @Test
    void shouldConvertCurrencyWithDifferentExchangeRates() {
        String from = "USD";
        String to = "EUR";
        double amount = 100;
        ZoneId zoneId = ZoneId.of("UTC");
        Instant instant = Instant.parse("2024-07-05T10:00:00Z");

        when(clock.getZone()).thenReturn(zoneId);
        when(clock.instant()).thenReturn(instant);

        today = LocalDate.ofInstant(instant, zoneId);

        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(from, today))
                .thenReturn(Optional.of(new ExchangeRate(from, 1.5, today)));
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(to, today))
                .thenReturn(Optional.of(new ExchangeRate(to, 0.9, today)));

        double result = currencyService.convertCurrency(from, amount, to);
        assertEquals(166.66667, result, 0.00001);
    }

    @Test
    void shouldConvertCurrencyWithZeroAmount() {
        String from = "USD";
        String to = "EUR";
        double amount = 0;
        ZoneId zoneId = ZoneId.of("UTC");
        Instant instant = Instant.parse("2024-07-05T10:00:00Z");

        when(clock.getZone()).thenReturn(zoneId);
        when(clock.instant()).thenReturn(instant);

        today = LocalDate.ofInstant(instant, zoneId);

        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(from, today))
                .thenReturn(Optional.of(new ExchangeRate(from, 1.2, today)));
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(to, today))
                .thenReturn(Optional.of(new ExchangeRate(to, 0.8, today)));

        double result = currencyService.convertCurrency(from, amount, to);
        assertEquals(0, result, 0.00001);
    }

    @Test
    void shouldThrowExceptionWhenCurrencyCodeIsBlank() {

        String from = "";
        String to = "EUR";
        double amount = 100;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> currencyService.convertCurrency(from, amount, to));

        assertEquals("Currency code cannot be blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForBlankCurrencyCode() {
        String from = "";
        String to = "EUR";
        double amount = 100;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> currencyService.convertCurrency(from, amount, to));

        assertEquals("Currency code cannot be blank", exception.getMessage());
    }
}
