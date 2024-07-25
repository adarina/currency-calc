package com.ada.currencycalc;

import com.ada.currencycalc.model.ExchangeRate;
import com.ada.currencycalc.repository.ExchangeRateRepository;
import com.ada.currencycalc.service.CleanupService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class ExampleIntegrationTest extends IntegrationTestConfig {

    @Autowired
    ExchangeRateRepository exchangeRateRepository;

    @Autowired
    CleanupService cleanupService;

    @BeforeEach
    void setUp() {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setRate(BigDecimal.TEN);
        exchangeRate.setCurrencyCode("PL");
        exchangeRate.setEffectiveDate(LocalDate.parse("2024-07-17"));
        exchangeRateRepository.save(exchangeRate);
    }

    @AfterEach
    void tearDown() {
        exchangeRateRepository.deleteAll();
    }

//    @Test
//    void shouldCleanExchangeRateTable() {
//        cleanupService.clearPreviousDayData();
//        assertTrue(exchangeRateRepository.findAll().isEmpty());
//    }

    @Test
    void shouldReturnConvertedCurrency() throws Exception {
        String fromCurrency = "USD";
        BigDecimal amount = BigDecimal.valueOf(100);
        String toCurrency = "EUR";

        mockMvc.perform(get("/api/v1/currency-conversion")
                        .param("from", fromCurrency)
                        .param("amount", amount.toString())
                        .param("to", toCurrency)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnInternalServerErrorForMissingParams() throws Exception {
        mockMvc.perform(get("/api/v1/currency-conversion")
                        .param("from", "USD")
                        .param("amount", "100")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnInternalServerErrorForInvalidAmount() throws Exception {
        mockMvc.perform(get("/api/v1/currency-conversion")
                        .param("from", "USD")
                        .param("amount", "sth")
                        .param("to", "PLN")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}
