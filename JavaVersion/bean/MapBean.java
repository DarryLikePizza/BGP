package com.example.bgpm.bean;

import java.util.List;

public class MapBean {
    private int id;
    private List<String> asList;
    private List<Double> value;
    private String country;
    private String province;
    private String city;
    private int coefficient;

    public MapBean(int id, List<String> asList, List<Double> value, String country, String province, String city, int coefficient) {
        this.id = id;
        this.asList = asList;
        this.value = value;
        this.country = country;
        this.province = province;
        this.city = city;
        this.coefficient = coefficient;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getAsList() {
        return asList;
    }

    public void setAsList(List<String> asList) {
        this.asList = asList;
    }

    public List<Double> getValue() {
        return value;
    }

    public void setValue(List<Double> value) {
        this.value = value;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(int coefficient) {
        this.coefficient = coefficient;
    }

    @Override
    public String toString() {
        return "MapBean{" +
                "id=" + id +
                ", asList=" + asList +
                ", value=" + value +
                ", country='" + country + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", coefficient=" + coefficient +
                '}';
    }
}
