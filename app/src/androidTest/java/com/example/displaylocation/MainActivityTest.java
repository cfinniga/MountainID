package com.example.displaylocation;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;

import java.util.MissingFormatArgumentException;

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
        // Test data from Google Maps
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
    }

    @Test
    public void getClosestMountain() {
        //
        String collection = "zone1";
        double myLat = 49.292647;
        double myLong =  -122.829899;
        double azimuth = 0;
        String closestMountain = "";
        double globalMinDistance = Double.POSITIVE_INFINITY;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(collection).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    MountainInfo info = MainActivity.getClosestMountain(task, myLat, myLong, azimuth);
                    String mountain = info.name;
                    double distance = info.distance;
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }

            }
        });
    }



    @Test
    public void isInCorrectQuadrant(){
        // These tests will just be checking that the mountain is in the correct quadrant
        // Testing with (myLat, myLong) in Port moody
        double myLat = 49.292647;
        double myLong =  -122.829899;
        double mountainLat;
        double mountainLong;
        double delta = (45.0)*Math.PI/180;
        double angle;
        double expected;
        double unexpected;

        // Eagle Mountain Test
        // Expected: North of me
        mountainLat = 49.348547;
        mountainLong = -122.830151;
        angle = MainActivity.getMountainAngle(myLat, myLong, mountainLat, mountainLong);
        Log.d(TAG, "getMountainAngle: Eagle angle " + angle);
        assertEquals(0, angle, delta);

        // Basic test 1
        myLat = 0;
        myLong = 10;
        mountainLat = 45;
        mountainLong = 10;
        expected = 0;
        delta = 0.2;

        angle = MainActivity.getMountainAngle(myLat, myLong, mountainLat, mountainLong);
        Log.d(TAG, "getMountainAngle: test angle " + angle);
        assertEquals(expected, angle, delta);

        // Basic test 1
        myLat = 0;
        myLong = 0;
        mountainLat = 90;
        mountainLong = 0;
        expected = 0;
        delta = 0.2;

        angle = MainActivity.getMountainAngle(myLat, myLong, mountainLat, mountainLong);
        Log.d(TAG, "getMountainAngle: test angle " + angle);
        assertEquals(expected, angle, delta);

        // Basic test 2
        myLat = 0;
        myLong = 90;
        mountainLat = 0;
        mountainLong = 0;
        expected = -Math.PI/2;
        delta = 0.2;

        angle = MainActivity.getMountainAngle(myLat, myLong, mountainLat, mountainLong);
        Log.d(TAG, "getMountainAngle: test angle2 " + angle);
        assertEquals(expected, angle, delta);

        // Mount Seymour test 49.398515, -122.946944
        // Expected: northwest of me
        mountainLat = 49.398515;
        mountainLong = -122.946944;
        myLat = 49.292647;
        myLong =  -122.829899;

        expected = -Math.PI/4; // This is actually not completely correct for a sphere
        delta = Math.PI/4;
        angle = MainActivity.getMountainAngle(myLat, myLong, mountainLat, mountainLong);
        Log.d(TAG, "getMountainAngle: Seymour angle " + angle);
        assertEquals(expected, angle, delta);

        unexpected = Math.PI/4;
        angle = MainActivity.getMountainAngle(myLat, myLong, mountainLat, mountainLong);
        Log.d(TAG, "getMountainAngle: Seymour angle " + angle);
        assertNotEquals(unexpected, angle, delta);

        // Burnaby Mountain test
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