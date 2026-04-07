package com.example.planexia.data;

public class Module {
    private int id;
    private String name;
    private int coefficient;
    private String color;

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
}