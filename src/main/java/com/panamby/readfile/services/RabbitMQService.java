package com.panamby.readfile.services;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.panamby.readfile.exceptions.ReadFileException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RabbitMQService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

	@Autowired
    private AmqpAdmin amqpAdmin;
    
    public void sendMessage(String nameQueue, Object obj) {
       
    	log.info(String.format("Sending message to QUEUE [%s] - MESSAGE [%s]", nameQueue, obj));
    	
    	try {
    		
			rabbitTemplate.convertAndSend(nameQueue, new Gson().toJson(obj));
		} catch (AmqpException e) {
			
			log.error(String.format("Unable to send to queue %s.", nameQueue), e);
		}
    }
    
    public void sendMessageNoJson(String nameQueue, Object obj) {
       
    	log.info(String.format("Sending message to QUEUE [%s] - MESSAGE [%s]", nameQueue, obj));
    	
    	try {
    		
			rabbitTemplate.convertAndSend(nameQueue, obj);
		} catch (AmqpException e) {
			
			log.error(String.format("Unable to send to queue %s.", nameQueue), e);
		}
    }

	public void sendMessageExchange(String exchange, String routingKey, Object obj) {

		log.info(String.format("Sending message... MESSAGE [%s] - EXCHANGE [%s] - ROUTING_KEY [%s]", obj, exchange, routingKey));		

    	try {
    		
			rabbitTemplate.convertAndSend(exchange, routingKey, obj);
		} catch (AmqpException e) {
			
			log.error(String.format("Unable to send to exchange %s.", exchange), e);
			
			throw new ReadFileException("Unable to send to exchange.", e);
		}
	}
	
    public int listQueueMessageSize(String nameQueue) {
        
    	log.info(String.format("Starting listQueueMessageSize. QUEUE [%s]", nameQueue));

		int messageCount = 0;
		QueueInformation queueInfo = amqpAdmin.getQueueInfo(nameQueue);

		if (queueInfo != null) {
			messageCount = queueInfo.getMessageCount();
		}

		return messageCount;
    }
}