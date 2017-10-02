package com.example.whitehat.mikroletmalang.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.whitehat.mikroletmalang.R;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreen.this,MainActivity.class));
                finish();
            }

        }
        ,2000
        );
    }
}
