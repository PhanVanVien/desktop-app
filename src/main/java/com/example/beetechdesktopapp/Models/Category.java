package com.example.beetechdesktopapp.Models;

public class Category {
    private String id;
    private String name;
    private String images;
    private String describe;
    private String model;
    private String manufacturer;
    private String actions;

    public Category() {
    }

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(String id, String name, String images, String describe, String model, String manufacturer,
                    String actions) {
        this.id = id;
        this.name = name;
        this.images = images;
        this.describe = describe;
        this.model = model;
        this.manufacturer = manufacturer;
        this.actions = actions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    @Override
    public String toString() {
        return name;
    }
}