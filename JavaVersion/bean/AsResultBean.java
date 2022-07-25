package com.example.bgpm.bean;

import java.util.List;
import java.util.Map;

public class AsResultBean {
    private List<AsLineBean> asOneSkipLineList;
    private List<AsLineBean> asTwoSkipLineList;

    public AsResultBean() {
    }

    public AsResultBean(List<AsLineBean> asOneSkipLineList,  List<AsLineBean> asTwoSkipLineList) {
        this.asOneSkipLineList = asOneSkipLineList;
        this.asTwoSkipLineList = asTwoSkipLineList;
    }



    public List<AsLineBean> getAsOneSkipLineList() {
        return asOneSkipLineList;
    }

    public void setAsOneSkipLineList(List<AsLineBean> asOneSkipLineList) {
        this.asOneSkipLineList = asOneSkipLineList;
    }


    public List<AsLineBean> getAsTwoSkipLineList() {
        return asTwoSkipLineList;
    }

    public void setAsTwoSkipLineList(List<AsLineBean> asTwoSkipLineList) {
        this.asTwoSkipLineList = asTwoSkipLineList;
    }

    @Override
    public String toString() {
        return "AsResultBean{asOneSkipLineList=" + asOneSkipLineList +
                ", asTwoSkipLineList=" + asTwoSkipLineList +
                '}';
    }
}
