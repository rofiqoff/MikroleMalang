package com.example.whitehat.mikroletmalang.angkot.database.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by rofiqoff on 9/21/17.
 */

public class JalurModel {
    @SerializedName("result")
    @Expose
    private String result;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("jalur")
    @Expose
    private List<Jalur> jalur = null;

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

    public List<Jalur> getJalur() {
        return jalur;
    }

    public void setJalur(List<Jalur> jalur) {
        this.jalur = jalur;
    }
}
