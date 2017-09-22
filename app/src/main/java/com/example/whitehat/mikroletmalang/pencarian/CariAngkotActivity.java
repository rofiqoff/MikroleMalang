package com.example.whitehat.mikroletmalang.pencarian;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whitehat.mikroletmalang.BuildConfig;
import com.example.whitehat.mikroletmalang.R;
import com.example.whitehat.mikroletmalang.angkot.database.APIService;
import com.example.whitehat.mikroletmalang.angkot.database.model.AngkotAGModel;
import com.example.whitehat.mikroletmalang.angkot.database.model.AngkotALModel;
import com.example.whitehat.mikroletmalang.angkot.database.model.AngkotLGModel;
import com.example.whitehat.mikroletmalang.angkot.database.model.JalurAngkot;
import com.example.whitehat.mikroletmalang.angkot.database.model.JalurAngkotModel;
import com.example.whitehat.mikroletmalang.pencarian.Util.Adapter;
import com.example.whitehat.mikroletmalang.pencarian.Util.JalurAdapterAwal;
import com.example.whitehat.mikroletmalang.pencarian.Util.JalurAdapterTujuan;
import com.example.whitehat.mikroletmalang.pencarian.geolocation.GeocoderIntentService;
import com.google.android.gms.common.ConnectionResult;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CariAngkotActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnTouchListener {

    private GoogleMap mGoogleMap;
    private KmlLayer kmlLayer;
    private APIService apiService;

    private EditText posisiAwal;
    private EditText posisiAkhir;
    private ImageView cariAngkot;
    private TextView textHasil;
    private ProgressBar progressBar;
    private ImageButton closeAwal;
    private ImageButton closeAkhir;
    private RelativeLayout hasil;

    private GeocoderIntentService geocoderReceiver;

    public static final int CODE_PLACE_AWAL = 1;
    public static final int CODE_PLACE_AKHIR = 2;

    ArrayList<AngkotALModel.MikroletAl> al = new ArrayList<>();
    ArrayList<AngkotAGModel.MikroletAG> ag = new ArrayList<>();
    ArrayList<AngkotLGModel.MikroletLG> lg = new ArrayList<>();
    ArrayList<JalurAngkotModel.JalurAngkot> jalur = new ArrayList<>();
    ArrayList<JalurAngkot.Jalur> jalurs = new ArrayList<>();

    private JalurAdapterAwal jalurAdapterAwal;
    private JalurAdapterTujuan jalurAdapterTujuan;

    private Adapter adapterPosisiAwal, adapterPosisiAkhir;

    Marker markerAwal, markerTujuan;

    private float downX, downY, upX, upY;

    String namaAngkot = "";

    public static final String TAG = "CariAngkotLOG";

    private RecyclerView recyclerViewJalurAwal, recyclerViewJalurAkhir;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_angkot);

        context = CariAngkotActivity.this;

        jalur = new ArrayList<>();
        jalurs = new ArrayList<>();

        apiService = APIService.Factory.create();

        posisiAwal = (EditText) findViewById(R.id.text_posisi_awal);
        posisiAkhir = (EditText) findViewById(R.id.text_posisi_akhir);

        recyclerViewJalurAwal = (RecyclerView) findViewById(R.id.recyclerView_posisi_awal);
        recyclerViewJalurAkhir = (RecyclerView) findViewById(R.id.recyclerView_posisi_akhir);

        LinearLayoutManager layoutManagerAwal = new LinearLayoutManager(CariAngkotActivity.this);
        LinearLayoutManager layoutManagerAkhir = new LinearLayoutManager(CariAngkotActivity.this);

        recyclerViewJalurAwal.setLayoutManager(layoutManagerAwal);
        recyclerViewJalurAkhir.setLayoutManager(layoutManagerAkhir);
        recyclerViewJalurAwal.setHasFixedSize(true);
        recyclerViewJalurAkhir.setHasFixedSize(true);

        adapterPosisiAwal = new Adapter(CariAngkotActivity.this, jalurs, R.layout.item_row_alamat, apiService);
        adapterPosisiAkhir = new Adapter(CariAngkotActivity.this, jalurs, R.layout.item_row_alamat, apiService);

        recyclerViewJalurAkhir.setAdapter(adapterPosisiAkhir);
        recyclerViewJalurAwal.setAdapter(adapterPosisiAwal);

        closeAwal = (ImageButton) findViewById(R.id.clear_awal);
        closeAkhir = (ImageButton) findViewById(R.id.clear_tujuan);

        hasil = (RelativeLayout) findViewById(R.id.rl_hasil);

        cariAngkot = (ImageView) findViewById(R.id.cari_angkot_tujuan);
        textHasil = (TextView) findViewById(R.id.hasil_tujuan_angkot);
        progressBar = (ProgressBar) findViewById(R.id.pbLoading_cari_angkot);

        cariAngkot.setOnClickListener(this);

        posisiAwal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                closeAwal.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    closeAwal.setVisibility(View.VISIBLE);
                    closeAwal.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            posisiAwal.setText("");
                        }
                    });

                    recyclerViewJalurAwal.setVisibility(View.VISIBLE);
                    adapterPosisiAwal.getFilter().filter(s.toString());

                } else {
                    closeAwal.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        posisiAkhir.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                closeAkhir.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    closeAkhir.setVisibility(View.VISIBLE);
                    closeAkhir.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            posisiAkhir.setText("");
                        }
                    });

                    recyclerViewJalurAkhir.setVisibility(View.VISIBLE);

                    adapterPosisiAkhir.getFilter().filter(s.toString());

                } else {
                    closeAkhir.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_cari_angkot);
        mapFragment.getMapAsync(this);

        apiService = APIService.Factory.create();
        geocoderReceiver = new GeocoderIntentService();

