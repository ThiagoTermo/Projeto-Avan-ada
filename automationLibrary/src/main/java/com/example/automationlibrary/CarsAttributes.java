package com.example.automationlibrary;

public class CarsAttributes {

    private String name;
    private int idImage;
    private int x;
    private int y;
    private long speed;
    private long lineSpeed;
    private int laps;
    private float distance;
    private float rotation;

    public CarsAttributes(String name, int idImage, int x, int y, long speed, long lineSpeed, int laps, float distance, float rotation) {

        this.name = name;
        this.idImage = idImage;
        this.x = x;
        this.y = y;
        this.distance = distance;
        this.laps = laps;
        this.speed = speed;
        this.rotation = rotation;
        this.lineSpeed = lineSpeed;
    }

    public String getName() {
        return name;
    }

    public int getIdImage() {
        return idImage;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLaps() {
        return laps;
    }

    public long getSpeed() {
        return speed;
    }

    public long getLineSpeed(){
        return lineSpeed;
    }

    public float getDistance() {
        return distance;
    }

    public float getRotation() {
        return rotation;
    }
}
