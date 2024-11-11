package com.example.formula1;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Car implements Runnable{

    private String name;
    private int idImage;
    private float x;
    private float y;
    private int fuelTank;
    private long speed;
    private int laps;
    private float distance;
    private float detectionRadius = 65f;
    private int penalty;
    private ArrayList<Sensor> sensors;
    private boolean finished;
    private MainActivity mainActivity;
    private boolean running;
    private Semaphore semaphore;
    private boolean controlSemaphore;

    public Car(String name, int idImage, float x, float y, int fuelTank, long speed, int laps, float distance, int penalty, MainActivity mainActivity, Semaphore semaphore) {

        this.name = name;
        this.idImage = idImage;
        this.x = x;
        this.y = y;
        this.penalty = penalty;
        this.distance = distance;
        this.laps = laps;
        this.speed = speed;
        this.fuelTank = fuelTank;
        this.sensors = new ArrayList<>();
        this.mainActivity = mainActivity;
        this.semaphore = semaphore;
        running = true;
        controlSemaphore = false;
    }

    public String getName(){
        return name;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public int getLaps() {
        return laps;
    }

    public void setLaps(int laps) {
        this.laps = laps;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public int getFuelTank() {
        return fuelTank;
    }

    public void setFuelTank(int fuelTank) {
        this.fuelTank = fuelTank;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public int getIdImage() {
        return idImage;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void addSensors(Sensor sensor){
        sensors.add(sensor);
    }

    public boolean isCollidingWith(Car otherCar, Sensor sensor) {
        try {
            float dx = sensor.getX() - (otherCar.getX() + 60);
            float dy = sensor.getY() - (otherCar.getY() + 25);
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            return distance <= detectionRadius;

        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao verificar se há colisao com outro carro: ", e);
            return false;
        }
    }

    public Sensor getSensor(int index) {
        return sensors.get(index);
    }

    public int getSizeSensors(){
        return sensors.size();
    }

    public void semaphoreRegion(){
        mainActivity.racing(Car.this);
        Log.d("Car","Estou aqui com o carro: " + getIdImage());
    }

    public void freeRegion(){
        mainActivity.racing(Car.this);
    }

    @Override
    public void run() {
        while (running) {
            if(!mainActivity.pause()) {
                try {
                    freeRegion();
                    if (mainActivity.verifySemaphore(Car.this) == 1) {
                        semaphore.acquire(); // Adquira o semáforo antes de entrar na região de corrida
                        semaphoreRegion();
                        controlSemaphore = true;
                        Thread.sleep(70);
                    }
                    if(mainActivity.verifySemaphore(Car.this) == 2 && controlSemaphore) {
                        semaphore.release(); // Libere o semáforo após deixar a região de corrida
                        controlSemaphore = false;
                        Log.d("Car","Liberei o carro: " + getIdImage());
                    }
                    Thread.sleep(70);
                } catch (Exception e) {
                    Log.e("MainActivity", "Erro na regiao de semaforo: ", e);
                }
            }
        }
    }

    public void setFinish(boolean running){
        this.running = running;

    }

}
