package com.ada.currencycalc.exceptions;

import lombok.Getter;

@Getter
public class ExternalApiException extends RuntimeException{
    public ExternalApiException(String message) {
        super(message);
    }
}
