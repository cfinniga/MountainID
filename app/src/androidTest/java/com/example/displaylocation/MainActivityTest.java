package com.example.displaylocation;

import android.util.Log;

import org.junit.Test;

import static org.junit.Assert.*;

public class MainActivityTest {

    private static final String TAG = "MainActivityTest";

    @Test
    public void onCreate() {
    }

    @Test
    public void onStart() {
    }

    @Test
    public void onResume() {
    }

    @Test
    public void onPause() {
    }

    @Test
    public void onSensorChanged() {
    }

    @Test
    public void updateOrientationAngles() {
    }

    @Test
    public void onAccuracyChanged() {
    }

    @Test
    public void isMapsEnabled() {
    }

    @Test
    public void isServicesOK() {
    }

    @Test
    public void haversine(){
        double lyonLat = 45.7597;
        double lyonLong = 4.8422;
        double parisLat = 48.8567;
        double parisLong = 2.3508;

        double dist = MainActivity.haversine(lyonLat, lyonLong, parisLat, parisLong);
        double expected = 392.2172595594006;
        double delta = 10;
        assertEquals(expected, dist, delta);

        double nebraskaLat = 41.507483;
        double nebraskaLong = -99.436554;
        double kansasLat = 38.504048;
        double kansasLong = -98.315949;
        expected = 347.3;
        assertEquals(expected,MainActivity.haversine(nebraskaLat,nebraskaLong,kansasLat,kansasLong),delta);

        // 49.292647, -122.829899;  49.398515, -122.946944
        // Mount Seymour
        double myLat = 49.292647;
        double myLong =  -122.829899;
        double mountainLat = 49.398515;
        double mountainLong = -122.946944;
        expected = 14.5;
        delta = 1;
        assertEquals(expected,MainActivity.haversine(myLat,myLong,mountainLat,mountainLong),delta);

        // Burnaby mountain
        mountainLat = 49.277177;
        mountainLong = -122.916175;
        expected = 6.54;
        delta = 1;
        assertEquals(expected,MainActivity.haversine(myLat,myLong,mountainLat,mountainLong),delta);
    }

    @Test
    public void retrieveMountains() {
        double myLat = 49;
        double myLong = 122;
        double azimuth = 0;
        //MainActivity.retrieveMountains(myLat, myLong, azimuth);
    }

    @Test
    public void isInCorrectQuadrant(){
        // Since we don't have any ground truth, these tests will just be checking that the mountain is in the correct quadrant
        //Port moody test
        double myLat = 49.292647;
        double myLong =  -122.829899;
        double mountainLat;
        double mountainLong;
        double delta = (45.0)*Math.PI/180;
        double azimuth;
        double angle;
        double expected;
        double unexpected;

        // Eagle Mountain
        mountainLat = 49.348547;
        mountainLong = -122.830151;
        azimuth = 0;
        angle = MainActivity.getMountainAngle(myLat, myLong, mountainLat, mountainLong);
        Log.d(TAG, "getMountainAngle: Eagle angle " + angle);
        assertEquals(0, angle, delta);

        //azimuth = -2.059;
        /*
        angle = MainActivity.getMountainAngle(myLat, myLong, mountainLat, mountainLong);
        Log.d(TAG, "getMountainAngle: Eagle angle " + angle);
        assertNotEquals(0, angle, delta);
        */

        // Mount Seymour 49.398515, -122.946944
        // Is northwest of "me"
        mountainLat = 49.398515;
        mountainLong = -122.946944;

        expected = -Math.PI/4;
        angle = MainActivity.getMountainAngle(myLat, myLong, mountainLat, mountainLong);
        Log.d(TAG, "getMountainAngle: Seymour angle " + angle);
        assertEquals(expected, angle, delta);

        unexpected = Math.PI/4;
        angle = MainActivity.getMountainAngle(myLat, myLong, mountainLat, mountainLong);
        Log.d(TAG, "getMountainAngle: Seymour angle " + angle);
        assertNotEquals(unexpected, angle, delta);

        // Burnaby Mountain
        mountainLat = 49.277177;
        mountainLong = -122.916175;

        expected = -Math.PI/2;
        angle = MainActivity.getMountainAngle(myLat, myLong, mountainLat, mountainLong);
        Log.d(TAG, "getMountainAngle: Burnaby angle " + angle);
        assertEquals(expected, angle, delta);

        unexpected = 0;
        angle = MainActivity.getMountainAngle(myLat, myLong, mountainLat, mountainLong);
        Log.d(TAG, "getMountainAngle: Burnaby angle " + angle);
        assertNotEquals(unexpected, angle, delta);
    }
}