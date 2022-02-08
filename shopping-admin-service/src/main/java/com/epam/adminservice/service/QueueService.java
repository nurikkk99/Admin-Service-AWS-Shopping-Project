package com.epam.adminservice.service;

import com.epam.adminservice.dto.GetImageDto;
import com.epam.adminservice.dto.ImageQueueRequestDto;
import com.epam.adminservice.dto.ProductIdQueueRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public void addProductToQueue(String type, Long productId) {
        ProductIdQueueRequestDto productIdQueueRequestDto = new ProductIdQueueRequestDto();
        productIdQueueRequestDto.setType(type);
        productIdQueueRequestDto.setProductId(productId);
        logger.info("Adding good entity with id {} to queue. {} - operation type", productId, type);
        try {
            amqpTemplate.convertAndSend("products", objectMapper.writeValueAsString(productIdQueueRequestDto));
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
