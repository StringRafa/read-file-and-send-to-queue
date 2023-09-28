package com.panamby.readfile.services;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.panamby.readfile.consts.RabbitMQConstants;
import com.panamby.readfile.models.dto.PropertiesConfigDto;
import com.panamby.readfile.models.dto.SubscribeRequest;
import com.panamby.readfile.models.dto.SubscribeResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@Slf4j
public class BacklogManagerService{

	@Autowired
	private WebClient webClientBacklogManager;
	
	@Autowired
	private RabbitMQService rabbitMQService;
	
	@Autowired
	private PropertiesConfigService propertiesConfigService;
	
	@Autowired
	private PriorityConfigService priorityConfigService;

	@Value("${backlog-manager.properties-config.name}")
	private String propertiesConfigName;

	@Value("${backlog-manager.subscibe.host.url.endpoint}")
	private String endpointBacklogManager;

	@Value("${backlog-manager.subscibe.timeout}")
	private int backlogManagerTimeout;
	
	@Value("${backlog-manager.subscibe.retry.maxattempts}")
	private int backlogManagerRetryMaxAttempts;

	@Value("${backlog-manager.subscibe.retry.fixeddelay}")
	private int backlogManagerRetryFixedDelay;

	public SubscribeResponse sendSubscriber(SubscribeRequest subscribeRequest, String transactionId) {
		
		log.trace(String.format("Sending request to Backlog Manager. ENDPOINT [%s] - SUBSCRIBE_REQUEST [%s] - TRANSACTION_ID [%s]", endpointBacklogManager, subscribeRequest, transactionId));
		
		Mono<SubscribeResponse> response = webClientBacklogManager
				.method(HttpMethod.POST)
				.uri(uriBuilder -> uriBuilder
						.path(endpointBacklogManager)
						.build())
				.bodyValue(subscribeRequest)
				.retrieve()
				.bodyToMono(SubscribeResponse.class)
				.retryWhen(Retry.fixedDelay(backlogManagerRetryMaxAttempts, Duration.ofMillis(backlogManagerRetryFixedDelay))
						 .filter(throwable -> throwable instanceof IllegalStateException));
		
		SubscribeResponse subscribeResponse = null; 
		
		try{

			subscribeResponse = response.block(Duration.ofMillis(backlogManagerTimeout));
		}catch(IllegalStateException | WebClientRequestException ex){

			log.error(String.format("Backlog Manager response time limit [%s]. SUBSCRIBE_REQUEST [%s]", ex.getMessage(), subscribeRequest));
			
			rabbitMQService.sendMessageNoJson(RabbitMQConstants.RETRY_QUEUE_FOR_BACKLOG_MANAGER, subscribeRequest);
			
			PropertiesConfigDto propertiesConfig = propertiesConfigService.getPropertiesConfig(propertiesConfigName);
			
			propertiesConfig.setBacklogManagerUnavailabilityErrorDate(LocalDateTime.now());
			
			propertiesConfigService.update(propertiesConfig, propertiesConfigName);
		}
		
		log.trace(String.format("Response of Backlog Manager. ENDPOINT [%s]  SUBSCRIBE_RESPONSE [%s]", endpointBacklogManager, subscribeResponse));
		
		return subscribeResponse;
	}
}
