package com.panamby.readfile.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.panamby.readfile.consts.RedisConstants;
import com.panamby.readfile.exceptions.MissDataAccessException;
import com.panamby.readfile.exceptions.PropertiesConfigInsertException;
import com.panamby.readfile.exceptions.PropertiesConfigNotFoundException;
import com.panamby.readfile.exceptions.PropertiesConfigUpdateException;
import com.panamby.readfile.models.PropertiesConfig;
import com.panamby.readfile.models.dto.DeletePropertiesConfigResponse;
import com.panamby.readfile.models.dto.PropertiesConfigDto;
import com.panamby.readfile.repository.PropertiesConfigRepository;

import lombok.extern.slf4j.Slf4j;

@EnableCaching
@Slf4j
@Service
public class PropertiesConfigService  {
	
	@Autowired
	private PropertiesConfigRepository propertiesConfigRepository;

	@Value("${backlog-manager.service-config.name}")
	private String configServiceName;
	
	@CachePut(value = RedisConstants.READ_FILE_PROPERTIES_CONFIG_CACHE, key = "#propertiesConfigDto.configName")
	public PropertiesConfigDto create(PropertiesConfigDto propertiesConfigDto) {

		log.trace(String.format("Starting service properties config. PROPERTIES_CONFIG [%s]",
				propertiesConfigDto));

		PropertiesConfig propertiesConfig = PropertiesConfigDto.toPropertiesConfig(propertiesConfigDto);
		
		// Save new properties config on read_file_properties_config collection
		Optional<PropertiesConfig> propertiesConfigCreated = Optional.empty();
		
		try {

			propertiesConfigCreated = Optional.of(propertiesConfigRepository.insert(propertiesConfig));
		} catch (DuplicateKeyException ex) {

			log.error(ex.getMessage());
			log.trace(ex.toString());
			throw new PropertiesConfigInsertException(String
					.format("Couldn't register properties config with ID - [%s] - Properties config already registered", propertiesConfig.getConfigName()));
		} catch (DataAccessResourceFailureException e) {

			log.error(e.getMessage());
			log.trace(e.toString());
			throw new MissDataAccessException(String
					.format("Couldn't register properties config with ID - [%s] - Couldn't Access Database", propertiesConfig.getConfigName()));
		}

		log.trace(String.format("Service properties config finished. PROPERTIES_CONFIG [%s]",
				propertiesConfigCreated));

		PropertiesConfig checked = propertiesConfigCreated.orElseThrow(
				// if not present
				() -> new RuntimeException(String.format("Couldn't register properties config with ID - [%s]", propertiesConfig.getConfigName())));
		
		return PropertiesConfig.toPropertiesConfigDto(checked);
	}
	
    //Persist in cache with key "configName"
    @Cacheable(value = RedisConstants.READ_FILE_PROPERTIES_CONFIG_CACHE, key = "#configName")
	public PropertiesConfigDto findByConfigName(String configName) {

		log.debug(String.format("Starting find by config name [%s]. ", configName));

		PropertiesConfig propertiesConfig = null;

		try {

			propertiesConfig = propertiesConfigRepository.findByConfigName(configName);
			
			log.trace(String.format("Properties config found [%s]." , propertiesConfig));
		} catch (DataAccessResourceFailureException e) {

			log.error(e.getMessage());
			log.trace(e.toString());
			throw new MissDataAccessException("Properties config couldn't be found");
		}

		if(propertiesConfig == null) {
			
			throw new PropertiesConfigNotFoundException(String.format("Properties Config not found. CONFIG_NAME_ID [%s]", configName));
		}
		
		PropertiesConfigDto propertiesConfigDto = PropertiesConfig.toPropertiesConfigDto(propertiesConfig);

		log.trace(String.format("Finished find properties config [%s].", propertiesConfigDto));
		   
		return propertiesConfigDto;
	}

