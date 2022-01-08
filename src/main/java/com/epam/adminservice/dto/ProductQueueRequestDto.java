package com.epam.adminservice.dto;

import com.epam.adminservice.dto.CreateGoodDto;

public class ProductQueueRequestDto {
    private String type;
    private CreateGoodDto productDto;
    private String creationDate;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CreateGoodDto getProductDto() {
        return productDto;
    }

    public void setProductDto(CreateGoodDto productDto) {
        this.productDto = productDto;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

}
