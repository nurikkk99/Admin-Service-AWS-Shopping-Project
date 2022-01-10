package com.epam.adminservice.dto;

public interface EntityDtoMapper<T,E> {

  T entityToDto(E entity);

  E dtoToEntity();
}
