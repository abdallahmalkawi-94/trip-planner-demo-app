package com.demo.tripappdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.service.carrier.MessagePdu;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.tripappdemo.Helper.LocaleHelper;

import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;

public class SplachActivity extends Activity {
    Timer timer;
    String valuesaved;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase , "en"));
    } // end attachBaseContext()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        timer = new Timer();
        SharedPreferences sharedPreferences = getSharedPreferences("Home Location" , MODE_PRIVATE);
        if (sharedPreferences.getString("Home Location" , "").length() > 0)
        {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplachActivity.this , TripInformationActivity.class);
                    startActivity(intent);
                    finish();
                } // end run()
            },1000); // end schedule()
        } // end if ()
        else
        {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplachActivity.this , WelcomeActivity.class);
                    startActivity(intent);
                    finish();
                } // end run()
            },1000); // end schedule()
        } // end else


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

} // end class