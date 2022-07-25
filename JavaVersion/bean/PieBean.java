package com.example.bgpm.bean;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieBean {
    private String areaName;
    private BigInteger ipCount;
    private HashMap<String, BigInteger> asIpMap;

    public PieBean() {
    }

    public PieBean(String areaName, BigInteger ipCount, HashMap<String, BigInteger> asIpMap) {
        this.areaName = areaName;
        this.ipCount = ipCount;
        this.asIpMap = asIpMap;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public BigInteger getIpCount() {
        return ipCount;
    }

    public void setIpCount(BigInteger ipCount) {
        this.ipCount = ipCount;
    }

    public HashMap<String, BigInteger> getAsIpMap() {
        return asIpMap;
    }

    public void setAsIpMap(HashMap<String, BigInteger> asIpMap) {
        this.asIpMap = asIpMap;
    }

    @Override
    public String toString() {
        return "pieBean{" +
                "areaName='" + areaName + '\'' +
                ", ipCount=" + ipCount +
                ", asList=" + asIpMap +
                '}';
    }

}
