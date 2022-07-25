package com.example.bgpm.utils.ljp;

import java.io.*;
import java.util.*;
import java.nio.file.*;
// import java.util.logging.*;
import java.nio.charset.StandardCharsets;
import com.example.bgpm.utils.ljp.*;

public class FindCountryInfos implements AsCountryInterface {
    // private static Logger logger = Logger.getLogger("FindCountryInfos");
    // ASN and Country
    private String asCountryFileName = "/Users/mac/desktop/as_country.csv";
    // Country, Longitude, Latitude
    private String countryOnlyFileName = "/Users/mac/desktop/country.csv";
    // ASN:Country
    private HashMap<String, String> asCountryMap = new HashMap<String, String>();
    // Country:[Longitude, Latitude]
    private HashMap<String, List<String>> countryInfos = new HashMap<String, List<String>>();

    public HashMap<String, String> getCountryMap() {
        return this.asCountryMap;
    }

    public HashMap<String, List<String>> getCountryInfos() {
        return this.countryInfos;
    }

    @Override
    public void readAsCountryFile(String fileName) {
        if (this.asCountryMap.isEmpty()) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.UTF_8)) {
                String DELIMITER = ",";
                String line;
                br.readLine(); // Skip the header
                while ((line = br.readLine()) != null) {
                    // [0] for ASN, [1] for country
                    String[] columns = line.split(DELIMITER);
                    this.asCountryMap.put(columns[0], columns[1]);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void readCountryOnlyFile(String fileName) {
        if (this.countryInfos.isEmpty()) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.UTF_8)) {
                String DELIMITER = ",";
                String line;
                br.readLine(); // Skip the header
                while ((line = br.readLine()) != null) {
                    List<String> details = new ArrayList<>();
                    // [0] for country, [1] for longitude, [2] for latitude
                    String[] columns = line.split(DELIMITER);
                    details.add(columns[1]);
                    details.add(columns[2]);
                    this.countryInfos.put(columns[0], details);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public String getCountryName(String as) {
        readAsCountryFile(this.asCountryFileName);
        return asCountryMap.get(as);
    }

    @Override
    public List<String> getCountryInfos(String country) {
        readCountryOnlyFile(this.countryOnlyFileName);
        return countryInfos.get(country);
    }

}
