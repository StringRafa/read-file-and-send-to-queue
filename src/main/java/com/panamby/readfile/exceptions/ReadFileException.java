package com.panamby.readfile.exceptions;

public class ReadFileException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	
	public ReadFileException(String msg) {
		super(msg);
	}
	
	public ReadFileException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
