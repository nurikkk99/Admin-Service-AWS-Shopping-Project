package com.epam.adminservice.service;

import com.epam.adminservice.dto.CreateGoodDto;
import com.epam.adminservice.dto.GetImageDto;
import com.epam.adminservice.entity.ImageEntity;
import com.epam.adminservice.dto.ImageQueueRequestDto;
import com.epam.adminservice.dto.ProductQueueRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

    private static Logger logger = LoggerFactory.getLogger(QueueService.class);

    private AmqpTemplate amqpTemplate;
    private ObjectMapper objectMapper;

    public QueueService( AmqpTemplate amqpTemplate, ObjectMapper objectMapper) {
        this.amqpTemplate = amqpTemplate;
        this.objectMapper = objectMapper;
    }

    public void addProductToQueue(String type, CreateGoodDto productDto) {
        ProductQueueRequestDto productQueueRequestDto = new ProductQueueRequestDto();
        productQueueRequestDto.setType(type);
        productQueueRequestDto.setProductDto(productDto);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss");
        String creationDate = (productDto.getReleaseDate().format(formatter));
        productQueueRequestDto.setCreationDate(creationDate);

        logger.info("Adding good entity with id {} to queue. {} - operation type", productDto.getId(), type);
        try {
            amqpTemplate.convertAndSend("products", objectMapper.writeValueAsString(productQueueRequestDto));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void addImageToQueue(String type, GetImageDto imageDto) {
        logger.info("Add image entity with id {} to queue. {} - operation type", imageDto.getImageId(), type);
        ImageQueueRequestDto imageQueueRequestDto = new ImageQueueRequestDto();
        imageQueueRequestDto.setType(type);
        imageQueueRequestDto.setImageDto(imageDto);

        try {
            amqpTemplate.convertAndSend("images", objectMapper.writeValueAsString(imageQueueRequestDto));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
