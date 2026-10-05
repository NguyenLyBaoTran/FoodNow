package com.example.foodnow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;

public class Restaurant implements Serializable {
    private int id;
    private String name;
    private String address;
    @SerializedName("open_hours")
    private String openHours;
    @SerializedName("logo_url")
    private String logoUrl;
    @SerializedName("cover_url")
    private String coverUrl;
    @SerializedName("foods")
    private ArrayList<Food> menu;
    private ArrayList<Category> categories;

    public Restaurant(String name, String address, String openHours, String logoUrl, String coverUrl) {
        this.name = name;
        this.address = address;
        this.openHours = openHours;
        this.logoUrl = logoUrl;
        this.coverUrl = coverUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getOpenHours() { return openHours; }
    public String getLogoUrl() { return logoUrl; }
    public String getCoverUrl() { return coverUrl; }
    public ArrayList<Food> getMenu() { return menu; }
    public void setMenu(ArrayList<Food> menu) { this.menu = menu; }
    public ArrayList<Category> getCategories() { return categories; }
}
