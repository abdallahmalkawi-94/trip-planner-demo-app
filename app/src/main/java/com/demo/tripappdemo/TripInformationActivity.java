package com.demo.tripappdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.tripappdemo.Helper.LocaleHelper;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import io.paperdb.Paper;

public class TripInformationActivity extends AppCompatActivity implements OnMapReadyCallback {
    ImageView imgView;
    TextView destinationselect;
    DatePickerDialog picker;
    EditText fromdateddit,todateedit;
    final Calendar myCalendar = Calendar.getInstance();
    int [] imagearray = {R.drawable.cover , R.drawable.cover1 , R.drawable.cover2 , R.drawable.cover3, R.drawable.cover4
            , R.drawable.cover5, R.drawable.cover6, R.drawable.cover8, R.drawable.cover9, R.drawable.cover10, R.drawable.cover11
            , R.drawable.cover12, R.drawable.cover13, R.drawable.cover14, R.drawable.cover15, R.drawable.cover16, R.drawable.cover17
            , R.drawable.cover18, R.drawable.cover19, R.drawable.cover20, R.drawable.cover21, R.drawable.cover22};
    Handler handler;
    int i = 0;
    int AUTOCOMPLETE_REQUEST_CODE = 1;
    PlacesClient placesClient;
    private GoogleMap mMap;
    LatLng  destination ;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase , "en"));
    } // end attachBaseContext()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_information_screen);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        imgView = findViewById(R.id.imageView);
        destinationselect = findViewById(R.id.Destination_Text);
        destinationselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME , Place.Field.LAT_LNG);
                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(TripInformationActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            } // end onClick()
        }); // end setOnClickListener()

        fromdateddit= findViewById(R.id.FromDateEdit);
        fromdateddit.setInputType(InputType.TYPE_NULL);

        todateedit= findViewById(R.id.ToDateEdit);
        todateedit.setInputType(InputType.TYPE_NULL);

        fromdateddit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCalander(0);
            } // end onClick()
        }); // end setOnClickListener()

        todateedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCalander(1);
            }// end onClick()
        }); // end setOnClickListener()

        Random random = new Random();
        int x;
        x = random.nextInt(22);
        imgView.setImageResource(imagearray[x]);
        handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                imgView.setImageResource(imagearray[i]);
                i++;
                if (i > imagearray.length - 1) {
                    i = 0;
                } // end if
                imgView.setImageResource(imagearray[i]);
                handler.postDelayed(this, 30000);
            } // end run()
        }; // end Runnable()
        handler.postDelayed(runnable, 30000);
        placeInit();

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(destination));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(destination));
    } // end onMapReady()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                destinationselect.setText(place.getName());
                destination = place.getLatLng();
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                destinationselect.setText("Not Selected Yet");
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            } // end else if()
        } // end if(requestCode)
    } // end onActivityResult()

    private void placeInit() {
        // Initialize the SDK
        if(!Places.isInitialized()){
            Places.initialize(TripInformationActivity.this, getString(R.string.places_api_key));
        } // end if()
        // Create a new Places client instance
        placesClient = Places.createClient(TripInformationActivity.this);
    } // end placeInit()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.trip_info_menu , menu);
        return true;
    } // end onCreateOptionsMenu()

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.YourTripItem:
                return true;
            case R.id.SettingsItem:
                Intent settingclick = new Intent(TripInformationActivity.this , SettingsActivity.class);
                startActivity(settingclick);
                return true;
            case R.id.HelpItem:
                return true;
            default: return super.onOptionsItemSelected(item);
        } // end switch()

    } // end onOptionsItemSelected()

    public void getCalander(final int Option){
        final EditText [] dateid =  {fromdateddit,todateedit};
        final Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        // date picker dialog
        picker = new DatePickerDialog(TripInformationActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dateid[Option].setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
            } // end onDateSet()
        }, year, month, day);
        picker.show();
    } // end getCalander()
} // end class