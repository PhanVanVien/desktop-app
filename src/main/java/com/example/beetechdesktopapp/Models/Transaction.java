package com.example.beetechdesktopapp.Models;

public class Transaction {
    private String transactionID;
    private String type;
    private String date;
    private String creator;

    public Transaction() {

    }

    public Transaction(String transactionID, String type, String date, String creator) {
        this.transactionID = transactionID;
        this.type = type;
        this.date = date;
        this.creator = creator;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
