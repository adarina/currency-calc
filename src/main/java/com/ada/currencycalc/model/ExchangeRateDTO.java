package com.ada.currencycalc.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExchangeRateDTO {
    private String table;
    private String currency;
    private String code;
    private List<RateDTO> rates;
}
