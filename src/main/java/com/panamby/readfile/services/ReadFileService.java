package com.panamby.readfile.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.panamby.readfile.consts.ConstantUtils;
import com.panamby.readfile.consts.RabbitMQConstants;
import com.panamby.readfile.exceptions.BacklogManagerTimeOutException;
import com.panamby.readfile.exceptions.ReadFileException;
import com.panamby.readfile.models.RetryTimeConfig;
import com.panamby.readfile.models.dto.PropertiesConfigDto;
import com.panamby.readfile.models.dto.SubscribeRequest;
import com.panamby.readfile.models.dto.SubscribeResponse;
import com.panamby.readfile.utils.UUIDUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReadFileService {
	
	@Autowired
	private RabbitMQService rabbitMQService;
	
	@Autowired
	private BacklogManagerService backlogManagerService;

	@Autowired
	private RabbitListenerEndpointRegistry endpointRegistry;
	
	@Autowired
	private PropertiesConfigService propertiesConfigService;
	
	@Autowired
	private RetryConfigService retryConfigService;

	@Value("${backlog-manager.service-config.name}")
	private String configServiceName;

	public String readFileAndSendQueue(MultipartFile multipartFile) {

		log.info("Started read file and send queue.");

		File file = convertMultiPartToFile(multipartFile);
		
		List<SubscribeRequest> list = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))){
			
			String employeeTxt = br.readLine();
			while (employeeTxt != null) {
				
				String[] fields = employeeTxt.split(",");
				list.add(new SubscribeRequest(fields[0], fields[1]));
				employeeTxt = br.readLine();
			}
			
			br.close();
			file.delete();
		} catch (IOException e) {
			
			log.error(String.format("Error: %s", e.getMessage()));
		}

		log.info(ConstantUtils.READ_FILE_AND_SEND_QUEUE_FINISHED);
		
		return sendSubscribeRequestForQueue(list);
	}

	private String sendSubscribeRequestForQueue(List<SubscribeRequest> list) {

		log.info("Started send subscriber for queue.");
		
		for (SubscribeRequest emp : list) {
			
			log.info(String.format("sending subscriber information to queue. INFO [%s]", emp));
			
			rabbitMQService.sendMessageExchange(RabbitMQConstants.EXCHANGE_READ_FILE, RabbitMQConstants.ROUTING_KEY, new Gson().toJson(emp));
		}

		log.info("Send subscriber for queue finished.");
		
		return ConstantUtils.READ_FILE_AND_SEND_QUEUE_FINISHED;
	}

	public List<SubscribeRequest> readFileAndSendQueueV2(MultipartFile multipartFile, Integer priority) {

		log.info("Started read file and send queue V2.");

		File file = convertMultiPartToFile(multipartFile);
		
		List<SubscribeRequest> list = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))){
			
			String employeeTxt = br.readLine();
			while (employeeTxt != null) {
				
				String[] fields = employeeTxt.split(",");
				list.add(new SubscribeRequest(fields[0], fields[1]));
				employeeTxt = br.readLine();
			}
			
			br.close();
			file.delete();
		} catch (IOException e) {
			
			log.error(String.format("Error: %s", e.getMessage()));
		}

		log.info("Read file and send queue V2 finished.");
		
		return list;
	}

	public String sendSubscribeRequestForQueueV2(MultipartFile multipartFile, Integer priority) {

		String transactionId = UUIDUtils.generateUUID();

		log.info("Started send subscriber for queue V2.");
		
		List<SubscribeRequest> list = readFileAndSendQueueV2(multipartFile, priority);
		
		for (SubscribeRequest subscribeRequest : list) {
			
			log.info(String.format("sending subscriber information to queue. INFO [%s]", subscribeRequest));
			
			sendSubscriberForBacklogManager(transactionId, subscribeRequest);
			
			MessageProperties properties = new MessageProperties();
			properties.setPriority(priority);

			Message message = MessageBuilder.withBody(subscribeRequest.toString().getBytes())
					.andProperties(properties)
					.build();
			
			rabbitMQService.sendMessageExchange(RabbitMQConstants.EXCHANGE_READ_FILE, RabbitMQConstants.ROUTING_KEY, message);
		}

		log.info("Send subscriber for queue V2 finished.");
		
		return ConstantUtils.READ_FILE_AND_SEND_QUEUE_FINISHED;
	}

	private void sendSubscriberForBacklogManager(String transactionId, SubscribeRequest subscribeRequest) {

		log.info(String.format("Started send Subscriber For Backlog Manager. SUBSCRIBE_REQUEST [%s] - TRANSACTION_ID [%s]", subscribeRequest, transactionId));
		
		SubscribeResponse subscriberResponse = sendRequestForBacklogManager(subscribeRequest, transactionId);

		log.info(String.format("Send Subscriber For Backlog Manager finished. SUBSCRIBE_RESPONSE [%s] - TRANSACTION_ID [%s]", subscriberResponse, transactionId));		
	}

	private File convertMultiPartToFile(MultipartFile multipartFile) {

		log.info("Started convert multi part to file.");
		String originalFilename = null;
		
		try {

			originalFilename = multipartFile.getOriginalFilename();
			File file = new File(originalFilename);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(multipartFile.getBytes());
			fos.close();

			log.info("Convert multi part to file finished.");
			
			return file;
		} catch (FileNotFoundException e) {

			log.error(String.format("File Not Found. FILE_NAME [%s]", originalFilename), e);

			throw new ReadFileException("File not found", e);
		} catch (IOException e) {

			log.error(String.format("Unable to convert multiple parts to file. FILE_NAME [%s]", originalFilename), e);

			throw new ReadFileException("Unable to convert multiple parts to file.", e);
		}
	}
	
	@RabbitListener(id = "queueBacklogManagerRetry", queues = RabbitMQConstants.RETRY_QUEUE_FOR_BACKLOG_MANAGER, autoStartup = "false")
	private void consumer(SubscribeRequest subscribeRequest) {

		log.info(String.format("Started queue consumer. QUEUE [%s]", RabbitMQConstants.RETRY_QUEUE_FOR_BACKLOG_MANAGER));
		
		String transactionId = UUIDUtils.generateUUID();
		SimpleMessageListenerContainer listenerEndpoint = (SimpleMessageListenerContainer) endpointRegistry.getListenerContainer("queueBacklogManagerRetry");
		
		int indexCount = 0;
		Long retryTime = 0L;
		PropertiesConfigDto propertiesConfig = propertiesConfigService.getPropertiesConfig(configServiceName);
		RetryTimeConfig retryTimeConfig = retryConfigService.findByServiceName(configServiceName);
		
		if(retryTimeConfig != null) {
			
			indexCount = retryTimeConfig.getIndexCount();
		}
		
        Long timeDifference = null;
		
        if(propertiesConfig != null && propertiesConfig.getBacklogManagerUnavailabilityErrorDate() != null) {

            timeDifference = Duration.between(propertiesConfig.getBacklogManagerUnavailabilityErrorDate(), LocalDateTime.now()).getSeconds();
            retryTime = propertiesConfig.getRetryTime().get(indexCount);
        }

        if(timeDifference != null && timeDifference <= retryTime) {
			
			listenerEndpoint.stop();
			log.debug("Stopping Retry Backlog Manager listener container.");
			
			System.out.println("=============================");
			System.out.println("Retry Time: " + retryTime);
			System.out.println("Time Difference: " + timeDifference);
			System.out.println("=============================");
			
		}else {
		
			System.out.println("=============================");
			System.out.println(subscribeRequest);
			System.out.println("=============================");
			
			sendRequestForBacklogManager(subscribeRequest, transactionId);
		}

		log.info(String.format("Queue consumer finished. QUEUE [%s]", RabbitMQConstants.RETRY_QUEUE_FOR_BACKLOG_MANAGER));
	}

	private SubscribeResponse sendRequestForBacklogManager(SubscribeRequest subscribeRequest, String transactionId) {

		log.info(String.format("Started send Request For Backlog-Manager. SUBSCRIBE_REQUEST [%s] - TRANSACTION_ID", subscribeRequest, transactionId));
		
		SubscribeResponse subscriberResponse = null;
		RetryTimeConfig retryTimeConfig = retryConfigService.findByServiceName(configServiceName);
		
		try {
			
			subscriberResponse = backlogManagerService.sendSubscriber(subscribeRequest, transactionId);
			retryConfigService.resetIndex(retryTimeConfig, configServiceName); 
		} catch (BacklogManagerTimeOutException e) {
			
			int indexCount = 0;
			Long retryTime = 0L;
			PropertiesConfigDto propertiesConfig = propertiesConfigService.getPropertiesConfig(configServiceName);
			
			if(retryTimeConfig != null) {
				
				indexCount = retryTimeConfig.getIndexCount();
			}
			
	        Long timeDifference = null;
			
	        if(propertiesConfig != null && propertiesConfig.getBacklogManagerUnavailabilityErrorDate() != null) {

	            timeDifference = Duration.between(propertiesConfig.getBacklogManagerUnavailabilityErrorDate(), LocalDateTime.now()).getSeconds();
	            retryTime = propertiesConfig.getRetryTime().get(indexCount);
	        }
	        
	        if(timeDifference != null && timeDifference >= retryTime) {
	        	
				propertiesConfig.setBacklogManagerUnavailabilityErrorDate(LocalDateTime.now());

				propertiesConfigService.update(propertiesConfig, configServiceName);
				retryConfigService.update(retryTimeConfig, configServiceName);
	        }else if(propertiesConfig.getBacklogManagerUnavailabilityErrorDate() == null) {
	        	
				propertiesConfig.setBacklogManagerUnavailabilityErrorDate(LocalDateTime.now());
				propertiesConfigService.update(propertiesConfig, configServiceName);
	        }
			
	        rabbitMQService.sendMessage(RabbitMQConstants.QUEUE_AUDIT, e);
		}

		log.info(String.format("Send Request For Backlog-Manager finished. SUBSCRIBE_REQUEST [%s] - TRANSACTION_ID", subscribeRequest, transactionId));
		
		return subscriberResponse;
	}

	@Scheduled(fixedRateString = "${backlog-manager.retry-request.fixedRate.in.milliseconds}")
	private void startRetryBacklogManagerJob() {
		
		log.info("Trying to start Retry Backlog Manager listener container.");
		
		SimpleMessageListenerContainer listenerEndpoint = (SimpleMessageListenerContainer) endpointRegistry.getListenerContainer("queueBacklogManagerRetry");
		
		if (listenerEndpoint.isRunning()) { return; }
		listenerEndpoint.start();
		
		log.info("Starting Retry Backlog Manager listener container.");
	}
}
