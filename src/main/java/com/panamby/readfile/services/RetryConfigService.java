package com.panamby.readfile.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import com.panamby.readfile.consts.RedisConstants;
import com.panamby.readfile.exceptions.PropertiesConfigNotFoundException;
import com.panamby.readfile.models.RetryTimeConfig;
import com.panamby.readfile.models.dto.PropertiesConfigDto;

import lombok.extern.slf4j.Slf4j;

@EnableCaching
@Slf4j
@Service
public class RetryConfigService {

	@Autowired
	private PropertiesConfigService propertiesConfigService;

	@Value("${backlog-manager.service-config.name}")
	private String configServiceName;
	
    //Persist in cache with key "serviceName"
    @Cacheable(value = RedisConstants.BACKLOG_MANAGER_RETRY_TIME_CONFIG_CACHE, key = "#serviceName")
	public RetryTimeConfig findByServiceName(String serviceName) {
    	
		log.debug(String.format("Starting find retry time config by serviceName [%s]. ", serviceName));
		
		RetryTimeConfig retryTimeConfig = new RetryTimeConfig(serviceName, 0);		

		log.trace(String.format("Finished find retry time config [%s].", retryTimeConfig));
		   
		return retryTimeConfig;
	}

    @CachePut(value = RedisConstants.BACKLOG_MANAGER_RETRY_TIME_CONFIG_CACHE, key = "#retryTimeConfig.serviceName")
	public RetryTimeConfig update(RetryTimeConfig retryTimeConfig, String serviceName) {

		log.debug(String.format("Starting retry time config update. SERVICE_NAME [%s]", serviceName));
		
		retryTimeConfig.setIndexCount(retryTimeConfig.getIndexCount() + 1);
		
		List<Long> retryTimeList = getRetryTimeByServiceName(serviceName);
		
		if(retryTimeConfig.getIndexCount() >= retryTimeList.size()) {
			
			retryTimeConfig.setIndexCount(0);
		}

		log.trace(String.format("Retry time config update finished. SERVICE_NAME [%s] - RETRY_TIME_CONFIG [%s]", serviceName, retryTimeConfig));
		
		return retryTimeConfig;
	}

    @CachePut(value = RedisConstants.BACKLOG_MANAGER_RETRY_TIME_CONFIG_CACHE, key = "#retryTimeConfig.serviceName")
	public RetryTimeConfig reset(RetryTimeConfig retryTimeConfig, String serviceName) {

		log.debug(String.format("Starting retry time config reset. SERVICE_NAME [%s]", serviceName));

		retryTimeConfig.setIndexCount(0);

		log.trace(String.format("Retry time config reset finished. SERVICE_NAME [%s] - RETRY_TIME_CONFIG [%s]",
				serviceName, retryTimeConfig));
		
		return retryTimeConfig;
	}
    
    @CachePut(value = RedisConstants.BACKLOG_MANAGER_RETRY_TIME_CONFIG_CACHE, key = "#retryTimeConfig.serviceName")
	public RetryTimeConfig verifyIndex(RetryTimeConfig retryTimeConfig, String serviceName) {

		log.debug(String.format("Starting retry time verify index SERVICE_NAME [%s]", serviceName));
		
		List<Long> retryTimeList = getRetryTimeByServiceName(serviceName);
		
		if(retryTimeConfig.getIndexCount() >= retryTimeList.size()) {
			
			retryTimeConfig.setIndexCount(0);
		}

		log.trace(String.format("Retry time verify index finished. SERVICE_NAME [%s] - RETRY_TIME_CONFIG [%s]", serviceName, retryTimeConfig));
		
		return retryTimeConfig;
	}

	private List<Long> getRetryTimeByServiceName(String serviceName) {

		log.debug(String.format("Starting get Retry Time By Service Name [%s].", serviceName));
		
		PropertiesConfigDto propertiesConfig = propertiesConfigService.findByConfigName(configServiceName);
		
		if(propertiesConfig.getRetryTime() == null) {
			
			log.error(String.format("Retry Time By Service Name config not found. PROPERTIES_CONFIG [%s]", propertiesConfig));
			
			throw new PropertiesConfigNotFoundException("Retry Time By Service Name config not found.");
		}
		
		List<Long> retryTimeByServiceNameConfigList = propertiesConfig
				.getRetryTime()
				.stream()
				.collect(Collectors.toList());
		
		if(retryTimeByServiceNameConfigList.isEmpty()) {
			
			log.error(String.format("Retry Time By Service Name not found. PROPERTIES_CONFIG [%s]", propertiesConfig));
			
			throw new PropertiesConfigNotFoundException(String.format("Retry Time By Service Name config not found. SERVICE_NAME [%s]", serviceName));
		}

		log.trace(String.format("Get Retry Time By Service Name finished. SERVICE_NAME [%s] - RETRY_TIME_BY_SERVICE_NAME_CONFIG [%s]",
				serviceName, retryTimeByServiceNameConfigList));		
		
		return retryTimeByServiceNameConfigList;
	}
	
}
