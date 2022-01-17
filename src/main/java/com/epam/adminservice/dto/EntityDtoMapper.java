package com.epam.adminservice.dto;

public interface EntityDtoMapper<H,E> {

  H entityToDto(E entity);

  E dtoToEntity();
}
