package com.panamby.readfile.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.panamby.readfile.models.dto.SubscribeRequest;
import com.panamby.readfile.services.BacklogManagerService;
import com.panamby.readfile.utils.UUIDUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BacklogManagerHealthIndicator implements HealthIndicator {
	
	@Autowired
	private BacklogManagerService backlogManagerService;
	
	@Override
	public Health health() {

		log.info("[BacklogManagerHealthIndicator] Starting heatlh check.");
		String transactionId = UUIDUtils.generateUUID();
		
		SubscribeRequest subscribeRequest = new SubscribeRequest("Carla", "TV");

		Builder statusBuilder = Health.up();
		
		try {
			
			backlogManagerService.sendSubscriber(subscribeRequest, transactionId);
			
		}catch(Exception ex) {
			
			statusBuilder = Health.down(ex);
			log.error(String.format("[BacklogManagerHealthIndicator] [%s]" , ex.getMessage()));
		}
		
		log.info(String.format("[BacklogManagerHealthIndicator] Finish heatlh check - SUBSCRIBE_REQUEST [%s]" , subscribeRequest));
		
		return statusBuilder.build();
	}
}
