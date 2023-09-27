package com.panamby.readfile.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.panamby.readfile.services.ReadFileService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/read-file")
@RestController
public class ReadFileController {

	@Autowired
	private ReadFileService service;
	
	@PostMapping(path = "/v1")
	public ResponseEntity<String> readFile(@RequestParam(name = "file") MultipartFile multipartFile) throws IOException {

		log.info("Started read file controller.");
		
		String response = service.readFileAndSendQueue(multipartFile);

		log.info("Read file controller finished.");
		
		return ResponseEntity.ok(response);
	}
	
	@PostMapping(path = "/v2")
	public ResponseEntity<String> readFileV2(@RequestParam(name = "file") MultipartFile multipartFile, @RequestParam Integer priority) throws IOException {

		log.info("Started read file controller V2.");
		
		String response = service.sendSubscribeRequestForQueueV2(multipartFile, priority);

		log.info("Read file controller V2 finished.");
		
		return ResponseEntity.ok(response);
	}
}
