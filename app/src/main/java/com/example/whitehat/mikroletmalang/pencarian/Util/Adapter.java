package com.example.whitehat.mikroletmalang.pencarian.Util;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.whitehat.mikroletmalang.R;
import com.example.whitehat.mikroletmalang.angkot.database.APIService;
import com.example.whitehat.mikroletmalang.angkot.database.model.JalurAngkot;

import java.util.ArrayList;

/**
 * Created by rofiqoff on 9/21/17.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> implements Filterable {

    private static final String TAG = "AdapterLOG";
    Activity activity;
    ArrayList<JalurAngkot.Jalur> resultList;

    APIService apiService;

    public Adapter(Activity activity, ArrayList<JalurAngkot.Jalur> jalurs, APIService apiService) {
        this.activity = activity;
        this.resultList = jalurs;

        this.apiService = apiService;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_row_alamat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String lat = resultList.get(position).getLat();
        String lng = resultList.get(position).getLng();

        Log.d(TAG, "Koordinat : " + lat + ", "+ lng);
        Log.d(TAG, "Alamat : " + resultList.get(position).getNama_jalan());

        holder.textAlamat.setText(resultList.get(position).getNama_jalan());
        holder.textKoordinat.setText(lat +", "+ lng);
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textAlamat;
        TextView textKoordinat;

        public ViewHolder(View itemView) {
            super(itemView);
            textAlamat = (TextView) itemView.findViewById(R.id.tv_alamat);
            textKoordinat = (TextView) itemView.findViewById(R.id.tv_koordinat);
        }
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList queryResults = new ArrayList<>();
//                if (constraint != null && constraint.length() > 0) {
//                    queryResults = autocomplete();
//                }

                filterResults.values = queryResults;
                filterResults.count = queryResults.size();

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, final FilterResults results) {

                resultList = (ArrayList<JalurAngkot.Jalur>) results.values;
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetChanged();
                }
            }
        };

        return filter;
    }

//    public ArrayList autocomplete() {
//
//        final Call<JalurAngkot> jalur = apiService.jalur();
//        jalur.enqueue(new Callback<JalurAngkot>() {
//            @Override
//            public void onResponse(Call<JalurAngkot> call, Response<JalurAngkot> response) {
//                if (response.isSuccessful()) {
//                    resultList = new ArrayList<JalurAngkot.Jalur>(response.body().getJalur());
//
//                    JalurAngkot jalurAngkot = response.body();
//
//                    Log.d(TAG, "Response Successful " + jalurAngkot.toString());
//                    Log.d(TAG, "Response Code " + response.code());
//
//                } else {
//                    Log.d(TAG, "Response Not Successful " + response.body().toString());
//                    Log.d(TAG, "Response Code " + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<JalurAngkot> call, Throwable t) {
//                Log.e(TAG, "Kesalahan jaringan : " + t.getMessage());
//            }
//        });
//
//        return resultList;
//    }
}
