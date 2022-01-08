package com.epam.adminservice.dto;

import com.epam.adminservice.entity.GoodEntity;

public interface EntityDtoMapper<T,E> {

  T entityToDto(E entity);

  E dtoToEntity();
}
