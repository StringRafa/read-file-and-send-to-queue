package com.panamby.readfile.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.panamby.readfile.models.PropertiesConfig;

public interface PropertiesConfigRepository extends MongoRepository<PropertiesConfig, String>{

	PropertiesConfig findByConfigName(String configName);
}
