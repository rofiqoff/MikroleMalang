package com.example.whitehat.mikroletmalang.pencarian;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whitehat.mikroletmalang.R;
import com.example.whitehat.mikroletmalang.angkot.database.APIService;
import com.example.whitehat.mikroletmalang.angkot.database.model.AngkotAGModel;
import com.example.whitehat.mikroletmalang.angkot.database.model.AngkotALModel;
import com.example.whitehat.mikroletmalang.angkot.database.model.AngkotLGModel;
import com.example.whitehat.mikroletmalang.angkot.database.model.JalurAngkotModel;
import com.example.whitehat.mikroletmalang.pencarian.geolocation.GeocoderIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.kml.KmlLayer;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CariAngkotActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mGoogleMap;
    private KmlLayer kmlLayer;
    private APIService apiService;

    private EditText tujuanAwal;
    private EditText tujuanAkhir;
    private ImageView cariAngkot;
    private TextView textHasil;
    private ProgressBar progressBar;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private int PLACE_PICKER_REQUEST = 1;
    private GeocoderIntentService geocoderReceiver;



    ArrayList<AngkotALModel.MikroletAl> al = new ArrayList<>();
    ArrayList<AngkotAGModel.MikroletAG> ag = new ArrayList<>();
    ArrayList<AngkotLGModel.MikroletLG> lg = new ArrayList<>();
    ArrayList<JalurAngkotModel.JalurAngkot> jalur = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_angkot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tujuanAwal = (EditText) findViewById(R.id.edit_text_posisi_awal);
        tujuanAkhir = (EditText) findViewById(R.id.edit_text_posisi_akhir);
        cariAngkot = (ImageView) findViewById(R.id.cari_angkot_tujuan);
        textHasil = (TextView) findViewById(R.id.hasil_tujuan_angkot);
        progressBar = (ProgressBar) findViewById(R.id.pbLoading_cari_angkot);

        cariAngkot.setOnClickListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_cari_angkot);
        mapFragment.getMapAsync(this);

        apiService = APIService.Factory.create();

        geocoderReceiver = new GeocoderIntentService();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);

        double lat = -7.982449;
        double lng = 112.630712;
        int zoom = 14;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom);
                mGoogleMap.moveCamera(cameraUpdate);

                retrieveFileFromResourceAL();
                retrieveFileFromResourceAG();
                retrieveFileFromResourceLG();
            }
        } else {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom);
            mGoogleMap.moveCamera(cameraUpdate);

            retrieveFileFromResourceAL();
            retrieveFileFromResourceAG();
            retrieveFileFromResourceLG();
        }
    }

    private void retrieveFileFromResourceAL() {
        try {
            kmlLayer = new KmlLayer(mGoogleMap, R.raw.angkutanal, getApplicationContext());
            kmlLayer.addLayerToMap();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private void retrieveFileFromResourceAG() {
        try {
            kmlLayer = new KmlLayer(mGoogleMap, R.raw.angkutanag, getApplicationContext());
            kmlLayer.addLayerToMap();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private void retrieveFileFromResourceLG() {
        try {
            kmlLayer = new KmlLayer(mGoogleMap, R.raw.angkutanlg, getApplicationContext());
            kmlLayer.addLayerToMap();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    public void getAngkotAL(){
        Call<AngkotALModel> angkotAL = apiService.getAngkotAl();
        angkotAL.enqueue(new Callback<AngkotALModel>() {
            @Override
            public void onResponse(Call<AngkotALModel> call, Response<AngkotALModel> response) {
                al = new ArrayList<>(response.body().getMikrolet_al());
            }

            @Override
            public void onFailure(Call<AngkotALModel> call, Throwable t) {

            }
        });
    }

    public void getAngkotLG(){
        Call<AngkotLGModel> angkotLG = apiService.getAngkotLG();
        angkotLG.enqueue(new Callback<AngkotLGModel>() {
            @Override
            public void onResponse(Call<AngkotLGModel> call, Response<AngkotLGModel> response) {
                lg = new ArrayList<>(response.body().getMikrolet_lg());
            }

            @Override
            public void onFailure(Call<AngkotLGModel> call, Throwable t) {

            }
        });
    }

    public void getAngkotAG(){
        Call<AngkotAGModel> angkotAG = apiService.getAngkotAG();
        angkotAG.enqueue(new Callback<AngkotAGModel>() {
            @Override
            public void onResponse(Call<AngkotAGModel> call, Response<AngkotAGModel> response) {
                ag = new ArrayList<>(response.body().getMikrolet_ag());
            }

            @Override
            public void onFailure(Call<AngkotAGModel> call, Throwable t) {

            }
        });
    }

    public void jalurAngkot(){
        Call<JalurAngkotModel> jalurAngkot = apiService.jalurAngkot();
        jalurAngkot.enqueue(new Callback<JalurAngkotModel>() {
            @Override
            public void onResponse(Call<JalurAngkotModel> call, Response<JalurAngkotModel> response) {
                jalur = new ArrayList<>(response.body().getJalur());
            }

            @Override
            public void onFailure(Call<JalurAngkotModel> call, Throwable t) {

            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
        if (tujuanAwal.getText().toString().equals("") && tujuanAkhir.getText().toString().equals("")){
            Toast.makeText(CariAngkotActivity.this, "Titik Awal dan Titik Akhir harus diisi terlebih dahulu", Toast.LENGTH_SHORT).show();
        } else if (tujuanAwal.getText().toString().equals("")){
            Toast.makeText(CariAngkotActivity.this, "Titik Awal harus diisi", Toast.LENGTH_SHORT).show();
        } else if (tujuanAkhir.getText().toString().equals("")){
            Toast.makeText(CariAngkotActivity.this, "Titik Akhir harus diisi", Toast.LENGTH_SHORT).show();
        } else {
            titikAwal();
            titikTujan();
            tujuanAwal.setText("");
            tujuanAkhir.setText("");
        }
    }

    public void titikAwal(){
        String locationAwal = tujuanAwal.getText().toString();
        List<Address> addressList = null;
        if (locationAwal != null || !locationAwal.equals("")){
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                addressList = geocoder.getFromLocationName(locationAwal, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(locationAwal);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            mGoogleMap.addMarker(markerOptions);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    public void titikTujan(){
        String locationTujuan = tujuanAkhir.getText().toString();
        List<Address> addressList = null;
        if (locationTujuan != null || !locationTujuan.equals("")){
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                addressList = geocoder.getFromLocationName(locationTujuan, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(locationTujuan);
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng))
                    .setTitle(locationTujuan);
//            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }
}
