package com.epam.adminservice.service;

import org.junit.jupiter.api.Assertions;
import com.epam.adminservice.config.TestContainerConfig;
import com.epam.adminservice.dto.CreateGoodDto;
import com.epam.adminservice.dto.GetImageDto;
import com.epam.adminservice.entity.GoodsType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.io.Files;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

@SpringBootTest(classes = {TestContainerConfig.class})
@Testcontainers
@ActiveProfiles("test")
public class GoodsServiceTest {

    private static final String IMAGE_KEY = "testKey";

    @MockBean
    QueueService queueService;

    @Value("${s3.bucketName}")
    private String bucketName;

    private CreateGoodDto savedGoodDto;
    private GetImageDto savedImageDto;

    @Autowired
    private JdbcDatabaseContainer jdbcDatabaseContainer;

    @Autowired
    private GoodsService goodsService;

    @BeforeEach
    public void prepareData() throws IOException {
        CreateGoodDto goodDto = new CreateGoodDto();
        goodDto.setId(1L);
        goodDto.setName("AirForce");
        goodDto.setType(GoodsType.Sneakers);
        goodDto.setManufacturer("Nike");
        goodDto.setPrice(BigDecimal.valueOf(7000));
        goodDto.setReleaseDate(LocalDateTime.now());
        savedGoodDto = goodsService.save(goodDto);

        File file = new File("src/test/resources/test_file.png");
        byte [] expectedArray = Files.toByteArray(file);
        FileInputStream inputStream = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
                "file", file.getName(), "image/png", IOUtils.toByteArray(inputStream));

        savedImageDto = goodsService.saveImage(savedGoodDto.getId(),multipartFile);
    }

    @AfterEach
    public void dropData() {
        goodsService.deleteAllImagesByGoodId(savedGoodDto.getId());
        goodsService.deleteAll();
    }

    @Test
    public void getImagesByGoodIdTest() {
        List<GetImageDto> expectedList = new ArrayList<>();
        expectedList.add(savedImageDto);
        List<GetImageDto> actualList = goodsService.getImagesByGoodId(savedGoodDto.getId());
        Assertions.assertEquals(expectedList, actualList);
    }

    @Test
    public void getImageByImageIdTest() {
        GetImageDto getImageDto = goodsService.getImageByImageId(savedImageDto.getImageId());
        Assertions.assertEquals(savedImageDto, getImageDto);
    }

    @Test
    public void deleteImageByImageTest() {
        goodsService.deleteImage(savedImageDto.getImageId());
        Assertions.assertThrows(Exception.class, ()->goodsService.getImageByImageId(savedImageDto.getImageId()));
    }

    @Test
    public void deleteAllImagesByGoodIdTest() {
        goodsService.deleteAllImagesByGoodId(savedGoodDto.getId());
        Assertions.assertThrows(Exception.class, ()->goodsService.getImageByImageId(savedImageDto.getImageId()));
    }

    @Test
    public void saveThrowsIllegalArgumentExceptionIfImageFormatIsInvalid() throws IOException {
        File file = new File("src/test/resources/invalid_format_image.gif");
        FileInputStream inputStream = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
                "file", file.getName(), "image/png", IOUtils.toByteArray(inputStream));
        Assertions.assertThrows(IllegalArgumentException.class, () -> goodsService.saveImage(savedGoodDto.getId(), multipartFile));
    }
}
