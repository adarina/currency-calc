package com.ada.currencycalc.service;

import com.ada.currencycalc.repository.ExchangeRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class CleanupService {

    private final ExchangeRateRepository exchangeRateRepository;

    private final Clock clock;

    public CleanupService(ExchangeRateRepository exchangeRateRepository, Clock clock) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void clearPreviousDayData() {
        LocalDate yesterday = LocalDate.now(clock).minusDays(1);
        log.info("Starting cleanup for date: {}", yesterday.format(DateTimeFormatter.ISO_DATE));

        exchangeRateRepository.deleteByEffectiveDate(yesterday);

        log.info("Completed cleanup for date: {}", yesterday.format(DateTimeFormatter.ISO_DATE));

    }
}
