package com.panamby.readfile.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BacklogManagerTimeOutException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String message;
    private String id;
    private String transactionId;
    private String product;

    public BacklogManagerTimeOutException(String message) {
        super(message);
    }

    public BacklogManagerTimeOutException(String message, String transactionId, String id, String product) {
        super(message);
        this.message = message;
        this.transactionId = transactionId;
        this.id = id;
        this.product = product;
    }
}
