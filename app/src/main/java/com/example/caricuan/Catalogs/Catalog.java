package com.example.caricuan.Catalogs;

public class Catalog {
    private String title;
    private String desc;
    private String images;
    private String user;
    private String classes;
    private double price;
    private int stocks;

    public Catalog(){

    }

    public Catalog(String title, String desc, String images, String user, String classes, double price, int stocks) {
        this.title = title;
        this.desc = desc;
        this.images = images;
        this.user = user;
        this.classes = classes;
        this.price = price;
        this.stocks = stocks;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getImages() {
        return images;
    }

    public String getUser() {
        return user;
    }

    public String getClasses(){
        return classes;
    }

    public double getPrice() {
        return price;
    }

    public int getStocks() {
        return stocks;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStocks(int stocks) {
        this.stocks = stocks;
    }

}
