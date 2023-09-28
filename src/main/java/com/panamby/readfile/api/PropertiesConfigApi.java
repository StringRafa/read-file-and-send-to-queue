package com.panamby.readfile.api;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.panamby.readfile.models.dto.DeletePropertiesConfigResponse;
import com.panamby.readfile.models.dto.PropertiesConfigDto;

@RequestMapping("/propertie-config")
public interface PropertiesConfigApi {

	@PostMapping("/v1")
	ResponseEntity<PropertiesConfigDto> create(@RequestBody(required = true) PropertiesConfigDto propertiesConfigDto);

	@GetMapping("/v1/{id}")
	ResponseEntity<PropertiesConfigDto> findByConfigName(@PathVariable(name = "id") String configName);

	@DeleteMapping("/v1/{id}")
	ResponseEntity<DeletePropertiesConfigResponse> delete(@PathVariable(name = "id") String configNameId);

	@PutMapping("/v1/{id}")
	ResponseEntity<PropertiesConfigDto> update(@PathVariable(name = "id") String configNameId,
			@Valid @RequestBody(required = true) PropertiesConfigDto propertiesConfigDto);

}
