package com.example.bgpm.controller;


import com.example.bgpm.bean.AsDetectBean;
import com.example.bgpm.bean.AsLineBean;
import com.example.bgpm.bean.AsNodeBean;
import com.example.bgpm.bean.AsResultBean;
import com.example.bgpm.utils.SaveAsFile;
import org.codehaus.jettison.json.JSONException;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.*;

public class KSkipMap {

    /**
     * get node list in each level, get duplicated net line info
     * @param centerAs
     * @param asDetectDataset
     * @param asLocationDictionary
     * @param k
     * @param chinaAs
     * @param findChina
     * @return
     */
    public AsResultBean ChinaSkipOutput(String centerAs, List<AsDetectBean> asDetectDataset,
                                        HashMap<String, AsNodeBean> asLocationDictionary, int k,
                                        String chinaAs, Boolean findChina) throws FileNotFoundException {

        HashMap<String, BigInteger> lineMap = new HashMap<>(); // duplicated net line info map
        HashMap<String, BigInteger> lineMapForNation = new HashMap<>();
        HashSet<String> nodeSet = new HashSet<>();  // duplicated node set
        HashSet<String> nationSet = new HashSet<>();  // duplicated nation set

        List<List<String>> nodesInEachLevel = new ArrayList<>();
        List<String> beginList = new ArrayList<>();
        if(findChina) {
            beginList.add(chinaAs);
            nodeSet.add(chinaAs);
            nationSet.add(asLocationDictionary.get(chinaAs).getNationName());
        }
        else {
            beginList.add(centerAs);
            nodeSet.add(centerAs);
            nationSet.add(asLocationDictionary.get(centerAs).getNationName());
        }
        nodesInEachLevel.add(beginList); // begin node

        String beginNation = asLocationDictionary.get(centerAs).getNationName();

        for (AsDetectBean asDetectItem : asDetectDataset) {
            String[] asList = asDetectItem.getAsList().split(" ");

            if (!Objects.equals(asList[0], centerAs)) continue; // del As which in-nation

            int asListSize = asList.length;

            int i = 0;  // begin node cycle index
            int t = 0;  // begin node static index

            // DEMAND: begin node may not the first in the record
            if(findChina){
                int hasChinaAs = 0;
                AsNodeBean asNBB = asLocationDictionary.get(chinaAs);
                if(asNBB==null) continue;  // this node not in dict
                beginNation = asNBB.getNationName();  // get begin nation name
                for (; t < asListSize - 1; t++){  // get the target node location in this record
                    if(Objects.equals(asList[t], chinaAs)){
                        hasChinaAs = 1;
                        i = t;
                        break;
                    }
                }
                if(hasChinaAs == 0) continue;  // this node not in this record
            }

            // cycle to get (1) nodes in each level, (2) line map to line list
            for (; i < t+k && i < asListSize - 1; i++) {
                String leftNode = asList[i];
                String rightNode = asList[i+1];

                if(Objects.equals(leftNode, rightNode)) continue;
                AsNodeBean asNBR = asLocationDictionary.get(rightNode);  // confirm can build a line
                if(asNBR==null) continue;
                String rightNation = asNBR.getNationName();
//                if(rightNation.equals(beginNation)) continue;
                AsNodeBean asNBL = asLocationDictionary.get(leftNode);
                if(asNBL==null) continue;
                String leftNation = asNBL.getNationName();
//                if(leftNation==null || leftNation.equals(rightNation) ||
//                        (i!=t && beginNation.equals(leftNation))) continue;
                String key = leftNode + "-" + rightNode;  // connect to get key value

                // (1) get nodes in each level
                if(!nodeSet.contains(rightNode)){  // hadn't read it
                    nodeSet.add(rightNode);
                    nationSet.add(asLocationDictionary.get(rightNode).getNationName());
                    if(nodesInEachLevel.size()<(i-t+2)){  // if don't have this level yet
                        List<String> newList = new ArrayList<>();
                        newList.add(rightNode);
                        nodesInEachLevel.add(newList);
                    } else {
                        nodesInEachLevel.get(i-t+1).add(rightNode);
                    }
                }

                // (2) get line map which just save as-ip-size to calculate the sum
                lineMap.merge(key, BigInteger.valueOf(2).pow(asDetectItem.getPowSize()), BigInteger::add);
                if(!Objects.equals(leftNation, rightNation)){
                    String keyForNation = leftNation + "-" + rightNation;
                    lineMapForNation.merge(keyForNation,
                            BigInteger.valueOf(2).pow(asDetectItem.getPowSize()), BigInteger::add);
                }
            }
        }

        // convert line map to list
        List<AsLineBean> lineList = new ArrayList<>();
        for (Map.Entry<String, BigInteger> entry : lineMap.entrySet()) {
            String [] beginEnd = entry.getKey().split("-");
            lineList.add(new AsLineBean(beginEnd[0], beginEnd[1], entry.getValue()));
        }
        List<AsLineBean> lineListForNation = new ArrayList<>();
        for (Map.Entry<String, BigInteger> entry : lineMapForNation.entrySet()) {
            String [] beginEnd = entry.getKey().split("-");
            lineListForNation.add(new AsLineBean(beginEnd[0], beginEnd[1], entry.getValue()));
        }

        // get outline nodes and nations
        HashSet<String> outNodesSet = new HashSet<>();
        HashSet<String> outNationsSet = new HashSet<>();
        for (Map.Entry<String, AsNodeBean> entry : asLocationDictionary.entrySet()) {
            String asId = entry.getValue().getAsId();
            if (!nodeSet.contains(asId)) {
                outNodesSet.add(asId);
                String asNation = entry.getValue().getNationName();
                if (!nationSet.contains(asNation)) {
                    outNationsSet.add(asNation);
                }
            }
        }
        ArrayList<String> outNodesList = new ArrayList<>(outNodesSet);
        ArrayList<String> outNationsList = new ArrayList<>(outNationsSet);

//        System.out.println("nodeList");
//        System.out.println(nodesInEachLevel);
//        System.out.println("lineList");
//        System.out.println(lineList);

        // output
        String nodeSavePath = "/Users/mac/Desktop/node.txt";
        String lineSavePath = "/Users/mac/Desktop/line.txt";
        outputForNetworkX(nodeSet, lineList, nodeSavePath, lineSavePath);

        System.out.println("outNodesList");
//        System.out.println(outNodesList);
        System.out.println(outNodesList.size());
        System.out.println("outNationsList");
//        System.out.println(outNationsList);
        System.out.println(outNationsList.size());
        int countDDD = 0;
        for(List<String> al : nodesInEachLevel){
            countDDD += al.size();
        }
        System.out.println("inNodesList");
        System.out.println(countDDD);
        System.out.println("inNationsList");
        System.out.println(nationSet.size());

        // merge as country nodes and lines
        String nodeSavePathForNation = "/Users/mac/Desktop/node_nation.txt";
        String lineSavePathForNation = "/Users/mac/Desktop/line_nation.txt";
        outputForNetworkX(nationSet, lineListForNation, nodeSavePathForNation, lineSavePathForNation);

        return new AsResultBean();
    }