//        jalur();
//        getAngkotAL();
//        getAngkotAG();
//        getAngkotLG();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);

        double lat = -7.982449;
        double lng = 112.630712;
        int zoom = 13;

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
        final Call<JalurAngkotModel> jalurAngkot = apiService.jalurAngkot();
        jalurAngkot.enqueue(new Callback<JalurAngkotModel>() {
            @Override
            public void onResponse(Call<JalurAngkotModel> call, Response<JalurAngkotModel> response) {
//                jalur = new ArrayList<>(response.body().getJalur());
//                jalurAdapterAwal = new JalurAdapterAwal(getApplicationContext(), R.layout.item_row_alamat, R.id.tv_alamat, R.id.tv_koordinat, jalur);
//                jalurAdapterTujuan = new JalurAdapterTujuan(getApplicationContext(), R.layout.item_row_alamat, R.id.tv_alamat, R.id.tv_koordinat, jalur);
//                posisiAwal.setAdapter(jalurAdapterAwal);
//                posisiAkhir.setAdapter(jalurAdapterTujuan);
            }

            @Override
            public void onFailure(Call<JalurAngkotModel> call, Throwable t) {

            }
        });
    }

    public void jalur() {
        final Call<JalurAngkot> jalur = apiService.jalur();
        jalur.enqueue(new Callback<JalurAngkot>() {
            @Override
            public void onResponse(Call<JalurAngkot> call, Response<JalurAngkot> response) {
                jalurs = new ArrayList<JalurAngkot.Jalur>(response.body().getJalur());
                adapterPosisiAwal = new Adapter(CariAngkotActivity.this, jalurs, R.layout.item_row_alamat, apiService);
                adapterPosisiAkhir = new Adapter(CariAngkotActivity.this, jalurs, R.layout.item_row_alamat, apiService);
                recyclerViewJalurAwal.setAdapter(adapterPosisiAwal);
                recyclerViewJalurAkhir.setAdapter(adapterPosisiAkhir);
            }

            @Override
            public void onFailure(Call<JalurAngkot> call, Throwable t) {
                Log.e(TAG, "Kesalahan jaringan : " + t.getMessage());
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
        if (v == cariAngkot) {

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

            if (posisiAwal.getText().toString().equals("") && posisiAkhir.getText().toString().equals("")) {
                Toast.makeText(CariAngkotActivity.this, "Titik Awal dan Titik Akhir harus diisi terlebih dahulu", Toast.LENGTH_SHORT).show();
            } else if (posisiAwal.getText().toString().equals("")) {
                Toast.makeText(CariAngkotActivity.this, "Titik Awal harus diisi", Toast.LENGTH_SHORT).show();
            } else if (posisiAkhir.getText().toString().equals("")) {
                Toast.makeText(CariAngkotActivity.this, "Titik Akhir harus diisi", Toast.LENGTH_SHORT).show();
            } else {
                titikAwal();
                titikTujan();
                searchAngkot();
            }

        }
    }

    public void titikAwal() {
        if (markerAwal != null) {
            markerAwal.remove();
        }

        double lat = Double.parseDouble(jalurAdapterAwal.lat);
        double lng = Double.parseDouble(jalurAdapterAwal.lng);


//        double lat = Double.parseDouble(jalurAdapterAwal.address.get(0).getLat());
//        double lng = Double.parseDouble(jalurAdapterAwal.address.get(0).getLng());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, lng));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        markerAwal = mGoogleMap.addMarker(markerOptions);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));

    }

    public void titikTujan() {

        if (markerTujuan != null) {
            markerTujuan.remove();
        }

        double lat = Double.parseDouble(jalurAdapterTujuan.address.get(0).getLat());
        double lng = Double.parseDouble(jalurAdapterTujuan.address.get(0).getLng());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, lng));
        markerTujuan = mGoogleMap.addMarker(markerOptions);

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
        }
        if (requestCode == CODE_PLACE_AKHIR) {
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

    public void swipeToGone() {
        hasil.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int swape = event.getAction();
        switch (swape) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                if (Math.abs(deltaX) > 100) {
                    if (deltaX < 0) {
                        this.swipeToGone();
                        return true;
                    }

                    if (deltaX > 0) {
                        this.swipeToGone();
                        return true;
                    }
                }

                if (Math.abs(deltaY) > 100) {
                    if (deltaY < 0) {
                        this.swipeToGone();
                        return true;
                    }
                }

                return false;
        }
        return false;
    }

    public void searchAngkot() {

        String startPoint_node = jalurAdapterAwal.nodeJalur;
        String endPoint_node = jalurAdapterTujuan.nodeJalur;

//        String startPoint_DN = jalurAdapterAwal.kodeJalur;
//        String endPoint_DN = jalurAdapterTujuan.kodeJalur;

        String namaJalanAwal = jalurAdapterAwal.namaJalan;
        String namaJalanTujuan = jalurAdapterTujuan.namaJalan;

//        String angkot = jalurAdapterAwal.kodeMikrolet;
//        String angkot2 = jalurAdapterTujuan.kodeMikrolet;

//        algoritmaPencarian(startPoint_node, endPoint_node, startPoint_DN, endPoint_DN, namaJalanAwal, namaJalanTujuan, angkot, angkot2);

    }

    public void algoritmaPencarian(String Fi, String Fj, String startPoint_Dn, String endPoint_DN, String namaJalanAwal, String namaJalanTujuan, String angkot, String angkot2) {

        int i = 0;
        int j = 0;

        if (Fi.equalsIgnoreCase("F1")) {
            i = 0;
        } else if (Fi.equalsIgnoreCase("F2")) {
            i = 1;
        } else if (Fi.equalsIgnoreCase("F3")) {
            i = 2;
        } else if (Fi.equalsIgnoreCase("F4")) {
            i = 3;
        } else if (Fi.equalsIgnoreCase("F5")) {
            i = 4;
        } else if (Fi.equalsIgnoreCase("F6")) {
            i = 5;
        } else if (Fi.equalsIgnoreCase("F7")) {
            i = 6;
        } else if (Fi.equalsIgnoreCase("F8")) {
            i = 7;
        } else if (Fi.equalsIgnoreCase("F9")) {
            i = 8;
        } else if (Fi.equalsIgnoreCase("F10")) {
            i = 9;
        } else if (Fi.equalsIgnoreCase("F11")) {
            i = 10;
        } else if (Fi.equalsIgnoreCase("F12")) {
            i = 11;
        } else if (Fi.equalsIgnoreCase("F13")) {
            i = 12;
        } else if (Fi.equalsIgnoreCase("F14")) {
            i = 13;
        } else if (Fi.equalsIgnoreCase("F15")) {
            i = 14;
        } else if (Fi.equalsIgnoreCase("F16")) {
            i = 15;
        } else if (Fi.equalsIgnoreCase("F17")) {
            i = 16;
        } else if (Fi.equalsIgnoreCase("F18")) {
            i = 17;
        } else if (Fi.equalsIgnoreCase("F19")) {
            i = 18;
        } else if (Fi.equalsIgnoreCase("F20")) {
            i = 19;
        } else if (Fi.equalsIgnoreCase("F21")) {
            i = 20;
        } else if (Fi.equalsIgnoreCase("F22")) {
            i = 21;
        } else if (Fi.equalsIgnoreCase("F23")) {
            i = 22;
        } else if (Fi.equalsIgnoreCase("F24")) {
            i = 23;
        } else if (Fi.equalsIgnoreCase("F25")) {
            i = 24;
        } else if (Fi.equalsIgnoreCase("F26")) {
            i = 25;
        } else if (Fi.equalsIgnoreCase("F27")) {
            i = 26;
        } else if (Fi.equalsIgnoreCase("F28")) {
            i = 27;
        } else if (Fi.equalsIgnoreCase("F29")) {
            i = 28;
        } else if (Fi.equalsIgnoreCase("F30")) {
            i = 29;
        } else if (Fi.equalsIgnoreCase("F31")) {
            i = 30;
        } else if (Fi.equalsIgnoreCase("F32")) {
            i = 31;
        } else if (Fi.equalsIgnoreCase("F33")) {
            i = 32;
        } else if (Fi.equalsIgnoreCase("F34")) {
            i = 33;
        } else if (Fi.equalsIgnoreCase("F35")) {
            i = 34;
        } else if (Fi.equalsIgnoreCase("F36")) {
            i = 35;
        } else if (Fi.equalsIgnoreCase("F37")) {
            i = 36;
        } else if (Fi.equalsIgnoreCase("F38")) {
            i = 37;
        } else if (Fi.equalsIgnoreCase("F39")) {
            i = 38;
        } else if (Fi.equalsIgnoreCase("F40")) {
            i = 39;
        } else if (Fi.equalsIgnoreCase("F41")) {
            i = 40;
        } else if (Fi.equalsIgnoreCase("F42")) {
            i = 41;
        } else if (Fi.equalsIgnoreCase("F43")) {
            i = 42;
        } else if (Fi.equalsIgnoreCase("F44")) {
            i = 43;
        } else if (Fi.equalsIgnoreCase("F45")) {
            i = 44;
        } else if (Fi.equalsIgnoreCase("F46")) {
            i = 45;
        } else if (Fi.equalsIgnoreCase("F47")) {
            i = 46;
        } else if (Fi.equalsIgnoreCase("F48")) {
            i = 47;
        } else if (Fi.equalsIgnoreCase("F49")) {
            i = 48;
        } else if (Fi.equalsIgnoreCase("F50")) {
            i = 49;
        } else if (Fi.equalsIgnoreCase("F51")) {
            i = 50;
        } else if (Fi.equalsIgnoreCase("F52")) {
            i = 51;
        } else if (Fi.equalsIgnoreCase("F53")) {
            i = 52;
        } else if (Fi.equalsIgnoreCase("F54")) {
            i = 53;
        } else if (Fi.equalsIgnoreCase("F55")) {
            i = 54;
        }

        if (Fj.equalsIgnoreCase("F1")) {
            j = 0;
        } else if (Fj.equalsIgnoreCase("F2")) {
            j = 1;
        } else if (Fj.equalsIgnoreCase("F3")) {
            j = 2;
        } else if (Fj.equalsIgnoreCase("F4")) {
            j = 3;
        } else if (Fj.equalsIgnoreCase("F5")) {
            j = 4;
        } else if (Fj.equalsIgnoreCase("F6")) {
            j = 5;
        } else if (Fj.equalsIgnoreCase("F7")) {
            j = 6;
        } else if (Fj.equalsIgnoreCase("F8")) {
            j = 7;
        } else if (Fj.equalsIgnoreCase("F9")) {
            j = 8;
        } else if (Fj.equalsIgnoreCase("F10")) {
            j = 9;
        } else if (Fj.equalsIgnoreCase("F11")) {
            j = 10;
        } else if (Fj.equalsIgnoreCase("F12")) {
            j = 11;
        } else if (Fj.equalsIgnoreCase("F13")) {
            j = 12;
        } else if (Fj.equalsIgnoreCase("F14")) {
            j = 13;
        } else if (Fj.equalsIgnoreCase("F15")) {
            j = 14;
        } else if (Fj.equalsIgnoreCase("F16")) {
            j = 15;
        } else if (Fj.equalsIgnoreCase("F17")) {
            j = 16;
        } else if (Fj.equalsIgnoreCase("F18")) {
            j = 17;
        } else if (Fj.equalsIgnoreCase("F19")) {
            j = 18;
        } else if (Fj.equalsIgnoreCase("F20")) {
            j = 19;
        } else if (Fj.equalsIgnoreCase("F21")) {
            j = 20;
        } else if (Fj.equalsIgnoreCase("F22")) {
            j = 21;
        } else if (Fj.equalsIgnoreCase("F23")) {
            j = 22;
        } else if (Fj.equalsIgnoreCase("F24")) {
            j = 23;
        } else if (Fj.equalsIgnoreCase("F25")) {
            j = 24;
        } else if (Fj.equalsIgnoreCase("F26")) {
            j = 25;
        } else if (Fj.equalsIgnoreCase("F27")) {
            j = 26;
        } else if (Fj.equalsIgnoreCase("F28")) {
            j = 27;
        } else if (Fj.equalsIgnoreCase("F29")) {
            j = 28;
        } else if (Fj.equalsIgnoreCase("F30")) {
            j = 29;
        } else if (Fj.equalsIgnoreCase("F31")) {
            j = 30;
        } else if (Fj.equalsIgnoreCase("F32")) {
            j = 31;
        } else if (Fj.equalsIgnoreCase("F33")) {
            j = 32;
        } else if (Fj.equalsIgnoreCase("F34")) {
            j = 33;
        } else if (Fj.equalsIgnoreCase("F35")) {
            j = 34;
        } else if (Fj.equalsIgnoreCase("F36")) {
            j = 35;
        } else if (Fj.equalsIgnoreCase("F37")) {
            j = 36;
        } else if (Fj.equalsIgnoreCase("F38")) {
            j = 37;
        } else if (Fj.equalsIgnoreCase("F39")) {
            j = 38;
        } else if (Fj.equalsIgnoreCase("F40")) {
            j = 39;
        } else if (Fj.equalsIgnoreCase("F41")) {
            j = 40;
        } else if (Fj.equalsIgnoreCase("F42")) {
            j = 41;
        } else if (Fj.equalsIgnoreCase("F43")) {
            j = 42;
        } else if (Fj.equalsIgnoreCase("F44")) {
            j = 43;
        } else if (Fj.equalsIgnoreCase("F45")) {
            j = 44;
        } else if (Fj.equalsIgnoreCase("F46")) {
            j = 45;
        } else if (Fj.equalsIgnoreCase("F47")) {
            j = 46;
        } else if (Fj.equalsIgnoreCase("F48")) {
            j = 47;
        } else if (Fj.equalsIgnoreCase("F49")) {
            j = 48;
        } else if (Fj.equalsIgnoreCase("F50")) {
            j = 49;
        } else if (Fj.equalsIgnoreCase("F51")) {
            j = 50;
        } else if (Fj.equalsIgnoreCase("F52")) {
            j = 51;
        } else if (Fj.equalsIgnoreCase("F53")) {
            j = 52;
        } else if (Fj.equalsIgnoreCase("F54")) {
            j = 53;
        } else if (Fj.equalsIgnoreCase("F55")) {
            j = 54;
        }

        int[][] data = {
                {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 0, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 0, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 2, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {2, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}
        };

        int value = 0;

        for (int baris = 0; baris < data.length; baris++) {
            for (int kolom = 0; kolom < data[baris].length; kolom++) {
                value = data[i][j];
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d("Log matrik i int : ", String.valueOf(i));
            Log.d("Log matrik j int : ", String.valueOf(j));
            Log.d("Log value : ", String.valueOf(value));
            Log.d("Log angkot : ", angkot);
            Log.d("Log jalan awal : ", namaJalanAwal);
            Log.d("Log jalan akhir : ", namaJalanTujuan);
            Log.d("Log DN : ", startPoint_Dn);
            Log.d("Log DN : ", endPoint_DN);
        }

        if (angkot.equalsIgnoreCase("M1") && angkot2.equalsIgnoreCase("M1")) {
            namaAngkot = "AL";
        } else if (angkot.equalsIgnoreCase("M2") && angkot2.equalsIgnoreCase("M2")) {
            namaAngkot = "LG";
        } else if (angkot.equalsIgnoreCase("M3") && angkot2.equalsIgnoreCase("M3")) {
            namaAngkot = "AG";
        }

        if (i == j) {
            hasil.setVisibility(View.VISIBLE);
            textHasil.setText(getString(R.string.text_posisi_sama));
        } else if (value == 1) {
            if (startPoint_Dn.equalsIgnoreCase(endPoint_DN)) {
                hasil.setVisibility(View.VISIBLE);
                textHasil.setText("Untuk jalur dari " + namaJalanAwal + " ke " + namaJalanTujuan + ", Anda bisa memilih Angkot " + namaAngkot);
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e("Log DN", "DN tidak ada");
                }
            }
        } else if (value == 2) {

            Log.e("Log Value", "Value adalah 2");

            if ((i == 1) && (j == 1)) {
                int m1;

            }

        }

    }

}
