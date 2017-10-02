package com.example.whitehat.mikroletmalang.entity.model;

import java.util.List;

/**
 * Created by rofiqoff on 9/27/17.
 */

public class KodeMirolet {
    String result;
    String msg;
    List<KodeJalur> kode_jalur_angkot;

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

    public List<KodeJalur> getKode_jalur_angkot() {
        return kode_jalur_angkot;
    }

    public void setKode_jalur_angkot(List<KodeJalur> kode_jalur_angkot) {
        this.kode_jalur_angkot = kode_jalur_angkot;
    }

    public class KodeJalur {
        String id_kode;
        String kode_jalur;
        String nama_jalan;
        String node_jalur;
        String kode_mikrolet;

        public String getId_kode() {
            return id_kode;
        }

        public void setId_kode(String id_kode) {
            this.id_kode = id_kode;
        }

        public String getKode_jalur() {
            return kode_jalur;
        }

        public void setKode_jalur(String kode_jalur) {
            this.kode_jalur = kode_jalur;
        }

        public String getNama_jalan() {
            return nama_jalan;
        }

        public void setNama_jalan(String nama_jalan) {
            this.nama_jalan = nama_jalan;
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
    }

}
