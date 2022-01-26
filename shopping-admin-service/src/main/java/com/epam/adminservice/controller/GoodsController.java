package com.epam.adminservice.controller;

import com.epam.adminservice.dto.CreateGoodDto;
import com.epam.adminservice.dto.GetGoodDto;
import com.epam.adminservice.dto.GetImageDto;
import com.epam.adminservice.dto.UpdateGoodDto;
import com.epam.adminservice.service.GoodsService;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    private static Logger logger = LoggerFactory.getLogger(GoodsController.class);
    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @GetMapping
    public Collection<GetGoodDto> findAll(@PageableDefault() Pageable pageable) {
        return goodsService.findAll(pageable);
    }

    @PostMapping
    public CreateGoodDto save(@RequestBody @Valid CreateGoodDto goodDto) {
        logger.info("Saving good entity (name = {}, manufacturer = {}, price = {})", goodDto.getName(),
                goodDto.getManufacturer(), goodDto.getPrice()
        );
        return goodsService.save(goodDto);
    }

    @GetMapping("/{id}")
    public GetGoodDto findOne(@PathVariable("id") Long id) {
        return goodsService.findOne(id);
    }

    @PutMapping("/{id}")
    public UpdateGoodDto updateOne(@PathVariable("id") long id, @RequestBody @Valid UpdateGoodDto goodDto) {
        logger.info("Updating good entity with id = {}", id);
        return goodsService.update(id, goodDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteOne(@PathVariable("id") long id) {
        logger.info("Deleting good entity with id = {}", id);
        goodsService.deleteOne(id);
        return ResponseEntity.ok().body("Entity with id " + id + " was deleted");
    }

    @GetMapping("/{id}/image")
    public List<GetImageDto> getAllImages(@PathVariable("id") long goodId) {
        return goodsService.getImagesByGoodId(goodId);
    }

    @PostMapping("/{id}/image")
    public GetImageDto saveImage(
            @PathVariable("id") long goodId, @RequestParam(value = "image") MultipartFile image
    ) throws IOException {
        logger.info("Saving image entity with good id = {}", goodId);
        return goodsService.saveImage(goodId, image);
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity deleteAllImages(@PathVariable("id") long goodId) {
        logger.info("Deleting all images with good id = {}", goodId);
        goodsService.deleteAllImagesByGoodId(goodId);
        return ResponseEntity.ok().body("Images with good id " + goodId + " was deleted");
    }

    @GetMapping("/{id}/image/{imageId}")
    public GetImageDto getImage(@PathVariable("id") long goodId, @PathVariable("imageId") long imageId)  {
        return goodsService.getImageByImageId(imageId);
    }

    @DeleteMapping("/{id}/image/{imageId}")
    public ResponseEntity deleteImage(@PathVariable("id") long goodId, @PathVariable("imageId") long imageId) {
        logger.info("Deleting image entity with id = {}", imageId);
        goodsService.deleteImage(imageId);
        return ResponseEntity.ok().body("Image with id " + imageId + " was deleted");
    }

    //TO DELETE
    @DeleteMapping
    public void deleteAll() {
        goodsService.deleteAll();
    }
}
