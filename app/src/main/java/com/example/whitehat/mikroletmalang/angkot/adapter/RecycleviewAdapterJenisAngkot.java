package com.example.whitehat.mikroletmalang.angkot.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bumptech.glide.Glide;
import com.example.whitehat.mikroletmalang.R;
import com.example.whitehat.mikroletmalang.angkot.database.Helper;
import com.example.whitehat.mikroletmalang.angkot.database.ModelJenisAngkot;
import com.example.whitehat.mikroletmalang.angkot.detail.DetailJenis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by whitehat on 04/05/17.
 */

public class RecycleviewAdapterJenisAngkot extends RecyclerView.Adapter<RecycleviewAdapterJenisAngkot.ViewHolder> {

    private Activity activity;
    private List<ModelJenisAngkot> modelJenisAngkots;

    private String sNodeJenisAwal;
    private String sLatAwal;
    private String sLongAwal;
    private String sNodeJalurAkhir;
    private String sLatAkhir;
    private String sLongAkhir;
    private String sNodeJalur;
    private String sLatJalur;
    private String sLongJalur;

    private String sTrak;

    private AQuery aQuery;

    public RecycleviewAdapterJenisAngkot(Activity activity, List<ModelJenisAngkot> modelJenisAngkots) {
        this.activity = activity;
        this.modelJenisAngkots = modelJenisAngkots;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.item_jenis_angkot,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
//        holder.imageAngkot.setImageResource(modelJenisAngkots.get(position).getImageangkot());
        holder.namaMikrolet1.setText(modelJenisAngkots.get(position).getNamaAngkot1());
        holder.namaMikrolet2.setText(modelJenisAngkots.get(position).getNamaAngkot2());

        Glide.with(activity)
                .load(Helper.BASE_URL + modelJenisAngkots.get(position).getImageangkot())
                .crossFade()
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imageAngkot);

        holder.container.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, DetailJenis.class);

                        String sGambar = modelJenisAngkots.get(position).getImageangkot();
                        String sNamaMikrolet1 = modelJenisAngkots.get(position).getNamaAngkot1();
                        String sNamaMikrolet2 = modelJenisAngkots.get(position).getNamaAngkot2();
                        String getNama = (String) holder.namaMikrolet1.getText();
                        int dataRaw = 0;

                        if (getNama.equalsIgnoreCase("AL")){
                            DataAL();
                            sTrak = activity.getString(R.string.trayek_al);
                            dataRaw = R.raw.angkutanal;
                        } else if (getNama.equalsIgnoreCase("AG")) {
                            DataAG();
                            sTrak = activity.getString(R.string.trayek_ag);
                            dataRaw = R.raw.angkutanag;
                        } else if (getNama.equalsIgnoreCase("LG")) {
                            DataLG();
                            sTrak = activity.getString(R.string.trayek_lg);
                            dataRaw = R.raw.angkutanlg;
                        }

                        intent.putExtra("gambar", sGambar);
                        intent.putExtra("nama1", sNamaMikrolet1);
                        intent.putExtra("nama2", sNamaMikrolet2);
                        intent.putExtra("nodeJalur", sNodeJalur);
                        intent.putExtra("latJalur", sLatJalur);
                        intent.putExtra("longJalur", sLongJalur);
                        intent.putExtra("trayek", sTrak);
                        intent.putExtra("peta", dataRaw);

                        activity.startActivity(intent);

                    }
                }
        );

    }

    @Override
    public int getItemCount() {
        return (null != modelJenisAngkots ? modelJenisAngkots.size() : 0);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageAngkot;
        private TextView namaMikrolet1;
        private TextView namaMikrolet2;
        private View container;

        public ViewHolder(View itemView) {
            super(itemView);
            imageAngkot = (ImageView) itemView.findViewById(R.id.imageJenisAngkot);
            namaMikrolet1 = (TextView) itemView.findViewById(R.id.textJenisAngkot);
            namaMikrolet2 = (TextView) itemView.findViewById(R.id.textJenisAngkot2);
            container = itemView.findViewById(R.id.cardviewJenisAngkot);
        }
    }

    private void DataAL(){
        String url = Helper.BASE_URL + "tampil-data-al.php";
        Map<String, String> param = new HashMap();

        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setInverseBackgroundForced(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("loading . . .");
        progressDialog.show();

        aQuery = new AQuery(activity);
        try {
            aQuery.progress(progressDialog).ajax(url, param, String.class, new AjaxCallback<String>(){
                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (object != null){
                        try {
                            JSONObject jsonObject = new JSONObject(object);
                            String result = jsonObject.getString("result");
                            String msg = jsonObject.getString("msg");

                            if (result.equalsIgnoreCase("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("mikrolet_al");
                                for (int a = 0; a < jsonArray.length(); a++){
                                    JSONObject object1 = jsonArray.getJSONObject(a);

                                    sNodeJalur = object1.getString("node_jalur");
                                    sLatJalur = object1.getString("lat_jalur");
                                    sLongAwal = object1.getString("long_jalur");

                                }
                            } else {
                                Toast.makeText(activity, "Gagal Ambil data", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void DataAG(){
        String url = Helper.BASE_URL + "tampil-data-ag.php";
        Map<String, String> param = new HashMap();

        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setInverseBackgroundForced(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("loading . . .");
        progressDialog.show();

        aQuery = new AQuery(activity);
        try {
            aQuery.progress(progressDialog).ajax(url, param, String.class, new AjaxCallback<String>(){
                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (object != null){
                        try {
                            JSONObject jsonObject = new JSONObject(object);
                            String result = jsonObject.getString("result");
                            String msg = jsonObject.getString("msg");

                            if (result.equalsIgnoreCase("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("mikrolet_ag");
                                for (int a = 0; a < jsonArray.length(); a++){
                                    JSONObject object1 = jsonArray.getJSONObject(a);

                                    sNodeJalur = object1.getString("node_jalur");
                                    sLatJalur = object1.getString("lat_jalur");
                                    sLongAwal = object1.getString("long_jalur");

                                }
                            } else {
                                Toast.makeText(activity, "Gagal Ambil data", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void DataLG(){
        String url = Helper.BASE_URL + "tampil-data-lg.php";
        Map<String, String> param = new HashMap();

        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setInverseBackgroundForced(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("loading . . .");
        progressDialog.show();

        aQuery = new AQuery(activity);
        try {
            aQuery.progress(progressDialog).ajax(url, param, String.class, new AjaxCallback<String>(){
                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (object != null){
                        try {
                            JSONObject jsonObject = new JSONObject(object);
                            String result = jsonObject.getString("result");
                            String msg = jsonObject.getString("msg");

                            if (result.equalsIgnoreCase("true")){
                                JSONArray jsonArray = jsonObject.getJSONArray("mikrolet_lg");
                                for (int a = 0; a < jsonArray.length(); a++){
                                    JSONObject object1 = jsonArray.getJSONObject(a);

                                    sNodeJalur = object1.getString("node_jalur");
                                    sLatJalur = object1.getString("lat_jalur");
                                    sLongAwal = object1.getString("long_jalur");

                                }
                            } else {
                                Toast.makeText(activity, "Gagal Ambil data", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
