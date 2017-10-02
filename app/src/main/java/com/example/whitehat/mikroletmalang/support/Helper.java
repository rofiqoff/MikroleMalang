package com.example.whitehat.mikroletmalang.support;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by whitehat on 18/05/17.
 */

public class Helper extends BaseApp {
    public static String BASE_URL = "http://mikroletmalang.000webhostapp.com/";

    public static void pesan (Context c, String msg){
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

}