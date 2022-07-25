package com.example.bgpm.utils.ljp;

import java.util.*;

public interface AsCountryInterface {
    /* Read the relationship between AS and country. */
    void readAsCountryFile(String fileName);

    /* Read country, longitude, and latitude information. */
    void readCountryOnlyFile(String fileName);

    /* Get country name by giving the ASN. */
    String getCountryName(String as);

    /* Get the longitude and latitude by given country name. */
    List<String> getCountryInfos(String country);
}