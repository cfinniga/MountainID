/* Location getting code from youtube tutorial by Android Coding
How to Get Current Location in Android Studio | CurrentLocation | Android Coding
https://www.youtube.com/watch?v=Ak1O9Gip-pg

 */

package com.example.displaylocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MainActivity";

    Button btLocation;
    TextView textView1, textView2, textView3, textView4, textView5;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    LocationManager locationManager;
    LocationListener locationListener;
    static final double NO_LATITUDE = -600;
    static final double NO_LONGITUDE = -600;

    double myLatitude = NO_LATITUDE;
    double myLongitude = NO_LATITUDE;
    double globalMinDistance = Double.POSITIVE_INFINITY;
    String closestMountain = "";
    double azimuth = 500;
    static final double EARTH_RADIUS = (6356.752 + 6378.137)/2;

    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private boolean mLocationPermissionGranted = false;

    // Sensor Variables
    SensorManager sensorManager;
    Sensor accelerometer, magneticField, gravity;

    // Orientation things
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    // for gravity sensor
    float[] mOrientation = new float[3];
    float[] rMat = new float[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!mLocationPermissionGranted){
            getLocationPermission();
        }
        //checkMapServices();

        btLocation = findViewById(R.id.bt_location);
        textView1 = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        textView5 = findViewById(R.id.textView5);

        btLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkMapServices();

                Calendar calendar = Calendar.getInstance();
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                Log.d(TAG, "onClick: Time" + minute + "\t" + second);

                ActivityCompat.requestPermissions(MainActivity.this
                        , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                if (ActivityCompat.checkSelfPermission(MainActivity.this
                        , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    boolean foundMountain = retrieveMountains();
                    if (myLatitude == NO_LATITUDE || myLongitude == NO_LONGITUDE) {
                        // Set latitude on Text View
                        String text1 = "Can't detect your location.";
                        textView1.setText(text1);
                        textView2.setText("");
                        textView3.setText("");
                        textView4.setText("");
                        String text5 = String.format(Locale.US, "Azimuth %1.3f", azimuth);
                        textView5.setText(text5);
                    }
                    else {
                        String text1 = "My Latitude: " + myLatitude;
                        String text2 = "My Longitude: " + myLongitude;
                        String text3 = "";
                        String text4 = "";
                        String text5 = String.format(Locale.US, "Azimuth %1.3f", azimuth);

                        // Set latitude on Text View
                        textView1.setText(text1);
                        textView2.setText(text2);
                        textView3.setText(text3);
                        textView4.setText(text4);
                        textView5.setText(text5);

                        if (foundMountain) {
                            setLocation();
                        }
                        else {
                            textView3.setText("No mountains over here");
                            textView4.setText(text4);
                        }
                    }
                    Log.d(TAG, "onClick: azimuth " + azimuth);
                }
                else{
                    Log.d(TAG, "onClick: no permission");
                }
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                myLatitude = location.getLatitude();
                myLongitude = location.getLongitude();

                String text1 = "Latitude: " + myLatitude;
                String text2 = "Longitude: " + myLongitude;

                Log.d(TAG, "onComplete: " + text1 + " " + text2);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }


            @Override
            public void onProviderDisabled(String provider) {
                /*
                Toast.makeText(MainActivity.this, "Disabled", Toast.LENGTH_SHORT).show();
                mLocationPermissionGranted = false;
                if (checkMapServices()) {
                    if (mLocationPermissionGranted) {
                        Log.d(TAG, "onProviderDisabled: permission grantedddd");
                    } else {
                        getLocationPermission();
                    }
                }*/
            }
        };

        // get an instance of the default rotation vector sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_UI);
        }
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_UI);
        }

        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (gravity != null) {
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }

        // Vivek's location manager code
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,  locationListener);
            Log.d(TAG, "onCreate: provider enabled");
        }
        else if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
            Log.d(TAG, "onCreate: provider enabled");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
        // locationManager.removeUpdates(locationListener);
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
        updateOrientationAngles();
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        azimuth =  orientationAngles[0];
        // "orientationAngles" now has up-to-date information.
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private String getCollectionName(String latitudeStr, String longitudeStr) {
        double latitude = Double.parseDouble(latitudeStr);
        double longitude = Double.parseDouble(longitudeStr);

        String collection = "zone1";
        return collection;
    }
    private void setLocation() {
        String text1 = "My Latitude: " + myLatitude;
        String text2 = "My Longitude: " + myLongitude;
        String text3 = "Closest Mountain: " + closestMountain;
        String text4 = String.format(Locale.US, "%1.3f km away", globalMinDistance);
        String text5 = String.format(Locale.US, "Azimuth %1.3f", azimuth);

        // Set latitude on Text View
        textView1.setText(text1);
        textView2.setText(text2);
        textView3.setText(text3);
        textView4.setText(text4);
        textView5.setText(text5);
    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( MainActivity.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You suck", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // Vivek
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        // Code modified from: https://towardsdatascience.com/heres-how-to-calculate-distance-between-2-geolocations-in-python-93ecab5bbba4
        // Source: https://en.wikipedia.org/wiki/Haversine_formula
        double r = 6371;
        double phi1 = degreeToRad(lat1);
        double phi2 = degreeToRad(lat2);
        double delta_phi = degreeToRad(lat2 - lat1);
        double delta_lambda = degreeToRad(lon2 - lon1);
        double a = Math.pow( Math.sin(delta_phi / 2), 2) + Math.cos(phi1) * Math.cos(phi2) *  Math.pow( Math.sin(delta_lambda / 2), 2);
        double res = r * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        return res;
    }

    private static double degreeToRad(double degree){
        return degree*Math.PI / 180.0;
    }


    public static double getMountainAngle(double myLatitude, double myLongitude, double mountainLatitude, double mountainLongitude){
        double northLatitude = 90;
        double northLongitude = myLongitude;

        // Distance to mountain
        double c = haversine(myLatitude, myLongitude, mountainLatitude, mountainLongitude) / EARTH_RADIUS;

        // Distance to north pole
        double b = haversine(myLatitude, myLongitude, northLatitude, northLongitude) / EARTH_RADIUS;

        // Distance from north pole to mountain
        double a = haversine(northLatitude, northLongitude, mountainLatitude, mountainLongitude) / EARTH_RADIUS;

        Log.d(TAG, "getMountainAngle: dist to mountain " + c + " , dist to np "  + b + ", dist to me " + a);
        double mountainAngle = Math.acos( (Math.cos(a) - Math.cos(b)*Math.cos(c) )/(Math.sin(b)*Math.sin(c)));

        // TODO make sure all 4 cases are covered
        if ( (myLongitude - mountainLongitude >= -180)
                && (myLongitude < mountainLongitude) ) {

            // Mountain is on the right of user

        } else if ( (myLongitude - mountainLongitude <= -180)
                && (myLongitude < mountainLongitude) ) {

            // Mountain is on the left of user
            mountainAngle = -mountainAngle;

        } else if ( (myLongitude - mountainLongitude >= 180)
                && (myLongitude > mountainLongitude) ) {

            // Mountain is on the right of user

        } else if ( ( myLongitude - mountainLongitude <= 180)
                && (myLongitude > mountainLongitude) ) {

            // Mountain is on the left of user
            mountainAngle = -mountainAngle;
        }
        Log.d(TAG, "getMountainAngle: mountain angle " + mountainAngle + " degrees");
        return mountainAngle;
    }

    public static double getAngleToMe(double mountainAngle, double azimuth){
        double angle = mountainAngle - azimuth;
        Log.d(TAG, "getAngleToMe: " + angle + " degrees");
        return angle;
    }

    public boolean retrieveMountains(){
        // retrieve mountains
        String collection = getCollectionName("" + myLatitude, "" + myLongitude);

        db.collection(collection).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    MountainInfo info = getClosestMountain(task, myLatitude, myLongitude, azimuth);
                    closestMountain = info.name;
                    globalMinDistance = info.distance;
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }

            }
        });

        if (closestMountain == "" || globalMinDistance == Double.POSITIVE_INFINITY){
            return false;
        }
        return true;
    }

    public static MountainInfo getClosestMountain(Task<QuerySnapshot> task, double myLatitude, double myLongitude, double azimuth) {
        double minDistance = Double.POSITIVE_INFINITY;
        String mountainName = "";

        for (QueryDocumentSnapshot document : task.getResult()) {
            Map<String, Object> doc = document.getData();
            Log.d(TAG, document.getId() + " => " + document.getData());

            double mountainLatitude = -600;
            double mountainLongitude = -600;
            double haversineDistance = Double.POSITIVE_INFINITY;

            GeoPoint latLong = document.getGeoPoint("coordinate");

            if (latLong != null) {
                mountainLatitude = latLong.getLatitude();
                mountainLongitude = latLong.getLongitude();
                haversineDistance = haversine(myLatitude, myLongitude, mountainLatitude, mountainLongitude);

                double mountainAngle = getMountainAngle(myLatitude, myLongitude, mountainLatitude,mountainLongitude);
                double angle = Math.abs( mountainAngle - azimuth );
                Log.d(TAG, "onComplete: angle " + angle);

                if (angle <= 20*Math.PI/180) {
                    if (haversineDistance < minDistance) {
                        minDistance = haversineDistance;
                        mountainName = document.getId();
                    }
                }
            }
            Log.d(TAG, "onComplete: distance " + haversineDistance);
        }

        MountainInfo info = new MountainInfo(mountainName,minDistance);
        info.distance = minDistance;
        info.name = mountainName;
        return info;
    }
}
