package com.example.bgpm.utils.ljp;

import com.example.bgpm.bean.AsPathInfoBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TwoHopsQuery implements AsQueryInterface {
    // private static Logger logger = Logger.getLogger("TwoHopsQuery");
    private List<AsPathInfoBean> asInfoList = new ArrayList<>();
    private List<AsPathInfoBean> asSelectList = new ArrayList<>();

    public boolean equalList(List<String> list1, List<String> list2) {
        return (list1.size() == list2.size()) && list1.containsAll(list2);
    }

    public BigInteger calPow(int x, int y) {
        BigInteger xB = new BigInteger(String.valueOf(x));
        return xB.pow(y);
    }

    @Override
    public void readAsPathFile(String fileName) {
        if (this.asInfoList.isEmpty()) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.UTF_8)) {
                String DELIMITER = ",";
                String line;
                br.readLine(); // Skip the header
                while ((line = br.readLine()) != null) {
                    AsPathInfoBean asLine = new AsPathInfoBean();
                    List<String> prefix = new ArrayList<>();
                    // [0] for begain AS, [1] for AS Path, [2] for prefix, [3] for pow
                    String[] columns = line.split(DELIMITER);

                    asLine.setAsPath(Arrays.asList(columns[1].split(" ")));
                    prefix.add(columns[2]);
                    asLine.setPrefix(prefix);
                    asLine.setIpSize(calPow(2, Integer.parseInt(columns[3])));

                    this.asInfoList.add(asLine);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void checkInfoExist(AsPathInfoBean asInfo) {
        AsPathInfoBean asSelectInfo = new AsPathInfoBean();
        // calculate the pow
        for (int i = 0; i < this.asSelectList.size(); i++) {
            List<String> asPrefixList = new ArrayList<>();
            BigInteger allAsIpSize;
            asSelectInfo = this.asSelectList.get(i);
            // judge if two AS_PATH are same
            if (equalList(asSelectInfo.getAsPath(), asInfo.getAsPath())) {
                // if the AS_PATH, prefix, and masks are all same then skip
                asPrefixList = asSelectInfo.getPrefix();
                if (asPrefixList.contains(asInfo.getPrefix().get(0))) {
                    return;
                }
                // if the AS_PATH are prefix are same but mask are different
                // then kip the small mask prefix
                allAsIpSize = asSelectInfo.getIpSize();
                String[] prefixAsInfo = asInfo.getPrefix().get(0).split("/");
                for (int j = 0; j < asPrefixList.size(); j++) {
                    String[] prefixSelect = asPrefixList.get(j).split("/");
                    if (prefixSelect[0].equals(prefixAsInfo[0])) {
                        
                        if (Integer.parseInt(prefixSelect[1]) > Integer.parseInt(prefixAsInfo[1])) {
                            asPrefixList.set(j, asInfo.getPrefix().get(0));
                            allAsIpSize = allAsIpSize.add(calPow(2, 32-Integer.parseInt(prefixSelect[1])));
                            allAsIpSize = allAsIpSize.add(asInfo.getIpSize());
                        }

                        asSelectInfo.setPrefix(asPrefixList);
                        asSelectInfo.setIpSize(allAsIpSize);
                        this.asSelectList.set(i, asSelectInfo);
                        return;
                    }
                }
                // if the AS_PATH are same, but prefix and mask are all different
                // then add prefix and mask, also calculate the IP size
                asPrefixList.add(asInfo.getPrefix().get(0));
                allAsIpSize = allAsIpSize.add(asInfo.getIpSize());
                asSelectInfo.setPrefix(asPrefixList);
                asSelectInfo.setIpSize(allAsIpSize);
                this.asSelectList.set(i, asSelectInfo);
                return;
            }
        }
        this.asSelectList.add(asInfo);
    }

    public List<String> getTowHopsAsPath(String queryAs, List<String> asPath) {
        Integer index = asPath.indexOf(queryAs);
        Integer asLength = asPath.size();
        List<String> subList = new ArrayList<>();

        if (index + 3 <= asLength) {
            subList = asPath.subList(index, index + 3);
        } else if (index + 2 <= asLength) {
            subList = asPath.subList(index, index + 2);
        }
        return subList;
    }

    public List<String> getCountriesList(List<String> asPath) {
        FindCountryInfos countryQuery = new FindCountryInfos();
        List<String> countriesList = new ArrayList<>();
        for (int i = 0; i < asPath.size() - 1; i++) {
            if (countryQuery.getCountryName(asPath.get(i)).equals(countryQuery.getCountryName(asPath.get(i + 1)))) {
                if (i==0){
                    break;
                }else{
                    countriesList.add(countryQuery.getCountryName(asPath.get(i)));
                    break;
                }
            }
            countriesList.add(countryQuery.getCountryName(asPath.get(i)));
            if (i == asPath.size() - 2) {
                countriesList.add(countryQuery.getCountryName(asPath.get(i + 1)));
            }
        }
        return countriesList;
    }

    @Override
    public List<AsPathInfoBean> twoHopsQuery(String queryAs) {
        this.asSelectList = new ArrayList<>();
        List<String> countries = new ArrayList<>();

        for (AsPathInfoBean asInfo : this.asInfoList) {
            List<String> asPath = new ArrayList<>();
            // judge if the ASN in AS_PATH
            if (asInfo.getAsPath().contains(queryAs)) {
                asPath = asInfo.getAsPath();
                // if true then check if the ASN have next hop
                if (asPath.indexOf(queryAs) + 1 != asPath.size()) {
                    // get as path in two hops
                    asPath = getTowHopsAsPath(queryAs, asPath);
                    // check if all of the AS are in different country
                    countries = getCountriesList(asPath);
                    if (!countries.isEmpty()) {
                        asPath=asPath.subList(0, countries.size());
                        asInfo.setAsPath(asPath);
                        asInfo.setCountry(countries);
                        checkInfoExist(asInfo);
                    }
                }
            }
        }

        return this.asSelectList;
    }

}
