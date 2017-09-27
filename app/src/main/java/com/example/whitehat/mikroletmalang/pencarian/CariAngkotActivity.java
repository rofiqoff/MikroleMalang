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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
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
import com.example.whitehat.mikroletmalang.angkot.database.model.KodeMirolet;
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
import com.google.android.gms.maps.model.CameraPosition;
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

    private AutoCompleteTextView posisiAwal;
    private AutoCompleteTextView posisiAkhir;
    private ImageView cariAngkot;
    private TextView textHasil;
    private ImageButton closeAwal;
    private ImageButton closeAkhir;
    private RelativeLayout hasil;
    private ProgressBar progressBar;

    private GeocoderIntentService geocoderReceiver;

    public static final int CODE_PLACE_AWAL = 1;
    public static final int CODE_PLACE_AKHIR = 2;

    ArrayList<AngkotALModel.MikroletAl> al = new ArrayList<>();
    ArrayList<AngkotAGModel.MikroletAG> ag = new ArrayList<>();
    ArrayList<AngkotLGModel.MikroletLG> lg = new ArrayList<>();
    ArrayList<JalurAngkotModel.JalurAngkot> jalur = new ArrayList<>();
    ArrayList<JalurAngkot.Jalur> jalurs = new ArrayList<>();
    ArrayList<KodeMirolet.KodeJalur> kodeJalurs = new ArrayList<>();

    private JalurAdapterAwal jalurAdapterAwal;
    private JalurAdapterTujuan jalurAdapterTujuan;

    Marker markerAwal, markerTujuan;

    private float downX, downY, upX, upY;

    String namaAngkot = "";

    public static final String TAG = "CariAngkotLOG";

    private RecyclerView recyclerViewJalurAwal, recyclerViewJalurAkhir;

    Context context;

    AdapterAwal adapterAawal;
    AdapterAkhir adapterAkhir;
    Algoritma hitungAlgoritma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_angkot);

        context = CariAngkotActivity.this;

        jalur = new ArrayList<>();
        jalurs = new ArrayList<>();
        kodeJalurs = new ArrayList<>();

        apiService = APIService.Factory.create();

        posisiAwal = (AutoCompleteTextView) findViewById(R.id.text_posisi_awal);
        posisiAkhir = (AutoCompleteTextView) findViewById(R.id.text_posisi_akhir);

        recyclerViewJalurAwal = (RecyclerView) findViewById(R.id.recyclerView_posisi_awal);
        recyclerViewJalurAkhir = (RecyclerView) findViewById(R.id.recyclerView_posisi_akhir);

        LinearLayoutManager layoutManagerAwal = new LinearLayoutManager(CariAngkotActivity.this);
        LinearLayoutManager layoutManagerAkhir = new LinearLayoutManager(CariAngkotActivity.this);

        recyclerViewJalurAwal.setLayoutManager(layoutManagerAwal);
        recyclerViewJalurAkhir.setLayoutManager(layoutManagerAkhir);
        recyclerViewJalurAwal.setHasFixedSize(true);
        recyclerViewJalurAkhir.setHasFixedSize(true);

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
                } else {
                    closeAwal.setVisibility(View.GONE);
                    hasil.setVisibility(View.GONE);

                    if (markerAwal != null) {
                        markerAwal.remove();
                    }
                    if (markerTujuan != null) {
                        markerTujuan.remove();
                    }

                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(-7.982449, 112.630712), 13)));
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

                } else {
                    closeAkhir.setVisibility(View.GONE);
                    hasil.setVisibility(View.GONE);

                    if (markerAwal != null) {
                        markerAwal.remove();
                    }
                    if (markerTujuan != null) {
                        markerTujuan.remove();
                    }

                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(-7.982449, 112.630712), 13)));
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

        jalur();
        getAngkotAL();
        getAngkotAG();
        getAngkotLG();

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

    public void jalur() {

        final Call<JalurAngkot> jalur = apiService.jalur();
        jalur.enqueue(new Callback<JalurAngkot>() {
            @Override
            public void onResponse(Call<JalurAngkot> call, Response<JalurAngkot> response) {
                jalurs = new ArrayList<JalurAngkot.Jalur>(response.body().getJalur());

                jalurAdapterAwal = new JalurAdapterAwal(CariAngkotActivity.this, R.layout.item_row_alamat, R.id.tv_alamat, R.id.tv_koordinat, jalurs);
                jalurAdapterTujuan = new JalurAdapterTujuan(CariAngkotActivity.this, R.layout.item_row_alamat, R.id.tv_alamat, R.id.tv_koordinat, jalurs);

                posisiAwal.setAdapter(jalurAdapterAwal);
                posisiAkhir.setAdapter(jalurAdapterTujuan);

                posisiAwal.setThreshold(2);
                posisiAkhir.setThreshold(2);

                hitungAlgoritma = new Algoritma();

                posisiAwal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        posisiAwal.setText(jalurs.get(position).getNama_jalan());

                        String sLat = jalurs.get(position).getLat();
                        String sLng = jalurs.get(position).getLng();

                        double lat = Double.parseDouble(sLat);
                        double lng = Double.parseDouble(sLng);

                        String nama = jalurs.get(position).getNama_jalan();
                        String node = jalurs.get(position).getNode_jalur();

                        adapterAawal = new AdapterAwal(CariAngkotActivity.this, lat, lng, nama, node);
                        adapterAawal.toHitung(hitungAlgoritma);

                        titikAwal(lat, lng);

                        Toast.makeText(CariAngkotActivity.this, "Lat : " + lat + ", " + ", Long : " + lng, Toast.LENGTH_SHORT).show();
                    }
                });

                posisiAkhir.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        posisiAkhir.setText(jalurs.get(position).getNama_jalan());

                        String sLat = jalurs.get(position).getLat();
                        String sLng = jalurs.get(position).getLng();

                        double lat = Double.parseDouble(sLat);
                        double lng = Double.parseDouble(sLng);

                        titikTujan(lat, lng);

                        String nama = jalurs.get(position).getNama_jalan();
                        String node = jalurs.get(position).getNode_jalur();

                        adapterAkhir = new AdapterAkhir(lat, lng, nama, node, CariAngkotActivity.this);
                        adapterAkhir.toHitung(hitungAlgoritma);

                        Toast.makeText(CariAngkotActivity.this, "Lat : " + lat + ", " + ", Long : " + lng, Toast.LENGTH_SHORT).show();
                    }
                });
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
                cariAngkot();
            }

        }
    }

    public void titikAwal(double latitude, double longitude) {
        if (markerAwal != null) {
            markerAwal.remove();
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(latitude, longitude));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        markerAwal = mGoogleMap.addMarker(markerOptions);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));

    }

    public void titikTujan(double latitude, double longitude) {

        if (markerTujuan != null) {
            markerTujuan.remove();
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(latitude, longitude));
        markerTujuan = mGoogleMap.addMarker(markerOptions);

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));

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

    public void cariAngkot() {
        hitungAlgoritma.cari(CariAngkotActivity.this);
        hitungAlgoritma.algoritmaPencarian();
    }

