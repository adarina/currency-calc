package com.ada.currencycalc.service;

import com.ada.currencycalc.model.ExchangeRate;
import com.ada.currencycalc.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {
    @Mock
    ExchangeRateRepository exchangeRateRepository;

    @Mock
    Clock clock;

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    CurrencyService currencyService;

    private LocalDate today;

    @Value("${nbp.api.url}")
    private String nbpApiUrl;

    @Test
    void shouldConvertCurrency() {
        String from = "USD";
        String to = "EUR";
        BigDecimal amount = BigDecimal.valueOf(100);
        ZoneId zoneId = ZoneId.of("UTC");
        Instant instant = Instant.parse("2024-07-05T10:00:00Z");

        when(clock.getZone()).thenReturn(zoneId);
        when(clock.instant()).thenReturn(instant);

        today = LocalDate.ofInstant(instant, zoneId);

        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(from, today))
                .thenReturn(Optional.of(new ExchangeRate(from, BigDecimal.valueOf(1.2), today)));
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(to, today))
                .thenReturn(Optional.of(new ExchangeRate(to, BigDecimal.valueOf(0.8), today)));

        BigDecimal result = currencyService.convertCurrency(from, amount, to);
        assertEquals(BigDecimal.valueOf(150.0).setScale(2, RoundingMode.HALF_UP), result);
    }

//    @Test
//    void shouldConvertSameCurrency() {
//        String currency = "USD";
//        BigDecimal amount = BigDecimal.valueOf(100);
//        ZoneId zoneId = ZoneId.of("UTC");
//        Instant instant = Instant.parse("2024-07-05T10:00:00Z");
//
//        when(clock.getZone()).thenReturn(zoneId);
//        when(clock.instant()).thenReturn(instant);
//
//        LocalDate today = LocalDate.ofInstant(instant, zoneId);
//
//        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(currency, today))
//                .thenReturn(Optional.empty());
//
//        RateDTO rateDTO = new RateDTO();
//        rateDTO.setMid(BigDecimal.ONE);
//        ExchangeRateDTO exchangeRateDTO = new ExchangeRateDTO();
//        exchangeRateDTO.setRates(Collections.singletonList(rateDTO));
//
//        ResponseEntity<ExchangeRateDTO> responseEntity = ResponseEntity.ok(exchangeRateDTO);
//
//        when(restTemplate.exchange(nbpApiUrl + "exchangerates/rates/A/USD/", HttpMethod.GET, HttpEntity.EMPTY, ExchangeRateDTO.class))
//                .thenReturn(responseEntity);
//
//        BigDecimal result = currencyService.convertCurrency(currency, amount, currency);
//        assertEquals(amount.setScale(2, RoundingMode.HALF_UP), result);
//    }

    @Test
    void shouldConvertCurrencyWithDifferentExchangeRates() {
        String from = "USD";
        String to = "EUR";
        BigDecimal amount = BigDecimal.valueOf(100);
        ZoneId zoneId = ZoneId.of("UTC");
        Instant instant = Instant.parse("2024-07-05T10:00:00Z");

        when(clock.getZone()).thenReturn(zoneId);
        when(clock.instant()).thenReturn(instant);

        today = LocalDate.ofInstant(instant, zoneId);

        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(from, today))
                .thenReturn(Optional.of(new ExchangeRate(from, BigDecimal.valueOf(1.5), today)));
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(to, today))
                .thenReturn(Optional.of(new ExchangeRate(to, BigDecimal.valueOf(0.9), today)));

        BigDecimal result = currencyService.convertCurrency(from, amount, to);
        assertEquals(BigDecimal.valueOf(166.67).setScale(2, RoundingMode.HALF_UP), result);
    }

    @Test
    void shouldConvertCurrencyWithZeroAmount() {
        String from = "USD";
        String to = "EUR";
        BigDecimal amount = BigDecimal.ZERO;
        ZoneId zoneId = ZoneId.of("UTC");
        Instant instant = Instant.parse("2024-07-05T10:00:00Z");

        when(clock.getZone()).thenReturn(zoneId);
        when(clock.instant()).thenReturn(instant);

        today = LocalDate.ofInstant(instant, zoneId);

        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(from, today))
                .thenReturn(Optional.of(new ExchangeRate(from, BigDecimal.valueOf(1.2), today)));
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(to, today))
                .thenReturn(Optional.of(new ExchangeRate(to, BigDecimal.valueOf(0.8), today)));

        BigDecimal result = currencyService.convertCurrency(from, amount, to);
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result);
    }

    @Test
    void shouldThrowExceptionWhenCurrencyCodeIsBlank() {
        String from = "";
        String to = "EUR";
        BigDecimal amount = BigDecimal.valueOf(100);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> currencyService.convertCurrency(from, amount, to));

        assertEquals("Currency code cannot be blank, empty or null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForBlankCurrencyCode() {
        String from = "";
        String to = "EUR";
        BigDecimal amount = BigDecimal.valueOf(100);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> currencyService.convertCurrency(from, amount, to));

        assertEquals("Currency code cannot be blank, empty or null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        String from = "USD";
        String to = "EUR";
        BigDecimal amount = BigDecimal.valueOf(-100);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> currencyService.convertCurrency(from, amount, to));

        assertEquals("Amount cannot be negative", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFromCurrencyIsNull() {
        String from = null;
        String to = "EUR";
        BigDecimal amount = BigDecimal.valueOf(100);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> currencyService.convertCurrency(from, amount, to));

        assertEquals("Currency code cannot be blank, empty or null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenToCurrencyIsNull() {
        String from = "USD";
        String to = null;
        BigDecimal amount = BigDecimal.valueOf(100);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> currencyService.convertCurrency(from, amount, to));

        assertEquals("Currency code cannot be blank, empty or null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFromCurrencyIsBlank() {
        String from = "   ";
        String to = "EUR";
        BigDecimal amount = BigDecimal.valueOf(100);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> currencyService.convertCurrency(from, amount, to));

        assertEquals("Currency code cannot be blank, empty or null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenToCurrencyIsBlank() {
        String from = "USD";
        String to = "   ";
        BigDecimal amount = BigDecimal.valueOf(100);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> currencyService.convertCurrency(from, amount, to));

        assertEquals("Currency code cannot be blank, empty or null", exception.getMessage());
    }

//    @Test
//    void shouldHandleHttpClientErrorException() {
//        String from = "USD";
//        BigDecimal amount = BigDecimal.valueOf(100);
//        String to = "EUR";
//
//        ZoneId zoneId = ZoneId.of("UTC");
//        Instant instant = Instant.parse("2024-07-05T10:00:00Z");
//
//        when(clock.getZone()).thenReturn(zoneId);
//        when(clock.instant()).thenReturn(instant);
//
//        today = LocalDate.ofInstant(instant, zoneId);
//
//        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(from, today))
//                .thenReturn(Optional.empty());
//
//        String url = nbpApiUrl + "exchangerates/rates/A/" + from + "/";
//
//        when(restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, ExchangeRateDTO.class))
//                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
//
//        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
//                () -> currencyService.convertCurrency(from, amount, to));
//
//        assertEquals("404 NOT_FOUND", exception.getMessage());
//    }
}
