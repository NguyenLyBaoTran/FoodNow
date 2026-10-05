package com.example.foodnow.model;

import java.io.Serializable;
import java.util.List;

public class Category implements Serializable {
    private int id;
    private String name;
    private List<Food> foods;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Food> getFoods() {
        return foods;
    }
}
