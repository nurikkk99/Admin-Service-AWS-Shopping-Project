package com.epam.adminservice.config;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.model.Bucket;

@Configuration("s3")
public class S3Config {

    private static final Logger logger = LoggerFactory.getLogger((S3Config.class));

    private final String s3Region;
    private final String s3URI;
    private final String bucketName;

    public S3Config(
            @Value("${s3.region}") String s3Region, @Value("${s3.endpointURI}") String s3URI,
            @Value("${s3.bucketName}") String s3bucketName
    ) {
        this.s3Region = s3Region;
        this.s3URI = s3URI;
        this.bucketName = s3bucketName;
    }

    @Bean
    AmazonS3 configureS3Client() {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(s3URI, s3Region)).withPathStyleAccessEnabled(true)
                .build();
        if (!bucketIsExists(s3Client)) {
            logger.info("Creating bucket with name {}", bucketName);
            s3Client.createBucket(bucketName);
        }
        return s3Client;
    }

    private boolean bucketIsExists(AmazonS3 s3) {
        logger.info("Checking bucket with name {} is already exists", bucketName);
        return s3.listBuckets().contains(Bucket.builder().name(bucketName).build());
    }
}
