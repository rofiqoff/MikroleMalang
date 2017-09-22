package com.example.whitehat.mikroletmalang.angkot.database.model;

import java.util.List;

/**
 * Created by rofiqoff on 8/1/17.
 */

public class JalurAngkotModel {
    String result;
    String msg;
    List<JalurAngkot> jalur;

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

    public List<JalurAngkot> getJalur() {
        return jalur;
    }

    public void setJalur(List<JalurAngkot> jalur) {
        this.jalur = jalur;
    }

    public class JalurAngkot {
        String id_jalur;
        String node_jalur;
        String kode_mikrolet;
        String lat_jalur;
        String long_jalur;
        String nama_jalan;
        String kode_jalur;

        public String getId_jalur() {
            return id_jalur;
        }

        public void setId_jalur(String id_jalur) {
            this.id_jalur = id_jalur;
        }

        public String getNode_jalur() {
            return node_jalur;
        }

        public void setNode_jalur(String node_jalur) {
            this.node_jalur = node_jalur;
        }

        public String getKode_mikrolet() {
            return kode_mikrolet;
        }

        public void setKode_mikrolet(String kode_mikrolet) {
            this.kode_mikrolet = kode_mikrolet;
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

        public String getNama_jalan() {
            return nama_jalan;
        }

        public void setNama_jalan(String nama_jalan) {
            this.nama_jalan = nama_jalan;
        }

        public String getKode_jalur() {
            return kode_jalur;
        }

        public void setKode_jalur(String kode_jalur) {
            this.kode_jalur = kode_jalur;
        }
    }
}
