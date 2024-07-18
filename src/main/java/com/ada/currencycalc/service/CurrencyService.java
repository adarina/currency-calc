package com.ada.currencycalc.service;

import com.ada.currencycalc.exceptions.ExternalApiException;
import com.ada.currencycalc.model.ExchangeRateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.ada.currencycalc.repository.ExchangeRateRepository;
import com.ada.currencycalc.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
public class CurrencyService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final RestTemplate restTemplate;

    private final Clock clock;

    @Value("${nbp.api.url}")
    private String nbpApiUrl;

    public CurrencyService(ExchangeRateRepository exchangeRateRepository, Clock clock, RestTemplate restTemplate) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.restTemplate = restTemplate;
        this.clock = clock;
    }

    public BigDecimal convertCurrency(String from, BigDecimal amount, String to) {
        log.info("Converting currency from {} to {} with amount {}", from, to, amount);
        if (isBlank(from) || isBlank(to)) {
            log.error("Currency code cannot be blank, empty or null");
            throw new IllegalArgumentException("Currency code cannot be blank, empty or null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            log.error("Amount cannot be negative");
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        BigDecimal fromRate = getExchangeRate(from);
        BigDecimal toRate = getExchangeRate(to);
        BigDecimal result = amount.multiply(fromRate).divide(toRate, 2, RoundingMode.HALF_UP);
        log.info("Conversion result: {}", result);
        return result;
    }

    private BigDecimal getExchangeRate(String currencyCode) {
        LocalDate today = LocalDate.now(clock);
        log.info("Fetching exchange rate for {} on {}", currencyCode, today);
        Optional<ExchangeRate> optionalExchangeRate = exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(currencyCode, today);
        if (optionalExchangeRate.isPresent()) {
            log.info("Found exchange rate in database: {}", optionalExchangeRate.get().getRate());
            return optionalExchangeRate.get().getRate();
        } else {
            log.info("Exchange rate not found in database, fetching from external API");
            BigDecimal newRate = fetchAndSaveExchangeRate(currencyCode);
            log.info("Added new exchange rate to database: currencyCode={}, date={}, rate={}", currencyCode, today, newRate);
            return newRate;
        }
    }

    private boolean isValidExchangeRateDTO(ExchangeRateDTO exchangeRateDTO) {
        return exchangeRateDTO != null && exchangeRateDTO.getRates() != null && !exchangeRateDTO.getRates().isEmpty();
    }

    BigDecimal fetchAndSaveExchangeRate(String currencyCode) {
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

