package com.panamby.readfile.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissDataAccessException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String message;

	public MissDataAccessException(String message) {
		
		this.setMessage(message);
	}

}
