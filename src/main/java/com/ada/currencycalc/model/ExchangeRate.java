package com.ada.currencycalc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currencyCode;
    private BigDecimal rate;
    private LocalDate effectiveDate;

    public ExchangeRate(String currencyCode, BigDecimal rate, LocalDate effectiveDate) {
        this.currencyCode = currencyCode;
        this.rate = rate;
        this.effectiveDate = effectiveDate;
    }
}
