package com.example.formula1;

import static org.junit.Assert.*;

import android.graphics.Point;
import android.text.Layout;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class adjustCoordinatesTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() throws Exception {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(activity -> mainActivity = activity);
    }

    @Test
    public void adjustCoordinates() {
        float layoutX = 650;
        float layoutY = 710;
        Point expectedPoint = new Point(1540, 1686);
        Point result = mainActivity.adjustCoordinates(layoutX, layoutY);
        assertEquals(expectedPoint, result);
    }
}