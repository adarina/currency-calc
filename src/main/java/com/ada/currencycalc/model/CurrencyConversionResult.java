package com.ada.currencycalc.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CurrencyConversionResult {
    private String from;
    private String to;
    private BigDecimal amount;
    private BigDecimal result;
}
