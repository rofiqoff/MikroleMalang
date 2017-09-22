package com.example.whitehat.mikroletmalang.angkot.database;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by whitehat on 18/05/17.
 */

public class Helper extends BaseApp {
    public static String BASE_URL = "http://mikroletmalang.pe.hu/";

    public static void pesan (Context c, String msg){
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

}