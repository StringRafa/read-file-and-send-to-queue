package com.panamby.readfile.config;

import java.util.Collections;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
	
	//Backlog Manager
	@Value("${backlog-manager.subscibe.host.url}")
	private String backlogManagerhost;
	
	@Bean
	public WebClient webClientBacklogManager(WebClient.Builder builder) {
		return builder
				.baseUrl(backlogManagerhost)
				.defaultHeaders(getBacklogManagerHttpHeaders())
				.build();
	}
	
	public Consumer<HttpHeaders> getBacklogManagerHttpHeaders() {
	    return headers -> {
	        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); 
	        headers.setContentType(MediaType.APPLICATION_JSON);
	    };
	}
}
