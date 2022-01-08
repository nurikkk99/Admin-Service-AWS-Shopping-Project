package com.epam.adminservice.controller;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.adminservice.config.S3TestContainer;
import com.epam.adminservice.config.TestContainerConfig;
import com.epam.adminservice.dto.CreateGoodDto;
import com.epam.adminservice.dto.GetImageDto;
import com.epam.adminservice.entity.GoodsType;
import com.epam.adminservice.service.GoodsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.io.Files;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

@SpringBootTest(classes = {TestContainerConfig.class, S3TestContainer.class})
@Testcontainers
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GoodsControllerImageRequestsTest {

    public static String API_PATH = "/api/goods/";

    private CreateGoodDto savedGoodDto;
    private GetImageDto savedImageDto;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    JdbcDatabaseContainer jdbcDatabaseContainer;

    @Autowired
    private GoodsService goodsService;

    @Before
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
        byte [] expectedArray = Files.toByteArray(file);
        FileInputStream inputStream = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
                "file", file.getName(), "image/png", IOUtils.toByteArray(inputStream));

        savedImageDto = goodsService.saveImage(savedGoodDto.getId(),multipartFile);
    }

    @After
    public void dropData() {
        goodsService.deleteAllImagesByGoodId(savedGoodDto.getId());
        goodsService.deleteAll();
    }

    @Test
    public void getAllImagesByGoodIdTest() throws Exception {
        final Collection<GetImageDto> expectedCollection = goodsService.getImagesByGoodId(savedGoodDto.getId());
        assertNotNull(expectedCollection);
        assertFalse(expectedCollection.isEmpty());
        final String urlTemplate = API_PATH + savedGoodDto.getId() + "/image";

        final String contentAsString = mockMvc.perform(get(urlTemplate)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(expectedCollection.size()))).andReturn().getResponse()
                .getContentAsString();

        Collection<GetImageDto> resultCollection = objectMapper.readValue(
                contentAsString,
                TypeFactory.defaultInstance().constructCollectionType(Collection.class, GetImageDto.class)
        );
        assertTrue(resultCollection.containsAll(expectedCollection));
    }

    @Test
    @Ignore
    public void saveImageTest() throws Exception {
        File file = new File("src/test/resources/test_file.png");
        byte [] expectedArray = Files.toByteArray(file);
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
