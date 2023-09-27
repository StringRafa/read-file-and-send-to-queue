package com.panamby.readfile.exceptions.handler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import com.panamby.readfile.consts.ConstantUtils;
import com.panamby.readfile.consts.RabbitMQConstants;
import com.panamby.readfile.exceptions.ReadFileException;
import com.panamby.readfile.exceptions.dto.ReadFileGeneralDataErrorResponse;
import com.panamby.readfile.exceptions.dto.ReadFileGeneralErrorResponse;
import com.panamby.readfile.services.RabbitMQService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GeneralExceptionsHandler extends ExceptionHandlerExceptionResolver {
	
	@Autowired
	private RabbitMQService rabbitMQService;

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest webRequest) {

		log.error(String.format("EXCEPTION [%s] - MESSAGE [%s]", ex.getClass(), ex.getMessage()));

		ReadFileGeneralErrorResponse body = new ReadFileGeneralErrorResponse(new ReadFileGeneralDataErrorResponse(
				"Unexpected error occurs", "Internal server error.", ConstantUtils.FAIL));

		rabbitMQService.sendMessage(RabbitMQConstants.QUEUE_AUDIT, body);

		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
			HttpServletRequest request) {

		ReadFileGeneralErrorResponse body = new ReadFileGeneralErrorResponse(
				new ReadFileGeneralDataErrorResponse("Max Upload Size Exceeded", ex.getMessage(), ConstantUtils.FAIL));
		
		rabbitMQService.sendMessage(RabbitMQConstants.QUEUE_AUDIT, body);
		
		return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
	}

	@ExceptionHandler(ReadFileException.class)
	public ResponseEntity<Object> handleReadFileExceptionException(ReadFileException ex,
			HttpServletRequest request) {

		ReadFileGeneralErrorResponse body = new ReadFileGeneralErrorResponse(
				new ReadFileGeneralDataErrorResponse("Unprocessable Entity", ex.getMessage(), ConstantUtils.ERROR));
		
		rabbitMQService.sendMessage(RabbitMQConstants.QUEUE_AUDIT, body);
		
		return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
	}

}
