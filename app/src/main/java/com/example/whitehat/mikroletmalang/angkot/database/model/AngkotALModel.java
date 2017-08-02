package com.example.whitehat.mikroletmalang.angkot.database.model;

import java.util.List;

/**
 * Created by rofiqoff on 8/1/17.
 */

public class AngkotALModel {
    String result;
    String msg;
    List<MikroletAl> mikrolet_al;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<MikroletAl> getMikrolet_al() {
        return mikrolet_al;
    }

    public void setMikrolet_al(List<MikroletAl> mikrolet_al) {
        this.mikrolet_al = mikrolet_al;
    }

    public class MikroletAl {
        String kode_mikrolet;
        String jenis_mikrolet;
        String tujuan_mikrolet;
        String gambar_mikrolet;
        String node_jalur_awal;
        String lat_awal;
        String long_awal;
        String node_jalur_akhir;
        String lat_akhir;
        String node_jalur;
        String lat_jalur;
        String long_jalur;

        public String getKode_mikrolet() {
            return kode_mikrolet;
        }

        public void setKode_mikrolet(String kode_mikrolet) {
            this.kode_mikrolet = kode_mikrolet;
        }

        public String getJenis_mikrolet() {
            return jenis_mikrolet;
        }

        public void setJenis_mikrolet(String jenis_mikrolet) {
            this.jenis_mikrolet = jenis_mikrolet;
        }

        public String getTujuan_mikrolet() {
            return tujuan_mikrolet;
        }

        public void setTujuan_mikrolet(String tujuan_mikrolet) {
            this.tujuan_mikrolet = tujuan_mikrolet;
        }

        public String getGambar_mikrolet() {
            return gambar_mikrolet;
        }

        public void setGambar_mikrolet(String gambar_mikrolet) {
            this.gambar_mikrolet = gambar_mikrolet;
        }

        public String getNode_jalur_awal() {
            return node_jalur_awal;
        }

        public void setNode_jalur_awal(String node_jalur_awal) {
            this.node_jalur_awal = node_jalur_awal;
        }

        public String getLat_awal() {
            return lat_awal;
        }

        public void setLat_awal(String lat_awal) {
            this.lat_awal = lat_awal;
        }

        public String getLong_awal() {
            return long_awal;
        }

        public void setLong_awal(String long_awal) {
            this.long_awal = long_awal;
        }

        public String getNode_jalur_akhir() {
            return node_jalur_akhir;
        }

        public void setNode_jalur_akhir(String node_jalur_akhir) {
            this.node_jalur_akhir = node_jalur_akhir;
        }

        public String getLat_akhir() {
            return lat_akhir;
        }

        public void setLat_akhir(String lat_akhir) {
            this.lat_akhir = lat_akhir;
        }

        public String getNode_jalur() {
            return node_jalur;
        }

        public void setNode_jalur(String node_jalur) {
            this.node_jalur = node_jalur;
        }

        public String getLat_jalur() {
            return lat_jalur;
        }

        public void setLat_jalur(String lat_jalur) {
            this.lat_jalur = lat_jalur;
        }

        public String getLong_jalur() {
            return long_jalur;
        }

        public void setLong_jalur(String long_jalur) {
            this.long_jalur = long_jalur;
        }
    }
}
