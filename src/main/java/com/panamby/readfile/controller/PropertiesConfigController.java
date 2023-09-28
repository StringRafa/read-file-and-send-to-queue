package com.panamby.readfile.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.panamby.readfile.api.PropertiesConfigApi;
import com.panamby.readfile.models.dto.DeletePropertiesConfigResponse;
import com.panamby.readfile.models.dto.PropertiesConfigDto;
import com.panamby.readfile.services.PropertiesConfigService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class PropertiesConfigController implements PropertiesConfigApi {
	
	@Autowired
	private PropertiesConfigService propertiesConfigService;
	
	@Override
	public ResponseEntity<PropertiesConfigDto> create(PropertiesConfigDto propertiesConfigDto) {

		log.info(String.format("Starting properties config. PROPERTY_CONFIG [%s]", propertiesConfigDto));

		PropertiesConfigDto newPropertiesConfigDto = propertiesConfigService.create(propertiesConfigDto);

		log.info(String.format("Finished properties config. PROPERTY_CONFIG [%s]", newPropertiesConfigDto));

		return new ResponseEntity<PropertiesConfigDto>(newPropertiesConfigDto, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<PropertiesConfigDto> findByConfigName(String configName) {

		log.info(String.format("Starting find by config name [%s]. ", configName));

		PropertiesConfigDto propertiesConfigDto = propertiesConfigService.findByConfigName(configName);

		log.info(String.format("Finished find properties config [%s].", propertiesConfigDto));

		return ResponseEntity.ok(propertiesConfigDto);
	}

	@Override
	public ResponseEntity<DeletePropertiesConfigResponse> delete(String configNameId) {

		log.info(String.format("Starting deleting properties config - CONFIG_NAME_ID [%s]", configNameId));

		DeletePropertiesConfigResponse response = propertiesConfigService.delete(configNameId);

		log.info(String.format("Finishing deleting properties config - CONFIG_NAME_ID [%s]", configNameId));

		return new ResponseEntity<DeletePropertiesConfigResponse>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<PropertiesConfigDto> update(String configNameId, @Valid PropertiesConfigDto propertiesConfigDto) {

		log.info(String.format("Starting properties config update CONFIG_NAME_ID [%s]", configNameId));
		  
		PropertiesConfigDto newPropertiesConfigDto = propertiesConfigService.update(propertiesConfigDto, configNameId);

		log.info(String.format("Finish properties config update CONFIG_NAME_ID [%s]", configNameId));

		return new ResponseEntity<PropertiesConfigDto>(newPropertiesConfigDto, HttpStatus.OK);
	}

}
