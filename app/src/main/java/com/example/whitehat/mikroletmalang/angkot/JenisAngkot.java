package com.example.whitehat.mikroletmalang.angkot;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.example.whitehat.mikroletmalang.R;
import com.example.whitehat.mikroletmalang.angkot.adapter.RecycleviewAdapterJenisAngkot;
import com.example.whitehat.mikroletmalang.angkot.database.Helper;
import com.example.whitehat.mikroletmalang.angkot.database.ModelJenisAngkot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JenisAngkot extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecycleviewAdapterJenisAngkot adapter;
    private ArrayList<ModelJenisAngkot> model;

    private AQuery aQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jenis_angkot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycleview_angkot);
        model = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayout);
        adapter = new RecycleviewAdapterJenisAngkot(this, model);
        recyclerView.setAdapter(adapter);

        setdata();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setdata() {
        String url = Helper.BASE_URL + "tampil-jeni-mikrolet.php";
        Map<String, String> param = new HashMap();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setInverseBackgroundForced(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("loading . . .");
        progressDialog.show();

        aQuery = new AQuery(this);
        try {
            aQuery.progress(progressDialog).ajax(url, param, String.class, new AjaxCallback<String>() {
                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (object != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(object);
                            String result = jsonObject.getString("result");
                            String msg = jsonObject.getString("msg");

                            if (result.equalsIgnoreCase("true")) {
                                JSONArray jsonArray = jsonObject.getJSONArray("jenis_mikrolet");
                                for (int a = 0; a < jsonArray.length(); a++) {
                                    JSONObject object1 = jsonArray.getJSONObject(a);
                                    ModelJenisAngkot modelJenisAngkot = new ModelJenisAngkot();

                                    modelJenisAngkot.setNamaAngkot1(object1.getString("jenis_mikrolet"));
                                    modelJenisAngkot.setNamaAngkot2(object1.getString("tujuan_mikrolet"));
                                    modelJenisAngkot.setImageangkot(object1.getString("gambar_mikrolet"));

                                    model.add(modelJenisAngkot);
                                    adapter = new RecycleviewAdapterJenisAngkot(JenisAngkot.this, model);
                                    recyclerView.setAdapter((adapter));

                                }
                            } else {
                                Helper.pesan(JenisAngkot.this, msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
