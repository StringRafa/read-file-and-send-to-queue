package com.panamby.readfile.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.panamby.readfile.consts.ConstantUtils;
import com.panamby.readfile.consts.RabbitMQConstants;
import com.panamby.readfile.exceptions.ReadFileException;
import com.panamby.readfile.models.Employee;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReadFileService {
	
	@Autowired
	private RabbitMQService rabbitMQService;

	public String readFileAndSendQueue(MultipartFile multipartFile) {

		log.info("Started read file and send queue.");

		File file = convertMultiPartToFile(multipartFile);
		
		List<Employee> list = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))){
			
			String employeeTxt = br.readLine();
			while (employeeTxt != null) {
				
				String[] fields = employeeTxt.split(",");
				list.add(new Employee(fields[0], Double.parseDouble(fields[1])));
				employeeTxt = br.readLine();
			}
			
			br.close();
			file.delete();
		} catch (IOException e) {
			
			log.error(String.format("Error: %s", e.getMessage()));
		}

		log.info(ConstantUtils.READ_FILE_AND_SEND_QUEUE_FINISHED);
		
		return sendEmployeeForQueue(list);
	}

	private String sendEmployeeForQueue(List<Employee> list) {

		log.info("Started send employee for queue.");
		
		for (Employee emp : list) {
			
			log.info(String.format("sending employee information to queue. INFO [%s]", emp));
			
			rabbitMQService.sendMessageExchange(RabbitMQConstants.EXCHANGE_READ_FILE, RabbitMQConstants.ROUTING_KEY, new Gson().toJson(emp));
		}

		log.info("Send employee for queue finished.");
		
		return ConstantUtils.READ_FILE_AND_SEND_QUEUE_FINISHED;
	}

	public List<Employee> readFileAndSendQueueV2(MultipartFile multipartFile, Integer priority) {

		log.info("Started read file and send queue V2.");

		File file = convertMultiPartToFile(multipartFile);
		
		List<Employee> list = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))){
			
			String employeeTxt = br.readLine();
			while (employeeTxt != null) {
				
				String[] fields = employeeTxt.split(",");
				list.add(new Employee(fields[0], Double.parseDouble(fields[1])));
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

	public String sendEmployeeForQueueV2(MultipartFile multipartFile, Integer priority) {

		log.info("Started send employee for queue V2.");
		
		List<Employee> list = readFileAndSendQueueV2(multipartFile, priority);
		
		for (Employee emp : list) {
			
			log.info(String.format("sending employee information to queue. INFO [%s]", emp));
			
			MessageProperties properties = new MessageProperties();
			properties.setPriority(priority);

			Message message = MessageBuilder.withBody(emp.toString().getBytes())
					.andProperties(properties)
					.build();
			
			rabbitMQService.sendMessageExchange(RabbitMQConstants.EXCHANGE_READ_FILE, RabbitMQConstants.ROUTING_KEY, message);
		}

		log.info("Send employee for queue V2 finished.");
		
		return ConstantUtils.READ_FILE_AND_SEND_QUEUE_FINISHED;
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
}
