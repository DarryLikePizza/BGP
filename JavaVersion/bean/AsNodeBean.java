package com.example.bgpm.bean;

public class AsNodeBean {
    private String asId;
    private String nationName;
    private String longitude;  // 经度
    private String latitude;  // 纬度
    private long degree=0;  //节点入度

    public AsNodeBean() {
    }

    public AsNodeBean(String asId, String nationName, String longitude, String latitude) {
        this.asId = asId;
        this.nationName = nationName;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public AsNodeBean(String asId, String nationName, String longitude, String latitude, long degree) {
        this.asId = asId;
        this.nationName = nationName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.degree = degree;
    }

    public String getAsId() {
        return asId;
    }

    public void setAsId(String asId) {
        this.asId = asId;
    }

    public String getNationName() {
        return nationName;
    }

    public void setNationName(String nationName) {
        this.nationName = nationName;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public long getDegree() {
        return degree;
    }

    public void setDegree(long degree) {
        this.degree = degree;
    }

    @Override
    public String toString() {
        return "asInformationData{" +
                "asId='" + asId + '\'' +
                ", nationName='" + nationName + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                '}';
    }
}
