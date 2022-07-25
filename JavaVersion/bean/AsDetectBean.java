package com.example.bgpm.bean;

public class AsDetectBean {
    private String begin;
    private String line;
    private String ip;
    private int pow;

    public AsDetectBean(){};
    public AsDetectBean(String begin, String line, String ip, int pow){
        this.begin = begin;
        this.line = line;
        this.ip = ip;
        this.pow = pow;
    };

    public String getStartAsId() {
        return begin;
    }

    public void setStartAsId(String begin) {
        this.begin = begin;
    }

    public String getAsList() {
        return line;
    }

    public void setAsList(String line) {
        this.line = line;
    }

    public String getIpArea() {
        return ip;
    }

    public void setIpArea(String ip) {
        this.ip = ip;
    }

    public int getPowSize() {
        return pow;
    }

    public void setPowSize(int pow) {
        this.pow = pow;
    }

    @Override
    public String toString() {
        return "AsDetectData{" +
                "begin='" + begin + '\'' +
                ", line=" + line +
                ", ip='" + ip + '\'' +
                ", pow=" + pow +
                '}';
    }
}
