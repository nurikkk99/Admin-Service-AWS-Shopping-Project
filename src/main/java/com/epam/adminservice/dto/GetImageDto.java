package com.epam.adminservice.dto;

import com.epam.adminservice.entity.ImageEntity;
import java.util.Objects;

public class GetImageDto implements EntityDtoMapper<GetImageDto, ImageEntity>{
    private long imageId;
    private long goodId;
    private String imageURI;

    public long getImageId() {
        return imageId;
    }

    public void setImageId(long imageId) {
        this.imageId = imageId;
    }

    public long getGoodId() {
        return goodId;
    }

    public void setGoodId(long goodId) {
        this.goodId = goodId;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    @Override
    public GetImageDto entityToDto(ImageEntity imageEntity) {
        GetImageDto imageDto = new GetImageDto();
        imageDto.setImageId(imageEntity.getId());
        imageDto.setImageURI(imageEntity.getImageURI());
        imageDto.setGoodId(imageEntity.getGoodEntity().getId());
        return imageDto;
    }

    @Override
    public ImageEntity dtoToEntity() {
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setId(this.imageId);
        imageEntity.setImageURI(this.imageURI);
        return imageEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetImageDto that = (GetImageDto) o;
        return imageId == that.imageId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageId);
    }
}
