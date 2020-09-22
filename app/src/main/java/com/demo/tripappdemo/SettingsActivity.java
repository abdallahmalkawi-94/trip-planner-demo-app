package com.demo.tripappdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.tripappdemo.Helper.LocaleHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import io.paperdb.Paper;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    Button en_btn , ar_btn , save_btn , cancel_btn , rest_btn;
    TextView saved_home_location;

    Spinner countresspinner;
    ArrayList<String> countres;
    ArrayAdapter <String> adapter;
    String countyname = null;
    CheckBox cb;
    int spinnerPosition;
    boolean clickcb = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase , "en"));
    } // end attachBaseContext()

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen);
        String CountryName = getString(R.string.not_set_yet);
        saved_home_location = findViewById(R.id.Home_Location_saved);
        SharedPreferences sharedPreferences = getSharedPreferences("Home Location" , MODE_PRIVATE);
        if (sharedPreferences.getString("Home Location" , CountryName).length() == 1)
            saved_home_location.setText(getString(R.string.current_location)+" "+CountryName);
        else
            saved_home_location.setText(getString(R.string.current_location)+" "+sharedPreferences.getString("Home Location" , ""));

        en_btn = findViewById(R.id.ENBtn);
        en_btn.setOnClickListener(this);

        ar_btn = findViewById(R.id.ARBtn);
        ar_btn.setOnClickListener(this);

        save_btn = findViewById(R.id.SaveBtn);
        save_btn.setOnClickListener(this);

        cancel_btn = findViewById(R.id.CancelBtn);
        cancel_btn.setOnClickListener(this);

        cb = findViewById(R.id.checkBox);
        cb.setOnClickListener(this);

        rest_btn = findViewById(R.id.RestBtn);
        rest_btn.setOnClickListener(this);

        countresspinner = findViewById(R.id.CountresSpin);
        countresspinner.setEnabled(false);
        Locale[] locale = Locale.getAvailableLocales();
        countres = new ArrayList<>();
        for (Locale lc : locale)
        {
            countyname = lc.getDisplayCountry();
            if (countyname.length() > 0 && !countres.contains(countyname))
                countres.add(countyname);
        } // end for ()
        Collections.sort(countres , String.CASE_INSENSITIVE_ORDER);
        adapter = new ArrayAdapter (this , android.R.layout.simple_spinner_item , countres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countresspinner.setAdapter(adapter);
        countresspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerPosition = position;
            } // end onItemSelected()

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            } // end onNothingSelected()
        }); // end setOnItemSelectedListener()

        Paper.init(this);
        String language = Paper.book().read("language");
        if (language == null)
            Paper.book().write("language" , "en");
        updateView((String)Paper.book().read("language"));

    } // end onCreate()

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.ENBtn:
                Paper.book().write("language" , "en");
                updateView((String)Paper.book().read("language"));
                break;
            case R.id.ARBtn:
                Paper.book().write("language" , "ar");
                updateView((String)Paper.book().read("language"));
                break;

            case R.id.checkBox:
                if (cb.isChecked())
                {
                    clickcb = true;
                    countresspinner.setEnabled(true);
                } // end if()
                else
                {
                    clickcb = false;
                    countresspinner.setEnabled(false);
                } // end else
                break;

            case R.id.SaveBtn:
                if (clickcb)
                {
                    updateHomeLocation();;
                } // end if()
                Intent saveclick = new Intent(this , SplachActivity.class);
                startActivity(saveclick);
                break;
            case R.id.CancelBtn:
                Intent cancelclick = new Intent(getApplicationContext() , TripInformationActivity.class);
                startActivity(cancelclick);
                break;

            case R.id.RestBtn:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.rest_dialog_massage);

                builder.setPositiveButton(R.string.dialoge_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Paper.book().write("language" , "en");
                        updateView((String)Paper.book().read("language"));
                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("Home Location" , Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("Home Location" , "");
                        editor.commit();
                        Intent restclick = new Intent(getApplicationContext() , SplachActivity.class);
                        startActivity(restclick);

                    } // end onClick()
                }); // end setPositiveButton()

                builder.setNegativeButton(R.string.dialoge_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent restclick = new Intent(getApplicationContext() , SettingsActivity.class);
                        startActivity(restclick);
                    } // end onClick()
                }); // end setNegativeButton()
                AlertDialog alertDialog = builder.create();
                builder.show();
                break;

        } // end switch()
    } // end onClick()

    private void updateView(String language) {
        Context context = LocaleHelper.setLocale(this , language);
        Resources resources = context.getResources();
    } // end updateView()

    public void updateHomeLocation()
    {
        SharedPreferences sharedPreferences = this.getSharedPreferences("Home Location" , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Home Location" , countres.get(spinnerPosition));
        editor.commit();
    } // end setNewLocation()

} // end class
