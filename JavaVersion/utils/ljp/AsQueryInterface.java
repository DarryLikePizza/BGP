package com.example.bgpm.utils.ljp;

import com.example.bgpm.bean.AsPathInfoBean;

import java.util.*;

public interface AsQueryInterface {
    /*
     * Read the AS information including AS_PATH, Prefix, and Pow from processed csv
     * document.
     */
    void readAsPathFile(String fileName);

    /*
     * Giving an ASN find in the database and return the infomation restricted by
     * some rules.
     */
    List<AsPathInfoBean> twoHopsQuery(String queryAs);
}
