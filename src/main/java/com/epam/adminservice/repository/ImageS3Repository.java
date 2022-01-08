package com.epam.adminservice.repository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

@Repository
public class ImageS3Repository {

    private static Logger logger = LoggerFactory.getLogger(ImageS3Repository.class);
    private final String BUCKET_NAME;
    private String URL;
    private final AmazonS3 s3;

    public ImageS3Repository(@Value("${s3.endpointURI}") String URL,
            @Value("${s3.bucketName}") String bucketName, AmazonS3 s3Client) {
        this.s3 = s3Client;
        this.URL = URL;
        this.BUCKET_NAME = bucketName;
    }

    public String saveImage(String imageKey, MultipartFile file) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, imageKey, file.getInputStream(),
                objectMetadata);
        logger.info("Saving image entity with key = {} in s3 bucket = {}", imageKey, BUCKET_NAME);
        s3.putObject(putObjectRequest);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(URL).append("/").append(BUCKET_NAME).append("/").append(imageKey);
        return stringBuilder.toString();
    }

    public void deleteImage(String imageKey) {
        logger.info("Deleting image entity with key = {} in s3 bucket = {}", imageKey, BUCKET_NAME);
        s3.deleteObject(new DeleteObjectRequest(BUCKET_NAME, imageKey));
    }

    @Transactional
    public void deleteImages(List<String> imageKeys) {
        logger.info("Deleting all images by image keys = {}", imageKeys.toString());
        Iterator<String> iterator= imageKeys.iterator();
        while(iterator.hasNext()) {
            s3.deleteObject(new DeleteObjectRequest(BUCKET_NAME, iterator.next()));
        }
    }
}
