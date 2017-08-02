package com.example.whitehat.mikroletmalang;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.example.whitehat.mikroletmalang.about.AboutApp;
import com.example.whitehat.mikroletmalang.angkot.JenisAngkot;
import com.example.whitehat.mikroletmalang.help.Help;
import com.example.whitehat.mikroletmalang.pencarian.PencarianRute;

public class MainActivity extends AppCompatActivity {

    private ImageView imageAngkot, imageSearch, imageHelp, imageAbout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Mendeklarasikan Id
        toolbar = (Toolbar) findViewById(R.id.include);
        imageAngkot = (ImageView) findViewById(R.id.image_angkot);
        imageSearch = (ImageView) findViewById(R.id.image_search);
        imageHelp = (ImageView) findViewById(R.id.image_help);
        imageAbout = (ImageView) findViewById(R.id.image_about);

        //Memberikan action click pada setiap tombol
        imageAngkot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, JenisAngkot.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        imageSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PencarianRute.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        imageHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Help.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        imageAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AboutApp.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });


    }
}
