package com.example.bgpm.bean;

import java.util.HashSet;
import java.math.BigInteger;

public class AsLineBean {
    private String begin;
    private String end;
    private BigInteger ipSize;
    private int k;

    public AsLineBean() {
    }

    public AsLineBean(String begin, String end, BigInteger ipSize) {
        this.begin = begin;
        this.end = end;
        this.ipSize = ipSize;
    }

    public AsLineBean(String begin, String end, BigInteger ipSize, int k) {
        this.begin = begin;
        this.end = end;
        this.ipSize = ipSize;
        this.k = k;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public BigInteger getIpSize() {
        return ipSize;
    }

    public void setIpSize(BigInteger ipSize) {
        this.ipSize = ipSize;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    @Override
    public String toString() {
        return "AsLineBean{" +
                "begin='" + begin + '\'' +
                ", end='" + end + '\'' +
                ", ipSize=" + ipSize +
                ", k=" + k +
                '}';
    }
}
