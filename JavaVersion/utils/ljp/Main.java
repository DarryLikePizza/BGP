package com.example.bgpm.utils.ljp;

import com.example.bgpm.bean.AsPathInfoBean;
import com.example.bgpm.utils.ljp.TwoHopsQuery;

import java.util.*;
// import java.util.logging.*;

public class Main {
    // private static Logger logger = Logger.getLogger("TestMain");
    public static void main(String[] args) {
        // String asCountryFileName =
        // "D:\\internship\\task\\data_analysis\\data\\as_country.csv";
        // String countryOnlyFileName =
        // "D:\\internship\\task\\data_analysis\\data\\country.csv";
        String asInfosFileName = "/Users/mac/desktop/snapshot.csv";
        String queryAs = "54574";

        TwoHopsQuery twoHopsQueryObject = new TwoHopsQuery();
        // FindCountryInfos findCountryInfosObject = new FindCountryInfos();

        List<AsPathInfoBean> asSelectList = new ArrayList<>();

        twoHopsQueryObject.readAsPathFile(asInfosFileName);
        asSelectList = twoHopsQueryObject.twoHopsQuery(queryAs);

        for (AsPathInfoBean asInfo : asSelectList) {
            System.out.println("**********************************************");
            System.out.printf("ASN: %s\n", String.join(" - ", asInfo.getAsPath()));
            System.out.printf("Country: %s\n", String.join(" - ", asInfo.getCountry()));
            System.out.printf("Prefix: \n%s\n", String.join("\n",asInfo.getPrefix()));
            System.out.printf("IPSize: %d\n", asInfo.getIpSize());
        }
        System.out.printf("List Length: %s\n", asSelectList.size());
    }
}