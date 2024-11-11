package com.example.formula1;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class verifySemaphoreTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(activity -> mainActivity = activity);
    }

    @Test
    public void verifySemaphoreRed() {
        Car car = new Car(null, 0, 340, 300, 0, 0, 0, 0, 0, null, null);
        car.addSensors(new Sensor());

        mainActivity.updateSensorPositions(car, null, null, car.getSensor(0), -90);

        int result = mainActivity.verifySemaphore(car);
        assertEquals(1, result);

    }

    @Test
    public void verifySemaphoreBlue() {
        Car car = new Car(null, 0, 100, 295, 0, 0, 0, 0, 0, null, null);
        car.addSensors(new Sensor());

        mainActivity.updateSensorPositions(car, null, null, car.getSensor(0), -90);

        int result = mainActivity.verifySemaphore(car);
        assertEquals(2, result);

    }
}