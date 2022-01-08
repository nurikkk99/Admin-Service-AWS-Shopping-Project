package com.epam.adminservice.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "image")
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "s3_uri")
    private String imageURI;

    @Column(name = "s3_key")
    private String imageKey;

    @ManyToOne
    @JoinColumn(name = "good_id")
    public GoodEntity goodEntity;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public GoodEntity getGoodEntity() {
        return goodEntity;
    }

    public void setGoodEntity(GoodEntity goodEntity) {
        this.goodEntity = goodEntity;
    }
}