    /**
     * output networkx format info
     * @param nodeSet
     * @param lineList
     */
    private void outputForNetworkX(HashSet<String> nodeSet, List<AsLineBean> lineList,
                                   String nodePath, String linePath) throws FileNotFoundException {

        SaveAsFile saveAsFile = new SaveAsFile();

        // save node list to txt
        List<String> nodeListForNetwrokX = new ArrayList<>(nodeSet);
        System.out.println(nodeListForNetwrokX);
        saveAsFile.saveAsTxt(nodeListForNetwrokX.toString(), nodePath);

        // save line list to txt
        List<List<String>> lineListForNetworkX = new ArrayList<>();
        for(AsLineBean alb: lineList){
            List<String> tmp = new ArrayList<>();
            tmp.add(alb.getBegin());
            tmp.add(alb.getEnd());
            tmp.add(alb.getIpSize().toString());
            lineListForNetworkX.add(tmp);
        }
        System.out.println(lineListForNetworkX);
        saveAsFile.saveAsTxt(lineListForNetworkX.toString(), linePath);
    }



    public static void main(String[] args) throws FileNotFoundException, JSONException {
        String centralAs = "14315";
        String inPath = "/Users/mac/Documents/ChinaTelecom/BGP/input_file/snapJava1000.csv";
        String asInfoDictPath = "/Users/mac/Documents/ChinaTelecom/BGP/input_file/as_country_with_geo.csv";
        int skipTimes = 100;

        KSkipMap kSkipMap = new KSkipMap();
        AsSkip asSkip = new AsSkip();
        // dict
        HashMap<String, AsNodeBean> asLocationDictionary = asSkip.ReadAsInformationAsMap(asInfoDictPath);
        // dataset
        List<AsDetectBean> asDetectDataset = asSkip.ReadAsDetectData(inPath, centralAs);

        AsResultBean asResultBean = kSkipMap.ChinaSkipOutput(centralAs, asDetectDataset, asLocationDictionary,
                skipTimes, "45352", false);
        System.out.println(asResultBean);
    }
}
