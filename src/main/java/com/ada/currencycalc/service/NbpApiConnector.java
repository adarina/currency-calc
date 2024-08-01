package com.ada.currencycalc.service;

import com.ada.currencycalc.exceptions.ExternalApiException;
import com.ada.currencycalc.model.ExchangeRate;
import com.ada.currencycalc.model.ExchangeRateDTO;
import com.ada.currencycalc.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NbpApiConnector {

    private final RestTemplate restTemplate;

    private final ExchangeRateRepository exchangeRateRepository;

    private final Clock clock;

    @Value("${nbp.api.url}")
    private String nbpApiUrl;

    private boolean isValidExchangeRateDTO(ExchangeRateDTO exchangeRateDTO) {
        return exchangeRateDTO != null && exchangeRateDTO.getRates() != null && !exchangeRateDTO.getRates().isEmpty();
    }

    public BigDecimal fetchAndSaveExchangeRate(String currencyCode) {
        String url = String.format(nbpApiUrl + "exchangerates/rates/A/%s/", currencyCode);
        ResponseEntity<ExchangeRateDTO> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, ExchangeRateDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            log.error("Error fetching exchange rate from external API: {}", ex.getMessage());
            throw new ExternalApiException(ex.getMessage());

        }
        ExchangeRateDTO exchangeRateDTO = response.getBody();
        if (!isValidExchangeRateDTO(exchangeRateDTO)) {
            String errorMessage = "Unable to fetch exchange rate for currency: " + currencyCode;
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        BigDecimal rate = exchangeRateDTO.getRates().get(0).getMid();
        ExchangeRate exchangeRate = new ExchangeRate(currencyCode, rate, LocalDate.now(clock));
        exchangeRateRepository.save(exchangeRate);
        return rate;
    }
}
