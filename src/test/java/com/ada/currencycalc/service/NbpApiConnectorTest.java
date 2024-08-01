package com.ada.currencycalc.service;

import com.ada.currencycalc.model.ExchangeRateDTO;
import com.ada.currencycalc.model.RateDTO;
import com.ada.currencycalc.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NbpApiConnectorTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private NbpApiConnector nbpApiConnector;


    private final ZoneId zoneId = ZoneId.of("UTC");
    private final Instant fixedInstant = Instant.parse("2024-08-01T10:00:00Z");
    private final LocalDate fixedDate = LocalDate.ofInstant(fixedInstant, zoneId);

    @BeforeEach
    void setUp() {
        when(clock.getZone()).thenReturn(zoneId);
        when(clock.instant()).thenReturn(fixedInstant);
    }

    @Test
    void shouldFetchAndSaveExchangeRateSuccessfully() {
        String currencyCode = "USD";
        BigDecimal expectedRate = BigDecimal.valueOf(1.2);
        ExchangeRateDTO exchangeRateDTO = new ExchangeRateDTO();
        RateDTO rateDTO = new RateDTO();
        rateDTO.setMid(expectedRate);
        exchangeRateDTO.setRates(Collections.singletonList(rateDTO));

        ResponseEntity<ExchangeRateDTO> responseEntity = ResponseEntity.ok(exchangeRateDTO);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(HttpEntity.EMPTY), eq(ExchangeRateDTO.class)))
                .thenReturn(responseEntity);

        BigDecimal rate = nbpApiConnector.fetchAndSaveExchangeRate(currencyCode);

        assertEquals(expectedRate, rate);
        verify(exchangeRateRepository).save(argThat(exchangeRate ->
                currencyCode.equals(exchangeRate.getCurrencyCode()) &&
                        expectedRate.equals(exchangeRate.getRate()) &&
                        fixedDate.equals(exchangeRate.getEffectiveDate())));
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
