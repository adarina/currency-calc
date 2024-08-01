package com.ada.currencycalc;

import com.ada.currencycalc.model.ExchangeRate;
import com.ada.currencycalc.repository.ExchangeRateRepository;
import com.ada.currencycalc.service.CleanupService;
import com.ada.currencycalc.service.NbpApiConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class ExampleIntegrationTest extends IntegrationTestConfig {

    @Autowired
    ExchangeRateRepository exchangeRateRepository;

    @Autowired
    CleanupService cleanupService;

    @MockBean
    NbpApiConnector nbpApiConnector;

    @BeforeEach
    void setUp() {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setRate(BigDecimal.TEN);
        exchangeRate.setCurrencyCode("PLN");
        exchangeRate.setEffectiveDate(LocalDate.parse("2024-07-31"));
        exchangeRateRepository.save(exchangeRate);
    }

    @AfterEach
    void tearDown() {
        exchangeRateRepository.deleteAll();
    }

    @Test
    void shouldCleanExchangeRateTable() {
        cleanupService.clearPreviousDayData();
        assertTrue(exchangeRateRepository.findAll().isEmpty());
    }

    @Test
    void shouldReturnConvertedCurrencyWithValidParams() throws Exception {
        final String from = "EUR";
        final String to = "CHF";
        final BigDecimal amount = BigDecimal.TEN;

        when(nbpApiConnector.fetchAndSaveExchangeRate(from)).thenReturn(BigDecimal.valueOf(1.5));
        when(nbpApiConnector.fetchAndSaveExchangeRate(to)).thenReturn(BigDecimal.valueOf(2.5));

        mockMvc.perform(get("/api/v1/currency-conversion")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .queryParam("amount", amount.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from", equalTo(from)))
                .andExpect(jsonPath("$.to", equalTo(to)))
                .andExpect(jsonPath("$.amount", equalTo(amount.intValue())))
                .andExpect(jsonPath("$.result", equalTo(6.00)));
    }

    @Test
    void shouldReturnBadRequestWhenCurrenciesAreNull() throws Exception {
        final BigDecimal amount = BigDecimal.TEN;

        mockMvc.perform(get("/api/v1/currency-conversion")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("amount", amount.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Required request parameter 'from' is not present")));
    }

    @Test
    void shouldReturnBadRequestForInvalidAmount() throws Exception {
        String from = "EUR";
        String invalidAmount = "invalidAmount";
        String to = "CHF";

        mockMvc.perform(get("/api/v1/currency-conversion")
                        .param("from", from)
                        .param("amount", invalidAmount)
                        .param("to", to)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid parameter: amount")));
    }
}
