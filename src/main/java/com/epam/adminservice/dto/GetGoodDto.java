package com.epam.adminservice.dto;

import com.epam.adminservice.entity.GoodEntity;
import com.epam.adminservice.entity.GoodsType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.List;

public class GetGoodDto implements EntityDtoMapper<GetGoodDto, GoodEntity> {

    private Long id;
    private String name;
    private GoodsType type;
    private BigDecimal price;
    private String manufacturer;
    private LocalDateTime releaseDate;

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GoodsType getType() {
        return type;
    }

    public void setType(GoodsType type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }


    @Override
    public GetGoodDto entityToDto(GoodEntity goodEntity) {
        GetGoodDto goodDto = new GetGoodDto();
        goodDto.setId(goodEntity.getId());
        goodDto.setName(goodEntity.getName());
        Optional.ofNullable(goodEntity.getType()).ifPresent(x -> goodDto.setType(GoodsType.valueOf(x)));
        goodDto.setPrice(goodEntity.getPrice());
        goodDto.setManufacturer(goodEntity.getManufacturer());
        goodDto.setReleaseDate(goodEntity.getReleaseDate());
        return goodDto;
    }

    @Override
    public GoodEntity dtoToEntity() {
        GoodEntity goodEntity = new GoodEntity();
        goodEntity.setId(this.id);
        goodEntity.setName(this.name);
        Optional.ofNullable(this.type).ifPresent(x -> goodEntity.setType(x.toString()));
        goodEntity.setPrice(this.price);
        goodEntity.setManufacturer(this.getManufacturer());
        goodEntity.setReleaseDate(this.releaseDate);
        return goodEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetGoodDto that = (GetGoodDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
