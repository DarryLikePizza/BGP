package com.example.bgpm.controller;

import com.example.bgpm.bean.*;
import com.example.bgpm.utils.SaveAsJson;
import com.example.bgpm.utils.ljp.TwoHopsQuery;
import com.opencsv.bean.CsvToBeanBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

//import static com.example.bgpm.utils.JsonArraysSorted.jsonArraySort;


public class AsSkip {

    // if the dataset we chose is the national one , using this method to select the records we need
    public List<AsDetectBean> ReadAsDetectData(String inPath, String centerAs) throws FileNotFoundException {
        return new CsvToBeanBuilder<AsDetectBean>(new FileReader(inPath))
                .withType(AsDetectBean.class)
                .withSeparator(',')
                .withFilter(line -> line[0].equals(centerAs))
                .build().parse();
    }

    // as location dictionary / nation location dictionary
    public HashMap<String, AsNodeBean> ReadAsInformationAsMap(String asInfoDictPath){
        HashMap<String, AsNodeBean> asInfoMap = new HashMap<>();
        try (BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(asInfoDictPath),
                StandardCharsets.UTF_8))) {
            String record;

            while ((record = file.readLine()) != null) {
                String[] cells = record.split(",");
                String asId = cells[0].substring(2,cells[0].length());
                AsNodeBean asInfoItem = new AsNodeBean(asId, cells[1], cells[2], cells[3]);
                asInfoMap.put(asId, asInfoItem);
            }
        } catch (Exception e) {
            System.out.println("Dictionary File Read Error");
        }
        return asInfoMap;
    }


    // get k skip information list
    public AsResultBean AsSkipInfoOutput(String centerAs, List<AsDetectBean> asDetectDataset,
                                         HashMap<String, AsNodeBean> asLocationDictionary, int k){

        // get firstSkipNodeList firstSkipLineList secondSkipLineList secondSkipNodeList
        HashMap<String, BigInteger> firstSkipLineMap = new HashMap<>();
        HashMap<String, BigInteger> secondSkipLineMap = new HashMap<>();

        String beginNation = asLocationDictionary.get(centerAs).getNationName();

        for (AsDetectBean asDetectItem : asDetectDataset) {
            String[] asList = asDetectItem.getAsList().split(" ");
            // del As which in-nation
            if (!Objects.equals(asList[0], centerAs)){
                continue;
            }

            int asListSize = asList.length;
            for (int i = 0; i < k && i < asListSize - 1; i++) {
                // del As which info not in the dict 723
                AsNodeBean asNB = asLocationDictionary.get(asList[i+1]);
                if(asNB==null){
                    continue;
                }
                String rightNation = asNB.getNationName();
                if(rightNation.equals(beginNation)){
                    continue;
                }

                String key = asList[i] + " " + asList[i + 1];

                if (i == 0) { // one skip
                    BigInteger oldIpSize = firstSkipLineMap.get(key);  // confirm is there had the key
                    String ipArea = asDetectItem.getIpArea();
                    if (oldIpSize != null) {  // if not
                        firstSkipLineMap.put(key, oldIpSize.add(BigInteger.valueOf(2).pow(asDetectItem.getPowSize())));
                    } else {
                        firstSkipLineMap.put(key, BigInteger.valueOf(2).pow(asDetectItem.getPowSize()));
                    }
                }
                if (i == 1) {  // two skip
                    AsNodeBean asNBL = asLocationDictionary.get(asList[i]);
                    if(asNBL==null){
                        continue;
                    }
                    String leftNation = asNBL.getNationName();
                    if(leftNation==null || leftNation.equals(rightNation) || beginNation.equals(rightNation)
                        || beginNation.equals(leftNation)){
                        continue;
                    }
                    BigInteger oldIpSize = secondSkipLineMap.get(key);  // confirm is there had the key
                    String ipArea = asDetectItem.getIpArea();
                    if (oldIpSize != null) {  // if not
                        // confirm is there had the ipArea
                        secondSkipLineMap.put(key, oldIpSize.add(BigInteger.valueOf(2).pow(asDetectItem.getPowSize())));
                    } else {
                        secondSkipLineMap.put(key, BigInteger.valueOf(2).pow(asDetectItem.getPowSize()));
                    }
                }
            }
        }

        // convert to list
        List<AsLineBean> asOneLineList = new ArrayList<>();
        List<AsLineBean> asTwoLineList = new ArrayList<>();
        for (Map.Entry<String, BigInteger> entry : firstSkipLineMap.entrySet()) {
            String [] beginEnd = entry.getKey().split(" ");
            asOneLineList.add(new AsLineBean(beginEnd[0], beginEnd[1], entry.getValue()));
        }
        for (Map.Entry<String, BigInteger> entry : secondSkipLineMap.entrySet()) {
            String [] beginEnd = entry.getKey().split(" ");
            asTwoLineList.add(new AsLineBean(beginEnd[0], beginEnd[1], entry.getValue()));
        }
        return new AsResultBean(asOneLineList, asTwoLineList);
    }

    // get china k skip
    public AsResultBean ChinaSkipOutput(String centerAs, List<AsDetectBean> asDetectDataset,
                                         HashMap<String, AsNodeBean> asLocationDictionary, int k,
                                        String chinaAs, Boolean findChina){

        // get firstSkipNodeList firstSkipLineList secondSkipLineList secondSkipNodeList
        HashMap<String, BigInteger> firstSkipMap = new HashMap<>();
        HashMap<String, BigInteger> secondSkipMap = new HashMap<>();

        String beginNation = asLocationDictionary.get(centerAs).getNationName();
        String beginNationReal = asLocationDictionary.get(centerAs).getNationName();

        for (AsDetectBean asDetectItem : asDetectDataset) {
            String[] asList = asDetectItem.getAsList().split(" ");
            // del As which in-nation
            if (!Objects.equals(asList[0], centerAs)){
                continue;
            }

            int asListSize = asList.length;

            int i = 0;
            int t = 0;

            // 用于第二个需求
            if(findChina){
                int hasChinaAs = 0;
                AsNodeBean asNBB = asLocationDictionary.get(chinaAs);
                if(asNBB==null) continue;
                beginNation = asNBB.getNationName();
                for (; t < k && t < asListSize - 1; t++){
                    if(Objects.equals(asList[t], chinaAs)){
                        hasChinaAs = 1;
                        i = t;
                        break;
                    }
                }
                if(hasChinaAs == 0) continue;
            }

            for (; i < k && i < asListSize - 1; i++) {
                // del As which info not in the dict 723
                AsNodeBean asNB = asLocationDictionary.get(asList[i+1]);
                if(asNB==null){
                    continue;
                }
                String rightNation = asNB.getNationName();
                if(rightNation.equals(beginNation) || rightNation.equals(beginNationReal)){
                    continue;
                }


                String key = asList[i] + " " + asList[i + 1];

                if (i == t) { // one skip
                    BigInteger oldIpSize = firstSkipMap.get(key);  // confirm is there had the key
                    if (oldIpSize != null) {  // if not
                        firstSkipMap.put(key, oldIpSize.add(BigInteger.valueOf(2).pow(asDetectItem.getPowSize())));
                    } else {
                        firstSkipMap.put(key, BigInteger.valueOf(2).pow(asDetectItem.getPowSize()));
                    }
                }
                else if (i == t+1) {  // two skip
                    AsNodeBean asNBL = asLocationDictionary.get(asList[i]);
                    if(asNBL==null){
                        continue;
                    }
                    String leftNation = asNBL.getNationName();
                    if(leftNation==null || leftNation.equals(rightNation) || beginNation.equals(rightNation)
                            || beginNation.equals(leftNation)){
                        continue;
                    }
                    BigInteger oldIpSize = secondSkipMap.get(key);  // confirm is there had the key
                    String ipArea = asDetectItem.getIpArea();
                    if (oldIpSize != null) {  // if not
                        // confirm is there had the ipArea
                        secondSkipMap.put(key, oldIpSize.add(BigInteger.valueOf(2).pow(asDetectItem.getPowSize())));
                    } else {
                        secondSkipMap.put(key, BigInteger.valueOf(2).pow(asDetectItem.getPowSize()));
                    }
                }
            }
        }

        // convert to list
        List<AsLineBean> asOneLineList = new ArrayList<>();
        List<AsLineBean> asTwoLineList = new ArrayList<>();
        for (Map.Entry<String, BigInteger> entry : firstSkipMap.entrySet()) {
            String [] beginEnd = entry.getKey().split(" ");
            asOneLineList.add(new AsLineBean(beginEnd[0], beginEnd[1], entry.getValue()));
        }
        for (Map.Entry<String, BigInteger> entry : secondSkipMap.entrySet()) {
            String [] beginEnd = entry.getKey().split(" ");
            asTwoLineList.add(new AsLineBean(beginEnd[0], beginEnd[1], entry.getValue()));
        }
        return new AsResultBean(asOneLineList, asTwoLineList);
    }


    // lzp
    public AsResultBean AsSkipInfoOutputLarge(String queryAs, String asInfosFileName,
                                              HashMap<String, AsNodeBean> asLocationDictionary){

        TwoHopsQuery twoHopsQueryObject = new TwoHopsQuery();
        // FindCountryInfos findCountryInfosObject = new FindCountryInfos();

        List<AsPathInfoBean> asSelectList = new ArrayList<>();

        twoHopsQueryObject.readAsPathFile(asInfosFileName);
        asSelectList = twoHopsQueryObject.twoHopsQuery(queryAs);

        List<AsLineBean> asOneLineBeanList = new ArrayList<>();
        List<AsLineBean> asTwoLineBeanList = new ArrayList<>();
        for (AsPathInfoBean asInfo : asSelectList) {
            // to save as AsResultBean;
            int asPathLength = asInfo.getAsPath().size();
            for(int i = 0; i < asPathLength-1; i++){
                AsLineBean asLineBean = new AsLineBean(asInfo.getAsPath().get(i),
                        asInfo.getAsPath().get(i+1),
                        asInfo.getIpSize());
                if(i==0) {
                    asOneLineBeanList.add(asLineBean);
                }
                else {
                    asTwoLineBeanList.add(asLineBean);
                }
            }
        }
        return new AsResultBean(asOneLineBeanList, asTwoLineBeanList);
    }


    // pie JsonObject
    public String GetPieJson(List<AsLineBean> lineList, HashMap<String, AsNodeBean> asLocalDict,
                             BigInteger limitCount, BigInteger limitCountForNode) throws JSONException {
        HashMap<String, PieBean> pieMap = new HashMap<>();
        for(AsLineBean alb : lineList){
            String nation = asLocalDict.get(alb.getEnd()).getNationName();
            PieBean pieItem = pieMap.get(nation);
            if(pieItem==null){
                HashMap<String, BigInteger> tmpMap = new HashMap<>();
                tmpMap.put(alb.getEnd(), alb.getIpSize());
                pieMap.put(nation, new PieBean(nation, alb.getIpSize(), tmpMap));
            } else {
                HashMap<String, BigInteger> tmpMap = pieItem.getAsIpMap();
                if(tmpMap.get(alb.getEnd()) == null){
                    tmpMap.put(alb.getEnd(), alb.getIpSize());
                } else{
                    BigInteger ipSize = tmpMap.get(alb.getEnd());
                    tmpMap.put(alb.getEnd(), alb.getIpSize().add(ipSize));
                }
                BigInteger totalIpSize = pieItem.getIpCount().add(alb.getIpSize());
                pieMap.put(nation, new PieBean(nation, totalIpSize, tmpMap));
            }
        }

        // arracation
        BigInteger ipCount = new BigInteger("0");
        HashMap<String, BigInteger> otherMap = new HashMap<>();
        List<String> delList = new ArrayList<>();
        for (Map.Entry<String, PieBean> pm : pieMap.entrySet()) {
            PieBean pmPie = pm.getValue();
            BigInteger ipNodeCount = new BigInteger("0");
            List<String> delNodeList = new ArrayList<>();
            HashMap<String, BigInteger> nodeMap = pmPie.getAsIpMap();
            for(Map.Entry<String, BigInteger> aim : nodeMap.entrySet()){
                if(aim.getValue().compareTo(limitCountForNode) < 0){
                    ipNodeCount = ipNodeCount.add(aim.getValue());
                    delNodeList.add(aim.getKey());
                }
            }
            if(!ipNodeCount.equals(new BigInteger("0"))){
                for(String delN: delNodeList){
                    nodeMap.remove(delN);
                }
                nodeMap.put("others", ipNodeCount);
                PieBean otherBean = new PieBean(pm.getValue().getAreaName(), pm.getValue().getIpCount(), nodeMap);
                pieMap.put(pm.getKey(),otherBean);
            }
            if(pmPie.getIpCount().compareTo(limitCount) < 0){
                ipCount = ipCount.add(pmPie.getIpCount());
                otherMap.putAll(pmPie.getAsIpMap());
                delList.add(pm.getKey());
            }
        }
        for(String delAs: delList){
            pieMap.remove(delAs);
        }
        if (!ipCount.equals(new BigInteger("0"))){
            PieBean otherBean = new PieBean("others", ipCount, otherMap);
            pieMap.put("others", otherBean);
        }

        // create json
        JSONObject resultJson = new JSONObject();
//        JSONArray areaArray = new JSONArray();
        JSONArray asArray = new JSONArray();

        ArrayList<PieBean> valueList = new ArrayList<>(pieMap.values());
        Collections.sort(valueList, (o1, o2) -> {
            //先按照年龄排序
            return o2.getIpCount().compareTo(o1.getIpCount());
        });

        for (PieBean entry: valueList) {

            JSONArray itemArray = new JSONArray();
            HashMap<String, BigInteger> asMap = entry.getAsIpMap();

            List<Map.Entry<String,BigInteger>> list = new ArrayList<Map.Entry<String,BigInteger>>(asMap.entrySet());
            //collections工具类的排序方法
            Collections.sort(list, new Comparator<Map.Entry<String, BigInteger>>() {
                @Override
                public int compare(Map.Entry<String, BigInteger> o1, Map.Entry<String, BigInteger> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            //遍历输出结果
            for (Map.Entry<String,BigInteger> m : list) {
                Map<String, Long> item = new HashMap(){
                    {
                        put("name", m.getKey());
                        put("value", m.getValue());
                    }
                };
                itemArray.put(item);
            }

            Map<String, Long> asItem = new HashMap(){
                {
                    put("name", entry.getAreaName());
                    put("children", itemArray);
                }
            };
            asArray.put(asItem);
        }
//        JSONArray asSortedArray = jsonArraySort(asArray, "IP_count", false);
//        resultJson.put("area", areaArray);
//        resultJson.put("as_detail", asArray);
        String result = asArray.toString();
        return result;
    }

    public String GetMapJson(String centralAs, HashMap<String, AsNodeBean> asLocationDict, AsResultBean asResultBean, int limitCount, int k) throws JSONException {
        AsNodeBean centralAsInfo = asLocationDict.get(centralAs);

        List<AsLineBean> asOneLineList = asResultBean.getAsOneSkipLineList();
        List<AsLineBean> asTwoLineList = asResultBean.getAsTwoSkipLineList();

        if(k==1){
            Map<String, MapBean> item = new HashMap<>();
            JSONArray lineArray = new JSONArray();
            // asnodebean to point
            int index = 1;
            for (AsLineBean alb : asOneLineList) {
                int ipSize = alb.getIpSize().toString(10).length() - limitCount;
                if(ipSize<=0){
                    continue;
                }
                // endNode
                String endNation = asLocationDict.get(alb.getEnd()).getNationName();
                MapBean hasMap = item.get(endNation);
                if(hasMap==null){
                    List<String> asList = new ArrayList<>();
                    asList.add(alb.getEnd());
                    List<Double> locationList = new ArrayList<>();
                    locationList.add(Double.parseDouble(asLocationDict.get(alb.getEnd()).getLongitude()));
                    locationList.add(Double.parseDouble(asLocationDict.get(alb.getEnd()).getLatitude()));
                    item.put(endNation, new MapBean(index++, asList, locationList, endNation, "", "", 1));
                } else{
                    List<String> asList = hasMap.getAsList();

                    // duplicate asList 723
                    String waitInAs = alb.getEnd();
                    if(asList.contains(waitInAs)){
                        continue;
                    }
                    asList.add(waitInAs);

                    item.put(endNation, new MapBean(hasMap.getId(), asList, hasMap.getValue(), endNation, "", "", hasMap.getCoefficient()+1));
                }
                // get line
                List<List<Double>> coords = new ArrayList<>();
                List<Double> locationLeft = new ArrayList<>();
                locationLeft.add(Double.parseDouble(centralAsInfo.getLongitude()));
                locationLeft.add(Double.parseDouble(centralAsInfo.getLatitude()));
                coords.add(locationLeft);
                List<Double> locationRight = new ArrayList<>();
                locationRight.add(Double.parseDouble(asLocationDict.get(alb.getEnd()).getLongitude()));
                locationRight.add(Double.parseDouble(asLocationDict.get(alb.getEnd()).getLatitude()));
                coords.add(locationRight);

                Map<String, Long> lineItem = new HashMap() {
                    {
                        put("from", 0);
                        put("to", item.get(endNation).getId());
                        put("coefficient", ipSize);
                        put("coords", coords);
                    }
                };
                lineArray.put(lineItem);
            }
            // get point
            JSONArray pointArray = new JSONArray();
            List<String> startList = new ArrayList<>();
            startList.add(centralAsInfo.getAsId());
            List<Double> locationList = new ArrayList<>();
            locationList.add(Double.parseDouble(centralAsInfo.getLongitude()));
            locationList.add(Double.parseDouble(centralAsInfo.getLatitude()));
            Map<String, Long> startPoint = new HashMap() {
                {
                    put("id", 0);
                    put("as_list", startList);
                    put("value", locationList);
                    put("country", centralAsInfo.getNationName());
                    put("province", "");
                    put("city", "");
                    put("coefficient", 10);
                }
            };
            pointArray.put(startPoint);
            for (Map.Entry<String, MapBean> entry : item.entrySet()) {
                // limit the coefficient in [5, 10]
                int coefficient = entry.getValue().getCoefficient();
                int coe = String.valueOf(coefficient).length();
                if(coe>10) coe = 10;
                else if(coe < 5) coe = 5;
                int finalCoe = coe;
                Map<String, Long> pointItem = new HashMap() {
                    {
                        put("id", entry.getValue().getId());
                        put("as_list", entry.getValue().getAsList());
                        put("value", entry.getValue().getValue());
                        put("country", entry.getValue().getCountry());
                        put("province", entry.getValue().getProvince());
                        put("city", entry.getValue().getCity());
                        put("coefficient", finalCoe);
                    }
                };
                pointArray.put(pointItem);
            }
            JSONObject oneResultJson = new JSONObject();
            oneResultJson.put("points", pointArray);
            oneResultJson.put("lines", lineArray);
            String oneResult = oneResultJson.toString();

            return oneResult;
        }

        // two
        // asnodebean to point
        int index = 1;
        Map<String, MapBean> itemTwo = new HashMap<>();
        JSONArray lineTwoArray = new JSONArray();
        for (AsLineBean alb : asTwoLineList) {
            int ipSize = alb.getIpSize().toString(10).length() - limitCount;
            if(ipSize<=0){
                continue;
            }
            // endNode
            String endNation = asLocationDict.get(alb.getEnd()).getNationName();
            MapBean hasMap = itemTwo.get(endNation);
            if(hasMap==null){
                List<String> asList = new ArrayList<>();
                asList.add(alb.getEnd());
                List<Double> locList = new ArrayList<>();
                locList.add(Double.parseDouble(asLocationDict.get(alb.getEnd()).getLongitude()));
                locList.add(Double.parseDouble(asLocationDict.get(alb.getEnd()).getLatitude()));
                itemTwo.put(endNation, new MapBean(index++, asList, locList, endNation, "", "", 1));
            } else{
                List<String> asList = hasMap.getAsList();

                // duplicate asList 723
                String waitInAs = alb.getEnd();
                if(asList.contains(waitInAs)){
                    continue;
                }
                asList.add(waitInAs);

                itemTwo.put(endNation, new MapBean(hasMap.getId(), asList, hasMap.getValue(), endNation, "", "", hasMap.getCoefficient()+1));
            }
            // beginNode
            String endNationB = asLocationDict.get(alb.getBegin()).getNationName();
            MapBean hasMapOne = itemTwo.get(endNationB);
            if(hasMapOne==null){
                List<String> asList = new ArrayList<>();
                asList.add(alb.getBegin());
                List<Double> locList = new ArrayList<>();
                locList.add(Double.parseDouble(asLocationDict.get(alb.getBegin()).getLongitude()));
                locList.add(Double.parseDouble(asLocationDict.get(alb.getBegin()).getLatitude()));
                itemTwo.put(endNationB, new MapBean(index++, asList, locList, endNationB, "", "", 1));
            } else{
                List<String> asList = hasMapOne.getAsList();
                asList.add(alb.getEnd());
                itemTwo.put(endNationB, new MapBean(hasMapOne.getId(), asList, hasMapOne.getValue(), endNationB, "", "", hasMapOne.getCoefficient()+1));
            }
            // get line
            List<List<Double>> coords = new ArrayList<>();
            List<Double> locationLeft = new ArrayList<>();
            locationLeft.add(Double.parseDouble(asLocationDict.get(alb.getBegin()).getLongitude()));
            locationLeft.add(Double.parseDouble(asLocationDict.get(alb.getBegin()).getLatitude()));
            coords.add(locationLeft);
            List<Double> locationRight = new ArrayList<>();
            locationRight.add(Double.parseDouble(asLocationDict.get(alb.getEnd()).getLongitude()));
            locationRight.add(Double.parseDouble(asLocationDict.get(alb.getEnd()).getLatitude()));
            coords.add(locationRight);
            Map<String, Long> lineItem = new HashMap() {
                {
                    put("from", itemTwo.get(endNationB).getId());
                    put("to", itemTwo.get(endNation).getId());
                    put("coefficient", ipSize);
                    put("coords", coords);
                }
            };
            lineTwoArray.put(lineItem);
        }
        // get point
        JSONArray pointArray = new JSONArray();
        for (Map.Entry<String, MapBean> entry : itemTwo.entrySet()) {
            // limit the coefficient in [5, 10]
            int coefficient = entry.getValue().getCoefficient();
            int coe = String.valueOf(coefficient).length();
            if(coe>10) coe = 10;
            else if(coe < 5) coe = 5;
            int finalCoe = coe;
            Map<String, Long> pointItem = new HashMap() {
                {
                    put("id", entry.getValue().getId());
                    put("as_list", entry.getValue().getAsList());
                    put("value", entry.getValue().getValue());
                    put("country", entry.getValue().getCountry());
                    put("province", entry.getValue().getProvince());
                    put("city", entry.getValue().getCity());
                    put("coefficient", finalCoe);
                }
            };
            pointArray.put(pointItem);
        }
        JSONObject twoResultJson = new JSONObject();
        twoResultJson.put("points", pointArray);
        twoResultJson.put("lines", lineTwoArray);
        String twoResult = twoResultJson.toString();

        return twoResult;
    }


    public static void main(String[] args) throws FileNotFoundException, JSONException {
        String centralAs = "14315";
        String inPath = "/Users/mac/desktop/BGP/input_file/snapJava1000.csv";
        String asInfoDictPath = "/Users/mac/desktop/BGP/input_file/as_country_with_geo.csv";
        int skipTimes = 2;

        AsSkip asSkip = new AsSkip();
        HashMap<String, AsNodeBean> asLocationDictionary = asSkip.ReadAsInformationAsMap(asInfoDictPath);

        // LiDa's project
        List<AsDetectBean> asDetectDataset = asSkip.ReadAsDetectData(inPath, centralAs);
        AsResultBean asResultBean = asSkip.AsSkipInfoOutput(centralAs, asDetectDataset, asLocationDictionary, skipTimes);
        System.out.println(asResultBean);
        // more Da project
        AsResultBean asResultBean2 = asSkip.ChinaSkipOutput(centralAs, asDetectDataset, asLocationDictionary, skipTimes, "45352", true);
        System.out.println(asResultBean2);
        // LuZhiping's project
//        AsResultBean asResultBean = asSkip.AsSkipInfoOutputLarge(centralAs, inPath, asLocationDictionary);

        SaveAsJson saveAsJson = new SaveAsJson();

        // pie
        String filePathOne = "/Users/mac/desktop/onePie.json";
        String filePathTwo = "/Users/mac/desktop/twoPie.json";
        BigInteger limitCount = new BigInteger("1000000");
        BigInteger limitCountForNode = new BigInteger("10000");
        // one
        List<AsLineBean> asOneLineList = asResultBean.getAsOneSkipLineList();
        String pieOneJson = asSkip.GetPieJson(asOneLineList, asLocationDictionary, limitCount, limitCountForNode);
        saveAsJson.saveJsonData(pieOneJson, filePathOne);
        System.out.println(pieOneJson);
        // two
        List<AsLineBean> asTwoLineList = asResultBean.getAsTwoSkipLineList();
        String pieTwoJson = asSkip.GetPieJson(asTwoLineList, asLocationDictionary, limitCount, limitCountForNode);
        saveAsJson.saveJsonData(pieTwoJson, filePathTwo);
        System.out.println(pieTwoJson);

        // map
        String mapFilePathOne = "/Users/mac/desktop/map.json";
        int limit = 4;  // 位数限制，小于这个位数的ip十进制长度直接删除。

        // one
        int k = 1;
        String mapResult = asSkip.GetMapJson(centralAs, asLocationDictionary, asResultBean, limit, k);
        saveAsJson.saveJsonData(mapResult, mapFilePathOne);
        System.out.println(mapResult);
        // two
        int k2 = 2;
        String mapResult2 = asSkip.GetMapJson(centralAs, asLocationDictionary, asResultBean, limit, k2);
        saveAsJson.saveJsonData(mapResult2, mapFilePathOne);
        System.out.println(mapResult2);
    }
}
