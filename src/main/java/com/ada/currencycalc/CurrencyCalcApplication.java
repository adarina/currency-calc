package com.ada.currencycalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class CurrencyCalcApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyCalcApplication.class, args);
    }

}
