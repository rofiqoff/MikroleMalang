package com.example.whitehat.mikroletmalang.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.whitehat.mikroletmalang.R;
import com.example.whitehat.mikroletmalang.support.Helper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPolygon;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
//import com.google.android.gms.vision.text.Text;

public class DetailJenis extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mGoogleMap;
    private TextView textTrayek;
    private ImageView mGambar;
    private TextView mNama;

    KmlLayer kmlLayer;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_jenis);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textTrayek = (TextView) findViewById(R.id.textJalur);
        mNama = (TextView) findViewById(R.id.text_jenis_detail);
        mGambar = (ImageView) findViewById(R.id.image_detail_jenis);

        bundle = getIntent().getExtras();

        String namaAngkot = "Angkot "+bundle.getString("namaAngkot");

        textTrayek.setText(bundle.getString("trayek"));
        mNama.setText(bundle.getString("nama2"));
        String sGambar = bundle.getString("gambar");

        getSupportActionBar().setTitle(namaAngkot);

        Glide.with(this)
                .load(Helper.BASE_URL + sGambar)
                .placeholder(getResources().getColor(R.color.bg_layout))
                .into(mGambar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_detail_jenis);
        mapFragment.getMapAsync(this);
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
        double lat = -7.982449;
        double lng = 112.630712;
        int zoom = 13;

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom);
        mGoogleMap.moveCamera(cameraUpdate);

        retrieveFileFromResource();

    }

    private void retrieveFileFromResource() {
        try {
            kmlLayer = new KmlLayer(mGoogleMap, bundle.getInt("peta"), getApplicationContext());
            kmlLayer.addLayerToMap();
            moveCameraToKml(kmlLayer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private void moveCameraToKml(KmlLayer kmlLayer) {
        //Retrieve the first container in the KML layer
        try {


            KmlContainer container = kmlLayer
                    .getContainers()
                    .iterator()
                    .next();
            //Retrieve a nested container within the first container
            container = container.getContainers().iterator().next();
            //Retrieve the first placemark in the nested container
            KmlPlacemark placemark = container.getPlacemarks().iterator().next();
            //Retrieve a polygon object in a placemark
            KmlPolygon polygon = (KmlPolygon) placemark.getGeometry();
            //Create LatLngBounds of the outer coordinates of the polygon
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : polygon.getOuterBoundaryCoordinates()) {
                builder.include(latLng);
            }

            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, 1));

        } catch (Exception e) {

        }
    }
}
