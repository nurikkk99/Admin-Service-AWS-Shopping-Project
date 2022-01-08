package com.epam.adminservice.repository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.epam.adminservice.config.S3TestContainer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.io.Files;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

@SpringBootTest(classes = S3TestContainer.class)
@Testcontainers
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class ImageS3RepositoryTest {

    @Autowired
    LocalStackContainer localStackContainer;

    @Autowired
    AmazonS3 s3Client;

    @Autowired
    ImageS3Repository imageS3Repository;

    @Value("${s3.bucketName}")
    String bucketName;

    @Test
    public void doSomething(){
        System.out.println();
    }

    @Test
    public void saveImageTest() throws IOException {
        String key = "testKey";
        File file = new File("src/test/resources/test_file.png");
        byte [] expectedArray = Files.toByteArray(file);
        FileInputStream inputStream = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
                "file", file.getName(), "image/png", IOUtils.toByteArray(inputStream));

        imageS3Repository.saveImage(key, multipartFile);
        S3Object s3Object = s3Client.getObject(bucketName,key);
        S3ObjectInputStream inputStream1 = s3Object.getObjectContent();
        byte [] actualArray = IOUtils.toByteArray(s3Object.getObjectContent());

        Assert.assertArrayEquals(expectedArray, actualArray);
    }

}
