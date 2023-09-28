package com.panamby.readfile.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertiesConfigNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String message;
    private String transactionType;

    public PropertiesConfigNotFoundException(String message) {
	this.message = message;
    }

    public PropertiesConfigNotFoundException(String message, String transactionType) {
	super(message);
	this.message = message;
	this.transactionType = transactionType;
    }
}