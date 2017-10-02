package com.example.whitehat.mikroletmalang.service;

import com.example.whitehat.mikroletmalang.support.Helper;
import com.example.whitehat.mikroletmalang.entity.model.AngkotAGModel;
import com.example.whitehat.mikroletmalang.entity.model.AngkotALModel;
import com.example.whitehat.mikroletmalang.entity.model.AngkotLGModel;
import com.example.whitehat.mikroletmalang.entity.model.JalurAngkot;
import com.example.whitehat.mikroletmalang.entity.model.JalurAngkotModel;
import com.example.whitehat.mikroletmalang.entity.model.JenisAngkotModel;
import com.example.whitehat.mikroletmalang.entity.model.KodeMirolet;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by rofiqoff on 8/1/17.
 */

public interface APIService {

    @GET("tampil-data-al.php")
    Call<AngkotALModel> getAngkotAl();

    @GET("tampil-data-ag.php")
    Call<AngkotAGModel> getAngkotAG();

    @GET("tampil-data-lg.php")
    Call<AngkotLGModel> getAngkotLG();

    @GET("tampil-jeni-mikrolet.php")
    Call<JenisAngkotModel> jenisAngkot();

    @GET("jalur_angkot.php")
    Call<JalurAngkotModel> jalurAngkot();

    @GET("jalur.php")
    Call<JalurAngkot> jalur();

    @GET("kode_jalur_angkot.php")
    Call<KodeMirolet> kodeJalur();

    class Factory {
        public static APIService create() {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Helper.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            return retrofit.create(APIService.class);
        }
    }
}
