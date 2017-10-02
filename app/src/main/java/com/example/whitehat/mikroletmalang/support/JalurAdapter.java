package com.example.whitehat.mikroletmalang.support;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.whitehat.mikroletmalang.entity.model.JalurAngkot;

import java.util.ArrayList;

/**
 * Created by rofiqoff on 9/21/17.
 */

public class JalurAdapter extends ArrayAdapter<JalurAngkot.Jalur> {

    private Context context;
    private int resource, textViewResourceId1, textViewResourceId2;
    private ArrayList<JalurAngkot.Jalur> address, tempAddress, suggestions;

    TextView textAlamat, textKoordinat;

    public String nodeJalur, lat, lng, namaJalan;

    public JalurAdapter(Context context, int resource, int textViewResourceId1, int textViewResourceId2, ArrayList<JalurAngkot.Jalur> address) {
        super(context, android.R.layout.simple_list_item_1, address);
        this.context = context;
        this.resource = resource;
        this.textViewResourceId1 = textViewResourceId1;
        this.textViewResourceId2 = textViewResourceId2;
        this.address = address;
        this.tempAddress = new ArrayList<>(address);
        this.suggestions= new ArrayList<>();
    }
}
