package com.example.bgpm.bean;

import java.math.BigInteger;
import java.util.List;

public class AsPathInfoBean {
    private List<String> asPath;
    private List<String> country;
    private List<String> prefix;
    private BigInteger ipSize;

    public AsPathInfoBean() {
        super();
    }

    public void setAsPath(List<String> asPath) {
        this.asPath = asPath;
    }

    public List<String> getAsPath() {
        return asPath;
    }

    public void setCountry(List<String> country) {
        this.country = country;
    }

    public List<String> getCountry() {
        return country;
    }

    public void setPrefix(List<String> prefix) {
        this.prefix = prefix;
    }

    public List<String> getPrefix() {
        return prefix;
    }

    public void setIpSize(BigInteger ipSize) {
        this.ipSize = ipSize;
    }

    public BigInteger getIpSize() {
        return ipSize;
    }

    @Override
    public String toString() {
        return "AsPathInfoBean{" +
                "asPath=" + asPath +
                ", country=" + country +
                ", prefix=" + prefix +
                ", ipSize=" + ipSize +
                '}';
    }
}
