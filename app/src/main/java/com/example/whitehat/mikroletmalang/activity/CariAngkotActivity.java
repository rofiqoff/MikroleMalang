package com.example.whitehat.mikroletmalang.activity;

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
import com.example.whitehat.mikroletmalang.service.APIService;
import com.example.whitehat.mikroletmalang.entity.model.AngkotAGModel;
import com.example.whitehat.mikroletmalang.entity.model.AngkotALModel;
import com.example.whitehat.mikroletmalang.entity.model.AngkotLGModel;
import com.example.whitehat.mikroletmalang.entity.model.JalurAngkot;
import com.example.whitehat.mikroletmalang.entity.model.JalurAngkotModel;
import com.example.whitehat.mikroletmalang.entity.model.KodeMirolet;
import com.example.whitehat.mikroletmalang.support.JalurAdapterAwal;
import com.example.whitehat.mikroletmalang.support.JalurAdapterTujuan;
import com.example.whitehat.mikroletmalang.support.GeocoderIntentService;
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

    public class AdapterAwal {
        Context context;
        double latAwal, lngAwal;
        String namaJalanSatu;
        String nodeSatu;
        String namaAngkotSatu;

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
        String namaAngkotDua;

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
        String namaAngkotSatu, namaAngkotDua;

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
                            {1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
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

                        Log.d(TAG, "nama Angkot : " + namaAngkot);

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

                                Log.d(TAG, "nama Angkot : " + namaAngkot);

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
                                if (nodeJalur == nodeJalur) {

                                }

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
