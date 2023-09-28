package com.panamby.readfile.models.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.panamby.readfile.models.PropertiesConfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertiesConfigDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Indexed(unique = true)
	private String configName;
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime backlogManagerUnavailabilityErrorDate;
	private List<Long> retryTime;

	public static PropertiesConfig toPropertiesConfig(PropertiesConfigDto propertiesConfigDto) {
		
		PropertiesConfig propertiesConfig = new PropertiesConfig();
			 
		BeanUtils.copyProperties(propertiesConfigDto, propertiesConfig);           
	 	 
		return propertiesConfig;
	}

}
