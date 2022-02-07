package com.epam.adminservice.service;

import com.epam.adminservice.dto.CreateGoodDto;
import com.epam.adminservice.dto.GetGoodDto;
import com.epam.adminservice.dto.GetImageDto;
import com.epam.adminservice.entity.GoodEntity;
import com.epam.adminservice.entity.ImageEntity;
import com.epam.adminservice.dto.UpdateGoodDto;
import com.epam.adminservice.exception.EntityNotFoundException;
import com.epam.adminservice.repository.ImageS3Repository;
import com.epam.adminservice.repository.GoodsRepository;
import com.epam.adminservice.repository.ImageSqlRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GoodsService {

    private static Logger logger = LoggerFactory.getLogger(GoodsService.class);

    private GoodsRepository goodsRepository;
    private ImageS3Repository imageS3Repository;
    private ImageSqlRepository imageSqlRepository;
    private GetGoodDto getGoodDto;
    private GetImageDto getImageDto;
    private List<String> validImageFormats;
    private QueueService queueService;

    public GoodsService(
            GoodsRepository goodsRepository, ImageS3Repository imageS3Repository, ImageSqlRepository imageSqlRepository,
            @Value("${valid-image-formats}") List<String> validImageFormats, QueueService queueService) {
        this.goodsRepository = goodsRepository;
        this.imageS3Repository = imageS3Repository;
        this.imageSqlRepository = imageSqlRepository;
        this.getGoodDto = new GetGoodDto();
        this.getImageDto = new GetImageDto();
        this.validImageFormats = validImageFormats;
        this.queueService = queueService;
    }

    public List<GetGoodDto> findAll() {
        return goodsRepository.findAll().stream().map(getGoodDto::entityToDto).collect(Collectors.toList());
    }

    public List<GetGoodDto> findAll(Pageable pageable) {
        return goodsRepository.findAll(pageable).get().map(getGoodDto::entityToDto).collect(Collectors.toList());
    }

    public CreateGoodDto save(CreateGoodDto goodDto) {
        goodDto.setReleaseDate(LocalDateTime.now());
        logger.info("Saving good entity (name = {}, manufacturer = {}, price = {}, release date = {})", goodDto.getName(),
                goodDto.getManufacturer(), goodDto.getPrice(), goodDto.getReleaseDate()
        );
        CreateGoodDto savedGoodDto = goodDto.entityToDto(goodsRepository.save(goodDto.dtoToEntity()));
        logger.info("Good entity was saved with id = {}", savedGoodDto.getId());
        queueService.addProductToQueue("save", savedGoodDto.getId());
        return savedGoodDto;
    }


    public UpdateGoodDto update(Long id, UpdateGoodDto goodDto) {
        GoodEntity goodEntity = goodsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Good entity with id " + id + " does not exist"));
        logger.info("Updating good entity with id = {}", id);
        goodDto.setId(id);
        goodDto.setReleaseDate(getGoodDto.entityToDto(goodEntity).getReleaseDate());
        GoodEntity savedEntity = goodsRepository.save(goodDto.dtoToEntity());
        queueService.addProductToQueue("update", savedEntity.getId());
        return goodDto.entityToDto(savedEntity);
    }

    public GetGoodDto findOne(Long id) {
        GoodEntity goodEntity = goodsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Good entity with id " + id + " does not exist"));
        return getGoodDto.entityToDto(goodEntity);
    }

    public void deleteOne(Long id) {
        goodsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Good entity with id " + id + " does not exist"));
        logger.info("Deleting good entity with id = {}", id);
        goodsRepository.deleteById(id);
    }

    public void deleteAll() {
        logger.info("Deleting all good entities");
        goodsRepository.deleteAll();
    }

    public List<GetImageDto> getImagesByGoodId(long id) {
        GoodEntity goodEntity = goodsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Good entity with id " + id + " does not exist"));
        return imageSqlRepository.findAllByGoodEntity(goodEntity).stream().map(getImageDto::entityToDto)
                .collect(Collectors.toList());
    }

    public GetImageDto getImageByImageId(long id) {
        ImageEntity imageEntity = imageSqlRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Image with id " + id + " does not exist"));
        return getImageDto.entityToDto(imageEntity);
    }

    public GetImageDto saveImage(long id, MultipartFile file) throws IOException {
        GoodEntity goodEntity = goodsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Good entity with id " + id + " does not exist"));
        String fileFormat = determineAndValidateFileFormat(file.getBytes());
        String imageKey = String.join(".", generateImageKey(goodEntity), fileFormat);
        String imageURI;
        logger.info("Saving image of good entity with id = {} in s3 repository", id);
        try {
            imageURI = imageS3Repository.saveImage(imageKey, file);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setImageURI(imageURI);
        imageEntity.setGoodEntity(goodEntity);
        imageEntity.setImageKey(imageKey);
        logger.info("Saving image entity with good id = {}, imageURI = {} in relational database", id, imageURI);
        ImageEntity savedImageEntity = imageSqlRepository.save(imageEntity);
        logger.info("Image entity was saved in relational database with id = {}", savedImageEntity.getId());
        queueService.addImageToQueue("save", getImageDto.entityToDto(savedImageEntity));
        return getImageDto.entityToDto(savedImageEntity);
    }

    public void deleteImage(long imageId) {
        ImageEntity imageEntity = imageSqlRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image with id " + imageId + " does not exist"));
        logger.info("Deleting image entity with image id = {} in s3 repository", imageId);
        imageS3Repository.deleteImage(imageEntity.getImageKey());
        logger.info("Deleting image entity with image id = {} in relational database", imageId);
        imageSqlRepository.deleteById(imageId);
        queueService.addImageToQueue("delete", getImageDto.entityToDto(imageEntity));
    }

    public void deleteAllImagesByGoodId(long goodId) {
        GoodEntity goodEntity = goodsRepository.findById(goodId)
                .orElseThrow(() -> new EntityNotFoundException("Entity with id " + goodId + " does not exist"));
        List<ImageEntity> imageEntities = imageSqlRepository.findAllByGoodEntity(goodEntity);
        List<String> imageKeys = imageEntities.stream().map(x -> x.getImageKey()).collect(Collectors.toList());
        logger.info("Deleting images with good id {} in s3 repository", goodId);
        imageS3Repository.deleteImages(imageKeys);
        logger.info("Deleting images with good id {} in relational database", goodId);
        imageSqlRepository.deleteAllByGoodEntity(goodEntity);

        Iterator<ImageEntity> iterator = imageEntities.iterator();
        while (iterator.hasNext()){
            queueService.addImageToQueue("delete", getImageDto.entityToDto(iterator.next()));
        }
    }

    private String generateImageKey(GoodEntity goodEntity) {
        int imageNumber = Optional.ofNullable(goodEntity.getImagesList().size()).orElse(0) + 1;
        String imageKey = String.join(".", goodEntity.getId().toString(), Integer.toString(imageNumber),
                goodEntity.getManufacturer()
        );
        return imageKey;
    }

    private String determineAndValidateFileFormat(byte[] fileAsByteArray) throws IOException {
        Iterator<ImageReader> imageReaders;
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(
                new ByteArrayInputStream(fileAsByteArray))) {
            imageReaders = ImageIO.getImageReaders(imageInputStream);
        }

        String formatName;
        if (imageReaders.hasNext()) {
            ImageReader imageReader = imageReaders.next();
            formatName = imageReader.getFormatName();
        } else {
            throw new IllegalArgumentException();
        }
        if (validImageFormats.contains(formatName.toLowerCase())) {
            return formatName.toLowerCase();
        } else {
            throw new IllegalArgumentException("Image format " + formatName + " is unsupported");
        }
    }
}
