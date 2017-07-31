package com.example.whitehat.mikroletmalang.pencarian;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.example.whitehat.mikroletmalang.R;

public class PencarianRute extends AppCompatActivity {

    private LinearLayout posisi_pengguna;
    private LinearLayout cari_angkot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pencarian_rute);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        posisi_pengguna = (LinearLayout) findViewById(R.id.posisi_pengguna);
        cari_angkot = (LinearLayout) findViewById(R.id.cari_angkot);

        posisi_pengguna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PencarianRute.this, PosisiPenggunaActivity.class));
            }
        });

        cari_angkot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PencarianRute.this, CariAngkotActivity.class));
            }
        });
    }

}