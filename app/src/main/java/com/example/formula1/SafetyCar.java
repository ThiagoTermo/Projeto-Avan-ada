package com.example.formula1;

import java.util.concurrent.Semaphore;

public class SafetyCar extends Car{

    public SafetyCar(String name, int idImage, float x, float y, int fuelTank, long speed, int laps, float distance, int penalty, MainActivity mainActivity, Semaphore semaphore) {
        super(name, idImage, x, y, fuelTank, speed, laps, distance, penalty, mainActivity, semaphore);
    }

}
