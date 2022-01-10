package com.epam.adminservice.controller;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Assertions;
import com.epam.adminservice.config.TestContainerConfig;
import com.epam.adminservice.dto.CreateGoodDto;
import com.epam.adminservice.dto.GetImageDto;
import com.epam.adminservice.entity.GoodsType;
import com.epam.adminservice.service.GoodsService;
import com.epam.adminservice.service.QueueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.io.Files;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

@SpringBootTest(classes = {TestContainerConfig.class})
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GoodsControllerImageRequestsTest {

    public static String API_PATH = "/api/goods/";

    private CreateGoodDto savedGoodDto;
    private GetImageDto savedImageDto;

    @MockBean
    QueueService queueService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GoodsService goodsService;

    @BeforeEach
    public void prepareData() throws IOException {
        CreateGoodDto goodDto = new CreateGoodDto();
        goodDto.setName("Stan Smith");
        goodDto.setPrice(BigDecimal.valueOf(7000));
        goodDto.setManufacturer("Adidas");
        goodDto.setType(GoodsType.Sneakers);
        goodDto.setReleaseDate(LocalDateTime.of(1,1,1,1,1,1));
        savedGoodDto = goodsService.save(goodDto);
        prepareImage();
    }

    public void prepareImage() throws IOException {
        File file = new File("src/test/resources/test_file.png");
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
    public void getAllImagesByGoodIdTest() throws Exception {
        final Collection<GetImageDto> expectedCollection = goodsService.getImagesByGoodId(savedGoodDto.getId());
        Assertions.assertNotNull(expectedCollection);
        Assertions.assertFalse(expectedCollection.isEmpty());
        final String urlTemplate = API_PATH + savedGoodDto.getId() + "/image";

        final String contentAsString = mockMvc.perform(get(urlTemplate)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(expectedCollection.size()))).andReturn().getResponse()
                .getContentAsString();

        Collection<GetImageDto> resultCollection = objectMapper.readValue(
                contentAsString,
                TypeFactory.defaultInstance().constructCollectionType(Collection.class, GetImageDto.class)
        );
        Assertions.assertTrue(resultCollection.containsAll(expectedCollection));
    }

    @Test
    @Disabled
    public void saveImageTest() throws Exception {
        File file = new File("src/test/resources/test_file.png");
        FileInputStream inputStream = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", file.getName(), "image/png", IOUtils.toByteArray(inputStream));

        final String urlTemplate = API_PATH + savedGoodDto.getId() + "/image";
        mockMvc.perform(multipart(urlTemplate).file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.imageId").isNotEmpty())
                .andExpect(jsonPath("$.goodId", is(savedGoodDto.getId())));
    }

    @Test
    public void deleteAllImagesTest() throws Exception {
        final String urlTemplate = API_PATH + savedGoodDto.getId() + "/image";
        mockMvc.perform(delete(urlTemplate)).andExpect(status().isOk());
    }

    @Test
    public void getImageTest() throws Exception {
        final String urlTemplate = API_PATH + savedGoodDto.getId() + "/image/" + savedImageDto.getImageId();
        mockMvc.perform(get(urlTemplate))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.imageId").isNotEmpty())
                .andExpect(jsonPath("$.goodId", is(savedGoodDto.getId().intValue())))
                .andExpect(jsonPath("$.imageURI").isNotEmpty());
    }

    @Test
    public void deleteImageTest() throws Exception {
        final String urlTemplate = API_PATH + savedGoodDto.getId() + "/image/" + savedImageDto.getImageId();
        mockMvc.perform(delete(urlTemplate)).andExpect(status().isOk());
    }
}
