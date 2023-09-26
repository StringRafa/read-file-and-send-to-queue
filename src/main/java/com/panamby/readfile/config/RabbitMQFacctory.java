package com.panamby.readfile.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.panamby.readfile.consts.RabbitMQConstants;

@Configuration
public class RabbitMQFacctory {

	@Autowired
	private ConnectionFactory rabbitConnectionFactory;
	
	// this will create a new queue if it doesn't exist; otherwise, it'll use the existing one of the same name

	// ...the second argument means to make the queue 'durable'
	
	@Bean
	public Declarables fanoutBindings() {
		
	    Queue queueReadFile = new Queue(RabbitMQConstants.QUEUE_READ_FILE, true);
	    Queue queueReadFileCopy = new Queue(RabbitMQConstants.QUEUE_READ_FILE_COPY, true);
	    FanoutExchange readFileExchange = new FanoutExchange(RabbitMQConstants.EXCHANGE_READ_FILE);

	    return new Declarables(
	      queueReadFile,
	      queueReadFileCopy,
	      readFileExchange,
	      BindingBuilder.bind(queueReadFile).to(readFileExchange),
	      BindingBuilder.bind(queueReadFileCopy).to(readFileExchange));
	}
	
	@Bean
	public Queue auditQueue() {

		return new Queue(RabbitMQConstants.QUEUE_AUDIT, true);

	}
	
// this is necessary for operations with Spring AMQP

	@Bean
	public RabbitTemplate getMyQueueTemplateConfig() {
		return new RabbitTemplate(rabbitConnectionFactory);
	}
}
