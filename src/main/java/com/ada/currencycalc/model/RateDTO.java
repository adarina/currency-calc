package com.ada.currencycalc.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RateDTO {
    private String no;
    private String effectiveDate;
    private BigDecimal mid;
}
