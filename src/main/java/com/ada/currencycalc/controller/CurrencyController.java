package com.ada.currencycalc.controller;

import com.ada.currencycalc.service.CurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/currency-conversion")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    public BigDecimal convertCurrency(@RequestParam String from, @RequestParam BigDecimal amount, @RequestParam String to) {
        return currencyService.convertCurrency(from, amount, to);
    }
}

