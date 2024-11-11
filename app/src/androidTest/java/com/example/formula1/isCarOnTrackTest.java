package com.example.formula1;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class isCarOnTrackTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(activity -> mainActivity = activity);
    }

    @Test
    public void isCarOnTrack() {
        boolean result1 = mainActivity.isCarOnTrack(500, 300, null, null);
        assertFalse("O carro esta fora da pista", result1);

        boolean result2 = mainActivity.isCarOnTrack(650, 710, null, null);
        assertTrue("O carro esta dentro da pista", result2);
    }
}