    @CachePut(value = RedisConstants.READ_FILE_PROPERTIES_CONFIG_CACHE, key = "#configNameId")
	public DeletePropertiesConfigResponse delete(String configNameId) {

		log.trace(String.format("Starting deleting properties config CONFIG_NAME_ID [%s]", configNameId));

		boolean configIsEmpty = false;

		try {

			log.trace(String.format("Finding properties config CONFIG_NAME_ID [%s]", configNameId));
			configIsEmpty = propertiesConfigRepository.findById(configNameId).isEmpty();
		} catch (DataAccessResourceFailureException e) {

			log.error(e.getMessage());
			log.trace(e.toString());
			throw new MissDataAccessException(String
					.format("Couldn't delete properties config with ID - [%s] - Couldn't Access Database", configNameId));
		}

		if (configIsEmpty) {

			log.debug(String.format("Properties config not found CONFIG_NAME_ID [%s]", configNameId));
			throw new PropertiesConfigNotFoundException(String.format("Properties Config not found. CONFIG_NAME_ID [%s]", configNameId));
		}

		try {

			propertiesConfigRepository.deleteById(configNameId);
		} catch (DataAccessResourceFailureException e) {

			log.error(e.getMessage());
			log.trace(e.toString());
			throw new MissDataAccessException("Couldn't Access Database");
		}

		log.trace(String.format("Ending deleting properties config CONFIG_NAME_ID [%s]", configNameId));

		return new DeletePropertiesConfigResponse(String.format("Properties config [%s] successfully deleted", configNameId));
	}

    @CachePut(value = RedisConstants.READ_FILE_PROPERTIES_CONFIG_CACHE, key = "#propertiesConfigDto.configName")
	public PropertiesConfigDto update(PropertiesConfigDto propertiesConfigDto, String configNameId) {

		log.debug(String.format("Starting properties config update CONFIG_NAME_ID [%s]", configNameId));

		if (!configNameId.equals(propertiesConfigDto.getConfigName())) {
			throw new PropertiesConfigUpdateException("Body's id is different from URI's id");
		}

		try {

			PropertiesConfig propertiesConfig = PropertiesConfigDto.toPropertiesConfig(propertiesConfigDto);
		   
			PropertiesConfigDto newPropertiesConfig = PropertiesConfig.toPropertiesConfigDto(propertiesConfigRepository.save(propertiesConfig));

			log.trace(String.format("Finish properties config update CONFIG_NAME_ID [%s]", configNameId));
			
			return  newPropertiesConfig;
		} catch (DataAccessResourceFailureException e) {

			log.error(e.getMessage());
			log.trace(e.toString());
			throw new MissDataAccessException(String
					.format("Couldn't update properties config with ID - [%s] - Couldn't Access Database", configNameId));
		}
	}

    public PropertiesConfigDto getPropertiesConfig(String configNameId) {
		
		log.debug(String.format("Starting Get Properties Config [%s]", configNameId));
		
		PropertiesConfigDto propertiesConfigName = null;
		
		try {
			
			propertiesConfigName = findByConfigName(configNameId);
		} catch (MissDataAccessException e) {
			
			log.error(String.format("Properties Config Error [%s]", e.getMessage()));
		}
		
		if(propertiesConfigName == null) {
			
			log.error(String.format("Properties Config not found. CONFIG_NAME_ID [%s]", configNameId));
			
			throw new PropertiesConfigNotFoundException(String.format("Properties Config not found. CONFIG_NAME_ID [%s]", configNameId));
		}
		
		log.trace(String.format("Get Properties Config finished. PROPERTIES_CONFIG [%s]", propertiesConfigName));
		
		return propertiesConfigName;
	}
    
    public void validatePropertiesConfig(PropertiesConfigDto propertiesConfig) {
		
		log.trace(String.format("Starting verify Properties config. PROPERTIES_CONFIG [%s]", propertiesConfig));
		
		if(propertiesConfig == null || propertiesConfig.getRetryTime() == null || propertiesConfig.getRetryTime().isEmpty()) {
			
			log.error(String.format("Properties Config not found. CONFIG_NAME_ID [%s]", propertiesConfig.getConfigName()));
		}		
	}
	
}
