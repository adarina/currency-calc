package com.ada.currencycalc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ada.currencycalc.model.ExchangeRate;

import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findByCurrencyCodeAndEffectiveDate(String currencyCode, LocalDate effectiveDate);
}