//    public void algoritmaPencarian(String Fi, String Fj, String startPoint_Dn, String endPoint_DN, String namaJalanAwal, String namaJalanTujuan, String angkot, String angkot2) {
//
//        int i = 0;
//        int j = 0;
//
//        if (Fi.equalsIgnoreCase("F1")) {
//            i = 0;
//        } else if (Fi.equalsIgnoreCase("F2")) {
//            i = 1;
//        } else if (Fi.equalsIgnoreCase("F3")) {
//            i = 2;
//        } else if (Fi.equalsIgnoreCase("F4")) {
//            i = 3;
//        } else if (Fi.equalsIgnoreCase("F5")) {
//            i = 4;
//        } else if (Fi.equalsIgnoreCase("F6")) {
//            i = 5;
//        } else if (Fi.equalsIgnoreCase("F7")) {
//            i = 6;
//        } else if (Fi.equalsIgnoreCase("F8")) {
//            i = 7;
//        } else if (Fi.equalsIgnoreCase("F9")) {
//            i = 8;
//        } else if (Fi.equalsIgnoreCase("F10")) {
//            i = 9;
//        } else if (Fi.equalsIgnoreCase("F11")) {
//            i = 10;
//        } else if (Fi.equalsIgnoreCase("F12")) {
//            i = 11;
//        } else if (Fi.equalsIgnoreCase("F13")) {
//            i = 12;
//        } else if (Fi.equalsIgnoreCase("F14")) {
//            i = 13;
//        } else if (Fi.equalsIgnoreCase("F15")) {
//            i = 14;
//        } else if (Fi.equalsIgnoreCase("F16")) {
//            i = 15;
//        } else if (Fi.equalsIgnoreCase("F17")) {
//            i = 16;
//        } else if (Fi.equalsIgnoreCase("F18")) {
//            i = 17;
//        } else if (Fi.equalsIgnoreCase("F19")) {
//            i = 18;
//        } else if (Fi.equalsIgnoreCase("F20")) {
//            i = 19;
//        } else if (Fi.equalsIgnoreCase("F21")) {
//            i = 20;
//        } else if (Fi.equalsIgnoreCase("F22")) {
//            i = 21;
//        } else if (Fi.equalsIgnoreCase("F23")) {
//            i = 22;
//        } else if (Fi.equalsIgnoreCase("F24")) {
//            i = 23;
//        } else if (Fi.equalsIgnoreCase("F25")) {
//            i = 24;
//        } else if (Fi.equalsIgnoreCase("F26")) {
//            i = 25;
//        } else if (Fi.equalsIgnoreCase("F27")) {
//            i = 26;
//        } else if (Fi.equalsIgnoreCase("F28")) {
//            i = 27;
//        } else if (Fi.equalsIgnoreCase("F29")) {
//            i = 28;
//        } else if (Fi.equalsIgnoreCase("F30")) {
//            i = 29;
//        } else if (Fi.equalsIgnoreCase("F31")) {
//            i = 30;
//        } else if (Fi.equalsIgnoreCase("F32")) {
//            i = 31;
//        } else if (Fi.equalsIgnoreCase("F33")) {
//            i = 32;
//        } else if (Fi.equalsIgnoreCase("F34")) {
//            i = 33;
//        } else if (Fi.equalsIgnoreCase("F35")) {
//            i = 34;
//        } else if (Fi.equalsIgnoreCase("F36")) {
//            i = 35;
//        } else if (Fi.equalsIgnoreCase("F37")) {
//            i = 36;
//        } else if (Fi.equalsIgnoreCase("F38")) {
//            i = 37;
//        } else if (Fi.equalsIgnoreCase("F39")) {
//            i = 38;
//        } else if (Fi.equalsIgnoreCase("F40")) {
//            i = 39;
//        } else if (Fi.equalsIgnoreCase("F41")) {
//            i = 40;
//        } else if (Fi.equalsIgnoreCase("F42")) {
//            i = 41;
//        } else if (Fi.equalsIgnoreCase("F43")) {
//            i = 42;
//        } else if (Fi.equalsIgnoreCase("F44")) {
//            i = 43;
//        } else if (Fi.equalsIgnoreCase("F45")) {
//            i = 44;
//        } else if (Fi.equalsIgnoreCase("F46")) {
//            i = 45;
//        } else if (Fi.equalsIgnoreCase("F47")) {
//            i = 46;
//        } else if (Fi.equalsIgnoreCase("F48")) {
//            i = 47;
//        } else if (Fi.equalsIgnoreCase("F49")) {
//            i = 48;
//        } else if (Fi.equalsIgnoreCase("F50")) {
//            i = 49;
//        } else if (Fi.equalsIgnoreCase("F51")) {
//            i = 50;
//        } else if (Fi.equalsIgnoreCase("F52")) {
//            i = 51;
//        } else if (Fi.equalsIgnoreCase("F53")) {
//            i = 52;
//        } else if (Fi.equalsIgnoreCase("F54")) {
//            i = 53;
//        } else if (Fi.equalsIgnoreCase("F55")) {
//            i = 54;
//        }
//
//        if (Fj.equalsIgnoreCase("F1")) {
//            j = 0;
//        } else if (Fj.equalsIgnoreCase("F2")) {
//            j = 1;
//        } else if (Fj.equalsIgnoreCase("F3")) {
//            j = 2;
//        } else if (Fj.equalsIgnoreCase("F4")) {
//            j = 3;
//        } else if (Fj.equalsIgnoreCase("F5")) {
//            j = 4;
//        } else if (Fj.equalsIgnoreCase("F6")) {
//            j = 5;
//        } else if (Fj.equalsIgnoreCase("F7")) {
//            j = 6;
//        } else if (Fj.equalsIgnoreCase("F8")) {
//            j = 7;
//        } else if (Fj.equalsIgnoreCase("F9")) {
//            j = 8;
//        } else if (Fj.equalsIgnoreCase("F10")) {
//            j = 9;
//        } else if (Fj.equalsIgnoreCase("F11")) {
//            j = 10;
//        } else if (Fj.equalsIgnoreCase("F12")) {
//            j = 11;
//        } else if (Fj.equalsIgnoreCase("F13")) {
//            j = 12;
//        } else if (Fj.equalsIgnoreCase("F14")) {
//            j = 13;
//        } else if (Fj.equalsIgnoreCase("F15")) {
//            j = 14;
//        } else if (Fj.equalsIgnoreCase("F16")) {
//            j = 15;
//        } else if (Fj.equalsIgnoreCase("F17")) {
//            j = 16;
//        } else if (Fj.equalsIgnoreCase("F18")) {
//            j = 17;
//        } else if (Fj.equalsIgnoreCase("F19")) {
//            j = 18;
//        } else if (Fj.equalsIgnoreCase("F20")) {
//            j = 19;
//        } else if (Fj.equalsIgnoreCase("F21")) {
//            j = 20;
//        } else if (Fj.equalsIgnoreCase("F22")) {
//            j = 21;
//        } else if (Fj.equalsIgnoreCase("F23")) {
//            j = 22;
//        } else if (Fj.equalsIgnoreCase("F24")) {
//            j = 23;
//        } else if (Fj.equalsIgnoreCase("F25")) {
//            j = 24;
//        } else if (Fj.equalsIgnoreCase("F26")) {
//            j = 25;
//        } else if (Fj.equalsIgnoreCase("F27")) {
//            j = 26;
//        } else if (Fj.equalsIgnoreCase("F28")) {
//            j = 27;
//        } else if (Fj.equalsIgnoreCase("F29")) {
//            j = 28;
//        } else if (Fj.equalsIgnoreCase("F30")) {
//            j = 29;
//        } else if (Fj.equalsIgnoreCase("F31")) {
//            j = 30;
//        } else if (Fj.equalsIgnoreCase("F32")) {
//            j = 31;
//        } else if (Fj.equalsIgnoreCase("F33")) {
//            j = 32;
//        } else if (Fj.equalsIgnoreCase("F34")) {
//            j = 33;
//        } else if (Fj.equalsIgnoreCase("F35")) {
//            j = 34;
//        } else if (Fj.equalsIgnoreCase("F36")) {
//            j = 35;
//        } else if (Fj.equalsIgnoreCase("F37")) {
//            j = 36;
//        } else if (Fj.equalsIgnoreCase("F38")) {
//            j = 37;
//        } else if (Fj.equalsIgnoreCase("F39")) {
//            j = 38;
//        } else if (Fj.equalsIgnoreCase("F40")) {
//            j = 39;
//        } else if (Fj.equalsIgnoreCase("F41")) {
//            j = 40;
//        } else if (Fj.equalsIgnoreCase("F42")) {
//            j = 41;
//        } else if (Fj.equalsIgnoreCase("F43")) {
//            j = 42;
//        } else if (Fj.equalsIgnoreCase("F44")) {
//            j = 43;
//        } else if (Fj.equalsIgnoreCase("F45")) {
//            j = 44;
//        } else if (Fj.equalsIgnoreCase("F46")) {
//            j = 45;
//        } else if (Fj.equalsIgnoreCase("F47")) {
//            j = 46;
//        } else if (Fj.equalsIgnoreCase("F48")) {
//            j = 47;
//        } else if (Fj.equalsIgnoreCase("F49")) {
//            j = 48;
//        } else if (Fj.equalsIgnoreCase("F50")) {
//            j = 49;
//        } else if (Fj.equalsIgnoreCase("F51")) {
//            j = 50;
//        } else if (Fj.equalsIgnoreCase("F52")) {
//            j = 51;
//        } else if (Fj.equalsIgnoreCase("F53")) {
//            j = 52;
//        } else if (Fj.equalsIgnoreCase("F54")) {
//            j = 53;
//        } else if (Fj.equalsIgnoreCase("F55")) {
//            j = 54;
//        }
//
//        int[][] data = {
//                {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//                {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//                {1, 1, 0, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 0, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//                {1, 1, 1, 1, 2, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//                {2, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
//                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1},
//                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1},
//                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1},
//                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1},
//                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1},
//                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1},
//                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1},
//                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
//                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}
//        };
//
//        int value = 0;
//
//        for (int baris = 0; baris < data.length; baris++) {
//            for (int kolom = 0; kolom < data[baris].length; kolom++) {
//                value = data[i][j];
//            }
//        }
//
//        if (BuildConfig.DEBUG) {
//            Log.d("Log matrik i int : ", String.valueOf(i));
//            Log.d("Log matrik j int : ", String.valueOf(j));
//            Log.d("Log value : ", String.valueOf(value));
//            Log.d("Log angkot : ", angkot);
//            Log.d("Log jalan awal : ", namaJalanAwal);
//            Log.d("Log jalan akhir : ", namaJalanTujuan);
//            Log.d("Log DN : ", startPoint_Dn);
//            Log.d("Log DN : ", endPoint_DN);
//        }
//
//        if (angkot.equalsIgnoreCase("M1") && angkot2.equalsIgnoreCase("M1")) {
//            namaAngkot = "AL";
//        } else if (angkot.equalsIgnoreCase("M2") && angkot2.equalsIgnoreCase("M2")) {
//            namaAngkot = "LG";
//        } else if (angkot.equalsIgnoreCase("M3") && angkot2.equalsIgnoreCase("M3")) {
//            namaAngkot = "AG";
//        }
//
//        if (i == j) {
//            hasil.setVisibility(View.VISIBLE);
//            textHasil.setText(getString(R.string.text_posisi_sama));
//        } else if (value == 1) {
//            if (startPoint_Dn.equalsIgnoreCase(endPoint_DN)) {
//                hasil.setVisibility(View.VISIBLE);
//                textHasil.setText("Untuk jalur dari " + namaJalanAwal + " ke " + namaJalanTujuan + ", Anda bisa memilih Angkot " + namaAngkot);
//            } else {
//                if (BuildConfig.DEBUG) {
//                    Log.e("Log DN", "DN tidak ada");
//                }
//            }
//        } else if (value == 2) {
//
//            Log.e("Log Value", "Value adalah 2");
//
//            if ((i == 1) && (j == 1)) {
//                int m1;
//
//            }
//
//        }

