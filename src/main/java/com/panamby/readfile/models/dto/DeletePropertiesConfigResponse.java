package com.panamby.readfile.models.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeletePropertiesConfigResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String message;
    
}
