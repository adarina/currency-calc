package com.ada.currencycalc.service;

import com.ada.currencycalc.model.ExchangeRateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.ada.currencycalc.repository.ExchangeRateRepository;
import com.ada.currencycalc.model.ExchangeRate;

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

    public CurrencyService(ExchangeRateRepository exchangeRateRepository, Clock clock) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.restTemplate = new RestTemplate();
        this.clock = clock;
    }

    public double convertCurrency(String from, double amount, String to) {
        if (isBlank(from) || isBlank(to)) {
            throw new IllegalArgumentException("Currency code cannot be blank, empty or null");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        double fromRate = getExchangeRate(from);
        double toRate = getExchangeRate(to);
        return amount * (fromRate / toRate);
    }

    private double getExchangeRate(String currencyCode) {
        LocalDate today = LocalDate.now(clock);
        Optional<ExchangeRate> optionalExchangeRate = exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(currencyCode, today);
        if (optionalExchangeRate.isPresent()) {
            return optionalExchangeRate.get().getRate();
        } else {
            double newRate = fetchAndSaveExchangeRate(currencyCode);
            log.info("Added new exchange rate to database: currencyCode={}, date={}, rate={}", currencyCode, today, newRate);
            return newRate;
        }
    }

    double fetchAndSaveExchangeRate(String currencyCode) {
        String url = String.format("https://api.nbp.pl/api/exchangerates/rates/A/%s/", currencyCode);
        ExchangeRateDTO response = restTemplate.getForObject(url, ExchangeRateDTO.class);
        if (response != null && response.getRates() != null && !response.getRates().isEmpty()) {
            double rate = response.getRates().get(0).getMid();
            ExchangeRate exchangeRate = new ExchangeRate(currencyCode, rate, LocalDate.now(clock));
            exchangeRateRepository.save(exchangeRate);
            return rate;
        } else {
            throw new RuntimeException("Unable to fetch exchange rate for currency: " + currencyCode);
        }
    }
}