//    }

    public class AdapterAwal {
        Context context;
        double latAwal, lngAwal;
        String namaJalanSatu;
        String nodeSatu;

        public AdapterAwal(Context context, double latAwal, double lngAwal, String namaJalan, String node) {
            this.context = context;
            this.latAwal = latAwal;
            this.lngAwal = lngAwal;
            this.namaJalanSatu = namaJalan;
            this.nodeSatu = node;
        }

        public void toHitung(Algoritma hitungAlgoritma) {
            hitungAlgoritma.TitikAwal(latAwal, lngAwal, namaJalanSatu, nodeSatu);
        }

    }

    public class AdapterAkhir {
        Context context;
        double latAkhir, lngAkhir;
        String namaJalanDua;
        String nodeDua;

        public AdapterAkhir(double latAkhir, double lngAkhir, String namaJalan, String node, Context context) {
            this.context = context;
            this.latAkhir = latAkhir;
            this.lngAkhir = lngAkhir;
            this.namaJalanDua = namaJalan;
            this.nodeDua = node;
        }

        public void toHitung(Algoritma hitungAlgoritma) {
            hitungAlgoritma.TitikAkhir(latAkhir, lngAkhir, namaJalanDua, nodeDua);
        }
    }

    public class Algoritma {

        double latAkhir, lngAkhir;
        double latAwal, lngAwal;
        String namaJalanSatu, namaJalanDua;
        String nodeSatu, nodeDua;

        public Algoritma() {
        }

        public void TitikAwal(double latAwal, double lngAwal, String namaJalan, String node) {
            this.latAwal = latAwal;
            this.lngAwal = lngAwal;
            this.namaJalanSatu = namaJalan;
            this.nodeSatu = node;
        }

        public void TitikAkhir(double latAkhir, double lngAkhir, String namaJalan, String node) {
            this.latAkhir = latAkhir;
            this.lngAkhir = lngAkhir;
            this.namaJalanDua = namaJalan;
            this.nodeDua = node;
        }

        public void cari(Context context) {
            Toast.makeText(context, "Node 1 : " + nodeSatu + "\nNode 2 : " + nodeDua + "\nJalan 1 : " + namaJalanSatu + "\nJalan 2 : " + namaJalanDua, Toast.LENGTH_LONG).show();
        }

        public void algoritmaPencarian() {

            final Call<KodeMirolet> kodeJalur = apiService.kodeJalur();
            kodeJalur.enqueue(new Callback<KodeMirolet>() {
                @Override
                public void onResponse(Call<KodeMirolet> call, Response<KodeMirolet> response) {
                    kodeJalurs = new ArrayList<KodeMirolet.KodeJalur>(response.body().getKode_jalur_angkot());

                    String start_point_dn = "", end_point_dn = "", namaAngkot = "";

                    int indexNodeStart = 0;
                    int indexNodeEnd = 0;

                    if (nodeSatu.equalsIgnoreCase("F1")) {
                        indexNodeStart = 0;
                    } else if (nodeSatu.equalsIgnoreCase("F2")) {
                        indexNodeStart = 1;
                    } else if (nodeSatu.equalsIgnoreCase("F3")) {
                        indexNodeStart = 2;
                    } else if (nodeSatu.equalsIgnoreCase("F4")) {
                        indexNodeStart = 3;
                    } else if (nodeSatu.equalsIgnoreCase("F5")) {
                        indexNodeStart = 4;
                    } else if (nodeSatu.equalsIgnoreCase("F6")) {
                        indexNodeStart = 5;
                    } else if (nodeSatu.equalsIgnoreCase("F7")) {
                        indexNodeStart = 6;
                    } else if (nodeSatu.equalsIgnoreCase("F8")) {
                        indexNodeStart = 7;
                    } else if (nodeSatu.equalsIgnoreCase("F9")) {
                        indexNodeStart = 8;
                    } else if (nodeSatu.equalsIgnoreCase("F10")) {
                        indexNodeStart = 9;
                    } else if (nodeSatu.equalsIgnoreCase("F11")) {
                        indexNodeStart = 10;
                    } else if (nodeSatu.equalsIgnoreCase("F12")) {
                        indexNodeStart = 11;
                    } else if (nodeSatu.equalsIgnoreCase("F13")) {
                        indexNodeStart = 12;
                    } else if (nodeSatu.equalsIgnoreCase("F14")) {
                        indexNodeStart = 13;
                    } else if (nodeSatu.equalsIgnoreCase("F15")) {
                        indexNodeStart = 14;
                    } else if (nodeSatu.equalsIgnoreCase("F16")) {
                        indexNodeStart = 15;
                    } else if (nodeSatu.equalsIgnoreCase("F17")) {
                        indexNodeStart = 16;
                    } else if (nodeSatu.equalsIgnoreCase("F18")) {
                        indexNodeStart = 17;
                    } else if (nodeSatu.equalsIgnoreCase("F19")) {
                        indexNodeStart = 18;
                    } else if (nodeSatu.equalsIgnoreCase("F20")) {
                        indexNodeStart = 19;
                    } else if (nodeSatu.equalsIgnoreCase("F21")) {
                        indexNodeStart = 20;
                    } else if (nodeSatu.equalsIgnoreCase("F22")) {
                        indexNodeStart = 21;
                    } else if (nodeSatu.equalsIgnoreCase("F23")) {
                        indexNodeStart = 22;
                    } else if (nodeSatu.equalsIgnoreCase("F24")) {
                        indexNodeStart = 23;
                    } else if (nodeSatu.equalsIgnoreCase("F25")) {
                        indexNodeStart = 24;
                    } else if (nodeSatu.equalsIgnoreCase("F26")) {
                        indexNodeStart = 25;
                    } else if (nodeSatu.equalsIgnoreCase("F27")) {
                        indexNodeStart = 26;
                    } else if (nodeSatu.equalsIgnoreCase("F28")) {
                        indexNodeStart = 27;
                    } else if (nodeSatu.equalsIgnoreCase("F29")) {
                        indexNodeStart = 28;
                    } else if (nodeSatu.equalsIgnoreCase("F30")) {
                        indexNodeStart = 29;
                    } else if (nodeSatu.equalsIgnoreCase("F31")) {
                        indexNodeStart = 30;
                    } else if (nodeSatu.equalsIgnoreCase("F32")) {
                        indexNodeStart = 31;
                    } else if (nodeSatu.equalsIgnoreCase("F33")) {
                        indexNodeStart = 32;
                    } else if (nodeSatu.equalsIgnoreCase("F34")) {
                        indexNodeStart = 33;
                    } else if (nodeSatu.equalsIgnoreCase("F35")) {
                        indexNodeStart = 34;
                    } else if (nodeSatu.equalsIgnoreCase("F36")) {
                        indexNodeStart = 35;
                    } else if (nodeSatu.equalsIgnoreCase("F37")) {
                        indexNodeStart = 36;
                    } else if (nodeSatu.equalsIgnoreCase("F38")) {
                        indexNodeStart = 37;
                    } else if (nodeSatu.equalsIgnoreCase("F39")) {
                        indexNodeStart = 38;
                    } else if (nodeSatu.equalsIgnoreCase("F40")) {
                        indexNodeStart = 39;
                    } else if (nodeSatu.equalsIgnoreCase("F41")) {
                        indexNodeStart = 40;
                    } else if (nodeSatu.equalsIgnoreCase("F42")) {
                        indexNodeStart = 41;
                    } else if (nodeSatu.equalsIgnoreCase("F43")) {
                        indexNodeStart = 42;
                    } else if (nodeSatu.equalsIgnoreCase("F44")) {
                        indexNodeStart = 43;
                    } else if (nodeSatu.equalsIgnoreCase("F45")) {
                        indexNodeStart = 44;
                    } else if (nodeSatu.equalsIgnoreCase("F46")) {
                        indexNodeStart = 45;
                    } else if (nodeSatu.equalsIgnoreCase("F47")) {
                        indexNodeStart = 46;
                    } else if (nodeSatu.equalsIgnoreCase("F48")) {
                        indexNodeStart = 47;
                    } else if (nodeSatu.equalsIgnoreCase("F49")) {
                        indexNodeStart = 48;
                    } else if (nodeSatu.equalsIgnoreCase("F50")) {
                        indexNodeStart = 49;
                    } else if (nodeSatu.equalsIgnoreCase("F51")) {
                        indexNodeStart = 50;
                    } else if (nodeSatu.equalsIgnoreCase("F52")) {
                        indexNodeStart = 51;
                    } else if (nodeSatu.equalsIgnoreCase("F53")) {
                        indexNodeStart = 52;
                    } else if (nodeSatu.equalsIgnoreCase("F54")) {
                        indexNodeStart = 53;
                    } else if (nodeSatu.equalsIgnoreCase("F55")) {
                        indexNodeStart = 54;
                    }

                    if (nodeDua.equalsIgnoreCase("F1")) {
                        indexNodeEnd = 0;
                    } else if (nodeDua.equalsIgnoreCase("F2")) {
                        indexNodeEnd = 1;
                    } else if (nodeDua.equalsIgnoreCase("F3")) {
                        indexNodeEnd = 2;
                    } else if (nodeDua.equalsIgnoreCase("F4")) {
                        indexNodeEnd = 3;
                    } else if (nodeDua.equalsIgnoreCase("F5")) {
                        indexNodeEnd = 4;
                    } else if (nodeDua.equalsIgnoreCase("F6")) {
                        indexNodeEnd = 5;
                    } else if (nodeDua.equalsIgnoreCase("F7")) {
                        indexNodeEnd = 6;
                    } else if (nodeDua.equalsIgnoreCase("F8")) {
                        indexNodeEnd = 7;
                    } else if (nodeDua.equalsIgnoreCase("F9")) {
                        indexNodeEnd = 8;
                    } else if (nodeDua.equalsIgnoreCase("F10")) {
                        indexNodeEnd = 9;
                    } else if (nodeDua.equalsIgnoreCase("F11")) {
                        indexNodeEnd = 10;
                    } else if (nodeDua.equalsIgnoreCase("F12")) {
                        indexNodeEnd = 11;
                    } else if (nodeDua.equalsIgnoreCase("F13")) {
                        indexNodeEnd = 12;
                    } else if (nodeDua.equalsIgnoreCase("F14")) {
                        indexNodeEnd = 13;
                    } else if (nodeDua.equalsIgnoreCase("F15")) {
                        indexNodeEnd = 14;
                    } else if (nodeDua.equalsIgnoreCase("F16")) {
                        indexNodeEnd = 15;
                    } else if (nodeDua.equalsIgnoreCase("F17")) {
                        indexNodeEnd = 16;
                    } else if (nodeDua.equalsIgnoreCase("F18")) {
                        indexNodeEnd = 17;
                    } else if (nodeDua.equalsIgnoreCase("F19")) {
                        indexNodeEnd = 18;
                    } else if (nodeDua.equalsIgnoreCase("F20")) {
                        indexNodeEnd = 19;
                    } else if (nodeDua.equalsIgnoreCase("F21")) {
                        indexNodeEnd = 20;
                    } else if (nodeDua.equalsIgnoreCase("F22")) {
                        indexNodeEnd = 21;
                    } else if (nodeDua.equalsIgnoreCase("F23")) {
                        indexNodeEnd = 22;
                    } else if (nodeDua.equalsIgnoreCase("F24")) {
                        indexNodeEnd = 23;
                    } else if (nodeDua.equalsIgnoreCase("F25")) {
                        indexNodeEnd = 24;
                    } else if (nodeDua.equalsIgnoreCase("F26")) {
                        indexNodeEnd = 25;
                    } else if (nodeDua.equalsIgnoreCase("F27")) {
                        indexNodeEnd = 26;
                    } else if (nodeDua.equalsIgnoreCase("F28")) {
                        indexNodeEnd = 27;
                    } else if (nodeDua.equalsIgnoreCase("F29")) {
                        indexNodeEnd = 28;
                    } else if (nodeDua.equalsIgnoreCase("F30")) {
                        indexNodeEnd = 29;
                    } else if (nodeDua.equalsIgnoreCase("F31")) {
                        indexNodeEnd = 30;
                    } else if (nodeDua.equalsIgnoreCase("F32")) {
                        indexNodeEnd = 31;
                    } else if (nodeDua.equalsIgnoreCase("F33")) {
                        indexNodeEnd = 32;
                    } else if (nodeDua.equalsIgnoreCase("F34")) {
                        indexNodeEnd = 33;
                    } else if (nodeDua.equalsIgnoreCase("F35")) {
                        indexNodeEnd = 34;
                    } else if (nodeDua.equalsIgnoreCase("F36")) {
                        indexNodeEnd = 35;
                    } else if (nodeDua.equalsIgnoreCase("F37")) {
                        indexNodeEnd = 36;
                    } else if (nodeDua.equalsIgnoreCase("F38")) {
                        indexNodeEnd = 37;
                    } else if (nodeDua.equalsIgnoreCase("F39")) {
                        indexNodeEnd = 38;
                    } else if (nodeDua.equalsIgnoreCase("F40")) {
                        indexNodeEnd = 39;
                    } else if (nodeDua.equalsIgnoreCase("F41")) {
                        indexNodeEnd = 40;
                    } else if (nodeDua.equalsIgnoreCase("F42")) {
                        indexNodeEnd = 41;
                    } else if (nodeDua.equalsIgnoreCase("F43")) {
                        indexNodeEnd = 42;
                    } else if (nodeDua.equalsIgnoreCase("F44")) {
                        indexNodeEnd = 43;
                    } else if (nodeDua.equalsIgnoreCase("F45")) {
                        indexNodeEnd = 44;
                    } else if (nodeDua.equalsIgnoreCase("F46")) {
                        indexNodeEnd = 45;
                    } else if (nodeDua.equalsIgnoreCase("F47")) {
                        indexNodeEnd = 46;
                    } else if (nodeDua.equalsIgnoreCase("F48")) {
                        indexNodeEnd = 47;
                    } else if (nodeDua.equalsIgnoreCase("F49")) {
                        indexNodeEnd = 48;
                    } else if (nodeDua.equalsIgnoreCase("F50")) {
                        indexNodeEnd = 49;
                    } else if (nodeDua.equalsIgnoreCase("F51")) {
                        indexNodeEnd = 50;
                    } else if (nodeDua.equalsIgnoreCase("F52")) {
                        indexNodeEnd = 51;
                    } else if (nodeDua.equalsIgnoreCase("F53")) {
                        indexNodeEnd = 52;
                    } else if (nodeDua.equalsIgnoreCase("F54")) {
                        indexNodeEnd = 53;
                    } else if (nodeDua.equalsIgnoreCase("F55")) {
                        indexNodeEnd = 54;
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
                            value = data[indexNodeStart][indexNodeEnd];
                        }
                    }

//                    if (angkot.equalsIgnoreCase("M1") && angkot2.equalsIgnoreCase("M1")) {
//                        namaAngkot = "AL";
//                    } else if (angkot.equalsIgnoreCase("M2") && angkot2.equalsIgnoreCase("M2")) {
//                        namaAngkot = "LG";
//                    } else if (angkot.equalsIgnoreCase("M3") && angkot2.equalsIgnoreCase("M3")) {
//                        namaAngkot = "AG";
//                    }

                    for (int i = 1; i < kodeJalurs.size(); i++) {

                        String nodeJalur = kodeJalurs.get(i).getNode_jalur();
                        String dn = kodeJalurs.get(i).getKode_jalur();
                        String angkot = kodeJalurs.get(i).getKode_mikrolet();

                        if (angkot.equals("M1")) {
                            namaAngkot = "AL";
                        } else if (angkot.equals("M2")) {
                            namaAngkot = "LG";
                        } else if (angkot.equals("M3")) {
                            namaAngkot = "AG";
                        }

                        if (nodeSatu.equalsIgnoreCase(nodeJalur)) {
                            start_point_dn = dn;

                            Log.d(TAG, "DN start : " + start_point_dn);
                            break;
                        }

                        if (nodeDua.equalsIgnoreCase(nodeJalur)) {
                            end_point_dn = dn;

                            Log.d(TAG, "DN end : " + end_point_dn);
                            break;
                        }

                        if (indexNodeStart == indexNodeEnd) {
                            hasil.setVisibility(View.VISIBLE);
                            textHasil.setText(getString(R.string.text_posisi_sama));
                        } else if (value == 1) {
                            if (start_point_dn.equalsIgnoreCase(end_point_dn)) {

                                hasil.setVisibility(View.VISIBLE);
                                textHasil.setText("Untuk jalur dari " + namaJalanSatu + " ke " + namaJalanDua + ", Anda bisa memilih Angkot " + namaAngkot);
                            } else {
                                hasil.setVisibility(View.VISIBLE);
                                textHasil.setText("DN tidak cocok");
                                if (BuildConfig.DEBUG) {
                                    Log.e("Log DN", "DN tidak ada");
                                }
                            }
                        } else if (value == 2) {
                            hasil.setVisibility(View.VISIBLE);
                            textHasil.setText("Value = 2");

                            Log.e("Log Value", "Value adalah 2");

                            if ((indexNodeStart == 1) && (indexNodeEnd == 1)) {
                                int m1;

                            }

                        }
                    }

                    if (BuildConfig.DEBUG) {
                        Log.d("Log matrik i int : ", String.valueOf(indexNodeStart));
                        Log.d("Log matrik j int : ", String.valueOf(indexNodeEnd));
                        Log.d("Log value : ", String.valueOf(value));
//                        Log.d("Log angkot : ", angkot);
                        Log.d("Log jalan awal : ", namaJalanSatu);
                        Log.d("Log jalan akhir : ", namaJalanDua);
                        Log.d("Log DN : ", start_point_dn);
                        Log.d("Log DN : ", end_point_dn);
                    }

                }

                @Override
                public void onFailure(Call<KodeMirolet> call, Throwable t) {

                }
            });
        }
    }
}
