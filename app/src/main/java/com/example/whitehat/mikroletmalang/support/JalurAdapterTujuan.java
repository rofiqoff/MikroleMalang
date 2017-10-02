package com.example.whitehat.mikroletmalang.support;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.example.whitehat.mikroletmalang.R;
import com.example.whitehat.mikroletmalang.entity.model.JalurAngkot;

import java.util.ArrayList;

/**
 * Created by rofiqoff on 8/24/17.
 */

public class JalurAdapterTujuan extends ArrayAdapter<JalurAngkot.Jalur> {

    private Context context;
    private int resource, textViewResourceId1, textViewResourceId2;
    public ArrayList<JalurAngkot.Jalur> address, tempAddress, suggestions;

    TextView textAlamat, textKoordinat;

    public String nodeJalur, lat, lng, namaJalan;

    public JalurAdapterTujuan(Context context, int resource, int textViewResourceId1, int textViewResourceId2, ArrayList<JalurAngkot.Jalur> address) {
        super(context, android.R.layout.simple_list_item_1, address);
        this.context = context;
        this.resource = resource;
        this.textViewResourceId1 = textViewResourceId1;
        this.textViewResourceId2 = textViewResourceId2;
        this.address = address;
        this.tempAddress = new ArrayList<>(address);
        this.suggestions= new ArrayList<>();
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_row_alamat, parent, false);
        }

        JalurAngkot.Jalur jalurAngkot = address.get(position);
        if (jalurAngkot != null){
            textAlamat = (TextView) view.findViewById(R.id.tv_alamat);
            textKoordinat = (TextView) view.findViewById(R.id.tv_koordinat);
            if (textAlamat != null && textKoordinat != null){

                nodeJalur = jalurAngkot.getNode_jalur();
                namaJalan = jalurAngkot.getNama_jalan();
                lat = jalurAngkot.getLat();
                lng = jalurAngkot.getLng();

                textAlamat.setText(namaJalan);
                textKoordinat.setText(lat +", "+ lng);

            }
        }

        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
//            JalurAngkotModel.JalurAngkot jalurAngkot = (JalurAngkotModel.JalurAngkot) resultValue;
//            return jalurAngkot.getNode_jalur();
            String namaJalan = ((JalurAngkot.Jalur)resultValue).getNama_jalan();
            return namaJalan;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null){
                suggestions.clear();
                for (JalurAngkot.Jalur jalurAngkot : tempAddress){
                    if (jalurAngkot.getNama_jalan().toLowerCase().contains(constraint.toString().toLowerCase())){
                        suggestions.add(jalurAngkot);
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<JalurAngkot.Jalur> filterList = (ArrayList<JalurAngkot.Jalur>) results.values;
            if (results != null && results.count > 0){
                clear();
                for (JalurAngkot.Jalur jalurAngkot : filterList){
                    add(jalurAngkot);
                    notifyDataSetChanged();
                }
            }
        }
    };
}
