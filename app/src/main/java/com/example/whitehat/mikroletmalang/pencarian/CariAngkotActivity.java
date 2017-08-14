package com.example.whitehat.mikroletmalang.pencarian;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

    private TextView posisiAwal;
    private TextView posisiAkhir;
    private ImageView cariAngkot;
    private TextView textHasil;
    private ProgressBar progressBar;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private int PLACE_PICKER_REQUEST = 1;
    private GeocoderIntentService geocoderReceiver;

    public static final int CODE_PLACE_AWAL = 1;
    public static final int CODE_PLACE_AKHIR = 2;

    ArrayList<AngkotALModel.MikroletAl> al = new ArrayList<>();
    ArrayList<AngkotAGModel.MikroletAG> ag = new ArrayList<>();
    ArrayList<AngkotLGModel.MikroletLG> lg = new ArrayList<>();
    ArrayList<JalurAngkotModel.JalurAngkot> jalur = new ArrayList<>();

//    koordinat AL
    float latAlAwal, longAlAwal, latAlAkhir, longAlAkhir;

//    koordinat AG
    float latAgAwal, longAgAwal, latAgAkhir, longAgAkhir;

//    koordinat LG
    float latLgAwal, longLgAwal, latLgAkhir, longLgAkhir;

    float latJalur, longJalur;
    LatLng latLngTujuan, latLngAwal;

    Address addressAwal, addressAkhir;

    Location locationAwalAL = new Location("lokasi Awal Al");
    Location locationAkhirAL = new Location("lokasi Akhir Al");

    Location locationAwalAG = new Location("lokasi Awal AG");
    Location locationAkhirAG = new Location("lokasi Akhir AG");

    Location locationAwalLG = new Location("lokasi Awal LG");
    Location locationAkhirLG = new Location("lokasi Akhir LG");

    Location locationAwal = new Location("lokasi Awal");
    Location locationAkhir = new Location("lokasi Akhir");

    Location locationJalur = new Location("lokasi Jalur");

    String kodeMikrolet, namaMikrolet;

    Marker markerAwal, markerTujuan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_angkot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        posisiAwal = (TextView) findViewById(R.id.text_posisi_awal);
        posisiAkhir = (TextView) findViewById(R.id.text_posisi_akhir);
        cariAngkot = (ImageView) findViewById(R.id.cari_angkot_tujuan);
        textHasil = (TextView) findViewById(R.id.hasil_tujuan_angkot);
        progressBar = (ProgressBar) findViewById(R.id.pbLoading_cari_angkot);

        cariAngkot.setOnClickListener(this);
        posisiAwal.setOnClickListener(this);
        posisiAkhir.setOnClickListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_cari_angkot);
        mapFragment.getMapAsync(this);

        apiService = APIService.Factory.create();
        geocoderReceiver = new GeocoderIntentService();

        jalurAngkot();
        getAngkotAL();
        getAngkotAG();
        getAngkotLG();

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);

        double lat = -7.982449;
        double lng = 112.630712;
        int zoom = 14;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

    public void getAngkotAL() {
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

    public void getAngkotLG() {
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

    public void getAngkotAG() {
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

    public void jalurAngkot() {
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
        if (v == posisiAwal) {
            findPlaceAwal();
        } else if (v == posisiAkhir) {
            findPlaceTujuan();
        } else if (v == cariAngkot) {

            if (posisiAwal.getText().toString().equals("") && posisiAkhir.getText().toString().equals("")) {
                Toast.makeText(CariAngkotActivity.this, "Titik Awal dan Titik Akhir harus diisi terlebih dahulu", Toast.LENGTH_SHORT).show();
            } else if (posisiAwal.getText().toString().equals("")) {
                Toast.makeText(CariAngkotActivity.this, "Titik Awal harus diisi", Toast.LENGTH_SHORT).show();
            } else if (posisiAkhir.getText().toString().equals("")) {
                Toast.makeText(CariAngkotActivity.this, "Titik Akhir harus diisi", Toast.LENGTH_SHORT).show();
            } else {
                titikAwal();
                titikTujan();
//                searchAngkot();
                posisiAwal.setText("");
                posisiAkhir.setText("");
            }

        }
    }

    public void titikAwal() {
        if (markerAwal != null){
            markerAwal.remove();
        }

        String locationAwal = posisiAwal.getText().toString();
        List<Address> addressList = null;
        if (locationAwal != null || !locationAwal.equals("")) {
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                addressList = geocoder.getFromLocationName(locationAwal, CODE_PLACE_AWAL);
            } catch (IOException e) {
                e.printStackTrace();
            }

            addressAwal = addressList.get(0);
            latLngAwal = new LatLng(addressAwal.getLatitude(), addressAwal.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLngAwal);
            markerOptions.title(locationAwal);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            markerAwal = mGoogleMap.addMarker(markerOptions);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLngAwal));
        }
    }

    public void titikTujan() {

        if (markerTujuan != null){
            markerTujuan.remove();
        }
        String locationTujuan = posisiAkhir.getText().toString();
        List<Address> addressList = null;
        if (locationTujuan != null || !locationTujuan.equals("")) {
            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                addressList = geocoder.getFromLocationName(locationTujuan, CODE_PLACE_AKHIR);
            } catch (IOException e) {
                e.printStackTrace();
            }

            addressAkhir = addressList.get(0);
            latLngTujuan = new LatLng(addressAkhir.getLatitude(), addressAkhir.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLngTujuan);
            markerOptions.title(locationTujuan);
            markerTujuan = mGoogleMap.addMarker(markerOptions);
//            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    public void findPlaceAwal() {
        try {
            Intent intent =
                    new PlaceAutocomplete
                            .IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, CODE_PLACE_AWAL);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public void findPlaceTujuan(){
        try {
            Intent intent = new PlaceAutocomplete
                    .IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, CODE_PLACE_AKHIR);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_PLACE_AWAL) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.e("TAG", "Place: " + place.getAddress() + place.getPhoneNumber());

                posisiAwal.setText(place.getAddress());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e("TAG", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {

            }
        } if (requestCode == CODE_PLACE_AKHIR) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.e("TAG", "Place: " + place.getAddress() + place.getPhoneNumber());

                posisiAkhir.setText(place.getAddress());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e("TAG", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }

    public void searchAngkot(){

        locationAwal.setLatitude(addressAwal.getLatitude());
        locationAwal.setLongitude(addressAwal.getLongitude());

        locationAkhir.setLatitude(addressAkhir.getLatitude());
        locationAkhir.setLongitude(addressAkhir.getLongitude());

        for (int i = 0; i <= jalur.size(); i++){
            latJalur = Float.parseFloat(jalur.get(i).getLat_jalur());
            longJalur = Float.parseFloat(jalur.get(i).getLong_jalur());

            kodeMikrolet = jalur.get(i).getKode_mikrolet();

            locationJalur.setLatitude(latJalur);
            locationJalur.setLongitude(longJalur);

            float jarakLokasi = locationAwal.distanceTo(locationJalur);

            if (locationJalur == locationAwal){
                if (kodeMikrolet.equalsIgnoreCase("M1")){
                    namaMikrolet = "AL";
                } else if (kodeMikrolet.equalsIgnoreCase("M2")){
                    namaMikrolet = "LG";
                } else if (kodeMikrolet.equalsIgnoreCase("M3")){
                    namaMikrolet = "AG";
                }
                Toast.makeText(CariAngkotActivity.this, "Nama Mikrolet 1 : "+namaMikrolet, Toast.LENGTH_SHORT).show();
            } else if (locationJalur == locationAkhir){
                if (kodeMikrolet.equalsIgnoreCase("M1")){
                    namaMikrolet = "AL";
                } else if (kodeMikrolet.equalsIgnoreCase("M2")){
                    namaMikrolet = "LG";
                } else if (kodeMikrolet.equalsIgnoreCase("M3")){
                    namaMikrolet = "AG";
                }
                Toast.makeText(CariAngkotActivity.this, "Nama Mikrolet 2 : "+namaMikrolet, Toast.LENGTH_SHORT).show();
            } else if (jarakLokasi <= 500){
                if (kodeMikrolet.equalsIgnoreCase("M1")){
                    namaMikrolet = "AL";
                } else if (kodeMikrolet.equalsIgnoreCase("M2")){
                    namaMikrolet = "LG";
                } else if (kodeMikrolet.equalsIgnoreCase("M3")){
                    namaMikrolet = "AG";
                }
                Toast.makeText(CariAngkotActivity.this, "Nama Mikrolet 3 : "+namaMikrolet, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
