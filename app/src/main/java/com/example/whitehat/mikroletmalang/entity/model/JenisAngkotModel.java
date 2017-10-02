package com.example.whitehat.mikroletmalang.entity.model;

import java.util.List;

/**
 * Created by rofiqoff on 8/1/17.
 */

public class JenisAngkotModel {
    String result;
    String msg;
    List<JenisMikrolet> jenis_mikrolet;

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

    public List<JenisMikrolet> getJenis_mikrolet() {
        return jenis_mikrolet;
    }

    public void setJenis_mikrolet(List<JenisMikrolet> jenis_mikrolet) {
        this.jenis_mikrolet = jenis_mikrolet;
    }

    private class JenisMikrolet {
        String kode_mikrolet;
        String jenis_mikrolet;
        String tujuan_mikrolet;
        String gambar_mikrolet;

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
    }
}
