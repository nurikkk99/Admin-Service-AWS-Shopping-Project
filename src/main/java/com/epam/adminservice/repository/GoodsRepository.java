package com.epam.adminservice.repository;

import com.epam.adminservice.entity.GoodEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface GoodsRepository extends JpaRepository<GoodEntity, Long> {

}
