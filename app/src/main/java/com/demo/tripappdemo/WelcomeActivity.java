package com.demo.tripappdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.demo.tripappdemo.Helper.LocaleHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

import io.paperdb.Paper;

public class WelcomeActivity extends AppCompatActivity {
    Button get_Location , go_btn;
    TextView set_Location;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;
    List<Address> addresses;
    double latitude , longitude;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase , "en"));
    } // end attachBaseContext()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        get_Location = findViewById(R.id.Get_Location_Btn);
        set_Location = findViewById(R.id.My_Address);
        go_btn = findViewById(R.id.Start_Btn);
        get_Location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastLocation();

            } // end onClick()
        }); // end setOnClickListener()
        go_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBtn();
            } // end onClick()
        }); // end setOnClickListener()

        Paper.init(this);
        String language = Paper.book().read("language");
        if (language == null)
            Paper.book().write("language" , "en");
        updateView((String)Paper.book().read("language"));
    } // end onCreate()

    private void updateView(String language) {
        Context context = LocaleHelper.setLocale(this , language);
        Resources resources = context.getResources();
    } // end updateView()

    public void goBtn(){
        AlertDialog.Builder GoDialog = new AlertDialog.Builder(WelcomeActivity.this);
        GoDialog.setTitle(R.string.go_dialog_title)
                .setMessage(R.string.go_dialog_message)
                .setPositiveButton(R.string.dialoge_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = getSharedPreferences("Home Location" , MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("Home Location" , set_Location.getText().toString());
                        editor.apply();
                        Intent yesclick = new Intent(WelcomeActivity.this , TripInformationActivity.class);
                        startActivity(yesclick);
                    } // end onClick()
                }) // end setPositiveButton()
                .setNegativeButton(R.string.dialoge_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = getSharedPreferences("Home Location" , MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("Home Location" , " ");
                        editor.apply();
                        Intent noclick = new Intent(WelcomeActivity.this , TripInformationActivity.class);
                        startActivity(noclick);
                    } // end onClick()
                }); // end setNegativeButton()
        AlertDialog builder = GoDialog.create();
        GoDialog.show();

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            try {
                                Geocoder geocoder = new Geocoder(WelcomeActivity.this);
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                addresses = geocoder.getFromLocation(latitude , longitude , 1);
                                set_Location.setText(addresses.get(0).getCountryName());

                            } // end try()
                            catch (IOException e) {
                                e.printStackTrace();
                            } // end catch()

                        } // end else
                    } // end onComplete()
                }); // end addOnCompleteListener()
            } // end inner if()
            else
            {
                AlertDialog.Builder GpsDialog = new AlertDialog.Builder(this);
                GpsDialog.setTitle(R.string.gps_dialog_title)
                        .setMessage(R.string.gps_dialog_message)
                        .setPositiveButton(R.string.dialoge_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            } // end onClick()
                        }) // end setPositiveButton()
                        .setNegativeButton(R.string.dialoge_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            } // end onClick()
                        }); // end setNegativeButton()
                AlertDialog builder = GpsDialog.create();
                GpsDialog.show();
            } // end else
        }  // end outer If()
        else
        {
            requestPermissions();
        } // end else
    } // end getLastLocation()

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    } // end requestNewLocationData()

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            try {
                Location mLastLocation = locationResult.getLastLocation();
                Geocoder geocoder = new Geocoder(WelcomeActivity.this);
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                addresses = geocoder.getFromLocation(latitude , longitude , 1);
            } // end try
            catch (IOException e) {
                e.printStackTrace();
            } // end catch()
            set_Location.setText(addresses.get(0).getCountryName());
        } // end onLocationResult()
    }; // end mLocationCallback()

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } // end if()
        return false;
    } // end checkPermissions()

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    } // end requestPermissions()

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    } // end isLocationEnabled()

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } // end inner if()
        } // end outer if ()
    } // end onRequestPermissionsResult()
} // end class
