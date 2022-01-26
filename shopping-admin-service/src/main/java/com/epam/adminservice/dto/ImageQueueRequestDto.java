package com.epam.adminservice.dto;

public class ImageQueueRequestDto {

    private String type;
    private GetImageDto imageDto;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GetImageDto getImageDto() {
        return imageDto;
    }

    public void setImageDto(GetImageDto imageDto) {
        this.imageDto = imageDto;
    }
}
