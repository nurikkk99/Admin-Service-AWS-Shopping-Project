package com.epam.adminservice.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.core.Is.is;

import com.epam.adminservice.config.TestContainerConfig;
import com.epam.adminservice.dto.CreateGoodDto;
import com.epam.adminservice.dto.GetGoodDto;
import com.epam.adminservice.dto.GetImageDto;
import com.epam.adminservice.entity.GoodsType;
import com.epam.adminservice.service.GoodsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = TestContainerConfig.class)
@Testcontainers
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class GoodsControllerTest {

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
    }

    @After
    public void dropData() {
        goodsService.deleteAll();
    }

    @Test
    public void findAllTest() throws Exception {
        final Collection<GetGoodDto> expectedCollection = goodsService.findAll();
        assertFalse("Collection is empty", expectedCollection.isEmpty());

        final String contentAsString = mockMvc.perform(get(API_PATH)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(expectedCollection.size()))).andReturn().getResponse()
                .getContentAsString();

        Collection<GetGoodDto> resultCollection = objectMapper.readValue(
                contentAsString,
                TypeFactory.defaultInstance().constructCollectionType(Collection.class, GetGoodDto.class)
        );
        assertTrue(resultCollection.containsAll(expectedCollection));
    }

    @Test
    public void findOneTest() throws Exception {
        final Collection<GetGoodDto> dtos = goodsService.findAll();
        assertFalse("Collection is empty", dtos.isEmpty());
        final GetGoodDto expectedDto = dtos.iterator().next();
        assertNotNull("Dto is null", expectedDto);

        final String contentAsString = mockMvc.perform(get(API_PATH + expectedDto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE)).andReturn().getResponse()
                .getContentAsString();
        GetGoodDto actualDto = objectMapper.readValue(contentAsString, GetGoodDto.class);
        assertThat(actualDto, is(expectedDto));
    }

    @Test
    public void saveTest() throws Exception {
        CreateGoodDto goodDto = new CreateGoodDto();
        goodDto.setName("Abercrombie & Fitch");
        goodDto.setPrice(BigDecimal.valueOf(5000));
        goodDto.setManufacturer("Levis");
        goodDto.setType(GoodsType.Trousers);

        byte[] content = objectMapper.writeValueAsBytes(goodDto);

        mockMvc.perform(post(API_PATH).contentType(MediaType.APPLICATION_JSON_VALUE).content(content))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name", is(goodDto.getName())))
                .andExpect(jsonPath("$.price").value(goodDto.getPrice()));
    }

    @Test
    public void putTest() throws Exception {
        final Long id = savedGoodDto.getId();
        final BigDecimal originalPrice = savedGoodDto.getPrice();
        final BigDecimal updatedPrice = BigDecimal.valueOf(100000);
        assertNotEquals(originalPrice,updatedPrice);
        savedGoodDto.setPrice(updatedPrice);
        savedGoodDto.setId(null);
        savedGoodDto.setReleaseDate(null);
        byte[] content = objectMapper.writeValueAsBytes(savedGoodDto);

        mockMvc.perform(put(API_PATH + id)
                        .contentType(MediaType.APPLICATION_JSON_VALUE).content(content))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name", is(savedGoodDto.getName())))
                .andExpect(jsonPath("$.price").value(updatedPrice));
    }

    @Test
    public void deleteOneTest() throws Exception {
        final Collection<GetGoodDto> dtos = goodsService.findAll();
        assertFalse("Collection is empty", dtos.isEmpty());
        final GetGoodDto expectedDto = dtos.iterator().next();
        assertNotNull("Dto is null", expectedDto);

        final String urlTemplate = API_PATH + expectedDto.getId();
        mockMvc.perform(delete(urlTemplate)).andExpect(status().isOk());
    }
}
