package com.panamby.readfile.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.panamby.readfile.consts.MongoDBConstants;
import com.panamby.readfile.models.dto.PropertiesConfigDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = MongoDBConstants.READ_FILE_PROPERIES_CONFIG)
public class PropertiesConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Indexed(unique = true)
	private String configName;
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime backlogManagerUnavailabilityErrorDate;
	private List<Long> retryTime;

	public static PropertiesConfigDto toPropertiesConfigDto(PropertiesConfig propertiesConfig) {
		
		PropertiesConfigDto fileProcessingDto = new PropertiesConfigDto();
			 
		BeanUtils.copyProperties(propertiesConfig, fileProcessingDto);          
	 	 
		return fileProcessingDto;
	}

}
