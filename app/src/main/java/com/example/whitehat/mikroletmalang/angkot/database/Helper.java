package com.example.whitehat.mikroletmalang.angkot.database;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by whitehat on 18/05/17.
 */

public class Helper extends BaseApp {
    public static String BASE_URL = "http://mikroletmalang.pe.hu/";

    public static boolean isCompare(EditText et1, EditText et2){
        String a = et1.getText().toString();
        String b = et2.getText().toString();

        if (a.equals(b)){
            return false;
        } else {
            return true;
        }
    }

    public static void pesan (Context c, String msg){
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

    public static boolean isEmpaty(EditText editText){
        if (editText.getText().toString().trim().length() > 0){
            return false;
        } else {
            return true;
        }
    }

}