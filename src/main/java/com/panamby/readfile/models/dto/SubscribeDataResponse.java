package com.panamby.readfile.models.dto;

import java.util.List;

import com.panamby.readfile.models.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeDataResponse {

    private String transactionId;
    private String id;
	private List<Product> products;	
}
