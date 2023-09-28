package com.panamby.readfile.models;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetryTimeConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	private String serviceName;
	private int indexCount;
}
