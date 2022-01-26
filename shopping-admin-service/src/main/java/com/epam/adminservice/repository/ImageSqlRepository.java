package com.epam.adminservice.repository;

import com.epam.adminservice.entity.GoodEntity;
import com.epam.adminservice.entity.ImageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ImageSqlRepository extends JpaRepository<ImageEntity, Long> {
    List<ImageEntity> findAllByGoodEntity(GoodEntity goodEntity);

    @Transactional
    void deleteAllByGoodEntity(GoodEntity goodEntity);
}
