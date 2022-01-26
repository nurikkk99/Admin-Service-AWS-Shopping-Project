package com.epam.adminservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue queue() {
        return new Queue("products");
    }

    @Bean
    public Queue queue2() {
        return new Queue("images");
    }
}
