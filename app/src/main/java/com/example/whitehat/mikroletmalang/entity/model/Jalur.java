package com.example.whitehat.mikroletmalang.entity.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by rofiqoff on 9/21/17.
 */

public class Jalur {

    @SerializedName("id_jalur")
    @Expose
    private String idJalur;
    @SerializedName("nama_jalan")
    @Expose
    private String namaJalan;
    @SerializedName("node_jalur")
    @Expose
    private String nodeJalur;
    @SerializedName("lat")
    @Expose
    private String lat;
    @SerializedName("lng")
    @Expose
    private String lng;

    public String getIdJalur() {
        return idJalur;
    }

    public void setIdJalur(String idJalur) {
        this.idJalur = idJalur;
    }

    public String getNamaJalan() {
        return namaJalan;
    }

    public void setNamaJalan(String namaJalan) {
        this.namaJalan = namaJalan;
    }

    public String getNodeJalur() {
        return nodeJalur;
    }

    public void setNodeJalur(String nodeJalur) {
        this.nodeJalur = nodeJalur;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

}
