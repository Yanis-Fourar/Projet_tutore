package com.example.planexia.model;

public class Module {
    private int id;
    private String name;
    private int coefficient;
    private String color; // ex: "#4F8EF7"

    public Module(int id, String name, int coefficient, String color) {
        this.id = id;
        this.name = name;
        this.coefficient = coefficient;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCoefficient() {
        return coefficient;
    }

    public String getColor() {
        return color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoefficient(int coefficient) {
        this.coefficient = coefficient;
    }

    public void setColor(String color) {
        this.color = color;
    }
}