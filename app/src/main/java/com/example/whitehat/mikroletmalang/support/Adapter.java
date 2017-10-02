package com.example.whitehat.mikroletmalang.support;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.whitehat.mikroletmalang.R;
import com.example.whitehat.mikroletmalang.service.APIService;
import com.example.whitehat.mikroletmalang.entity.model.JalurAngkot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Result;

import retrofit2.Callback;

/**
 * Created by rofiqoff on 9/21/17.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> implements Filterable {

    private static final String TAG = "AdapterLOG";
    Activity activity;
    ArrayList<JalurAngkot.Jalur> resultList;

    APIService apiService;

    Context context;
    List<JalurAngkot.Jalur> jalurResult;
    List<JalurAngkot.Jalur> filteredJalurList;

    private AdapterListFilter listFilter;

    List<JalurAngkot.Jalur> result;
    ArrayList<JalurAngkot.Jalur> arrayList;
    private List<String> mDefaultJalur;

    private List<JalurAngkot.Jalur> mData;
    private List<JalurAngkot.Jalur> originData;

    public Adapter(Context context, List<JalurAngkot.Jalur> jalurs) {
        this.jalurResult = jalurs;
        this.filteredJalurList = new ArrayList<>();
    }

    public Adapter(Callback<Result> callback, List<JalurAngkot.Jalur> result) {
        this.jalurResult = result;
        arrayList = new ArrayList<JalurAngkot.Jalur>();
        arrayList.addAll(jalurResult);
    }

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
        String lat = jalurResult.get(position).getLat();
        String lng = jalurResult.get(position).getLng();

        Log.d(TAG, "Koordinat : " + lat + ", "+ lng);
        Log.d(TAG, "Alamat : " + jalurResult.get(position).getNama_jalan());

        holder.textAlamat.setText(jalurResult.get(position).getNama_jalan());
        holder.textKoordinat.setText(lat +", "+ lng);
    }

    @Override
    public int getItemCount() {
        return jalurResult.size();
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
        if (listFilter == null) {
            listFilter = new AdapterListFilter(this, jalurResult);
        }
        return listFilter;
    }

    //    @Override
//    public Filter getFilter() {
//        Filter filter = new Filter() {
//            @Override
//            protected FilterResults performFiltering(CharSequence constraint) {
//                FilterResults filterResults = new FilterResults();
//                ArrayList queryResults = new ArrayList<>();
////                if (constraint != null && constraint.length() > 0) {
////                    queryResults = autocomplete();
////                }
//
//                filterResults.values = queryResults;
//                filterResults.count = queryResults.size();
//
//                return filterResults;
//            }
//
//            @Override
//            protected void publishResults(CharSequence constraint, final FilterResults results) {
//
//                resultList = (ArrayList<JalurAngkot.Jalur>) results.values;
//                if (results != null && results.count > 0) {
//                    notifyDataSetChanged();
//                } else {
//                    notifyDataSetChanged();
//                }
//            }
//        };
//
//        return filter;
//    }

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

    public class AdapterListFilter extends Filter {

        private final Adapter adapterList;
        private final List<JalurAngkot.Jalur> originalData;
        private List<JalurAngkot.Jalur> filterData;

        public AdapterListFilter(Adapter adapter, List<JalurAngkot.Jalur> originalData) {
            super();
            this.adapterList = adapter;
            this.originalData = new LinkedList<>(originalData);
            this.filterData = new ArrayList<>();
        }

        public AdapterListFilter(Adapter adapterList, List<JalurAngkot.Jalur> originalData, List<JalurAngkot.Jalur> filterData) {
            this.adapterList = adapterList;
            this.originalData = originalData;
            this.filterData = filterData;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            filterData.clear();
            final FilterResults results = new FilterResults();

            Log.d("performingFiltering : ", constraint.toString());

            if (TextUtils.isEmpty(constraint.toString())) {
                filterData.addAll(originalData);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();

                for (final JalurAngkot.Jalur jalur : originalData) {
                    if (jalur.getNama_jalan().toLowerCase().contains(filterPattern)) {
                        filterData.add(jalur);
                    }
                }
            }

            results.values = filterData;
            results.count = filterData.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapterList.filteredJalurList.clear();
            adapterList.filteredJalurList.addAll((ArrayList< JalurAngkot.Jalur>) results.values);
            adapterList.notifyDataSetChanged();
        }
    }
}
