package com.example.beetechdesktopapp.Models;


public class Asset {
    String assetDetails;
    String name;
    String category;
    String type;
    String parent;
    String barCode;
    String rfid;
    String serial;
    String state;
    String status;
    String dueDate;
    String dept;
    String actions;

    public Asset(String assetDetails, String name, String category, String type, String parent, String barCode,
                 String rfid, String serial, String state, String status, String dueDate, String dept, String actions) {
        super();
        this.assetDetails = assetDetails;
        this.name = name;
        this.category = category;
        this.type = type;
        this.parent = parent;
        this.barCode = barCode;
        this.rfid = rfid;
        this.serial = serial;
        this.state = state;
        this.status = status;
        this.dueDate = dueDate;
        this.dept = dept;
        this.actions = actions;
    }

    public Asset() {

    }

    public String getAssetDetails() {
        return assetDetails;
    }

    public void setAssetDetails(String assetDetails) {
        this.assetDetails = assetDetails;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this .state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

}