package com.example.whitehat.mikroletmalang.entity.model;

import java.util.List;

/**
 * Created by rofiqoff on 9/20/17.
 */

public class JalurAngkot {
    String result;
    String msg;
    List<Jalur> jalur;

    public String getResult() {
        return result;
    }

    public String getMsg() {
        return msg;
    }

    public List<Jalur> getJalur() {
        return jalur;
    }

    public class Jalur {
        String id;
        String nama_jalan;
        String node_jalur;
        String lat;
        String lng;

        public String getId() {
            return id;
        }

        public String getNama_jalan() {
            return nama_jalan;
        }

        public String getNode_jalur() {
            return node_jalur;
        }

        public String getLat() {
            return lat;
        }

        public String getLng() {
            return lng;
        }
    }
}
