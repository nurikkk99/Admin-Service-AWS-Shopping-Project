package com.epam.adminservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class S3TestContainer {

    DockerImageName localstackImage = DockerImageName.parse("localstack/localstack");

    @Bean (initMethod = "start", destroyMethod = "stop")
    LocalStackContainer localStackContainer () {
        return new LocalStackContainer(localstackImage).withServices(Service.S3);
    }
}
