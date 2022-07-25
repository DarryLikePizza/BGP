package com.example.bgpm.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

//import com.opencsv.CSVWriter;
import com.example.bgpm.bean.AsDetectBean;

public class ReadTextAsCsv {

    public void dataPreProcess(String inPath, String outPath, Boolean isChinaData, String centralAs){
        List<AsDetectBean> asDetectBeanList = new ArrayList<>();
        try {
            RandomAccessFile file = new RandomAccessFile(inPath, "r");
            String str;
            int a = 100000;
            int powSize = 0;
            HashMap<String, Integer> IPPowMap = new HashMap<>();
            if(!isChinaData){
                while ((str = file.readLine()) != null) {
                    String[] strArray = str.split("\\|",10);
                    if(!Objects.equals(strArray[7], "IGP")|| strArray[6].contains("{")){
                        continue;
                    }

                    // v6  v4
                    String [] ipFor = strArray[5].split("/");
                    if(ipFor[0].contains(":")){
                        powSize = 128 - Integer.parseInt(ipFor[1]);
                    }else{
                        powSize = 32 - Integer.parseInt(ipFor[1]);
                    }

                    // 去重
                    String [] lineArray = strArray[6].split(" ");
                    if (lineArray.length < 2) {
                        continue;
                    }
                    String key = lineArray[0] + lineArray[1] + strArray[5].split("/")[0];
                    if(IPPowMap.get(key)==null || IPPowMap.get(key) < powSize){
                        IPPowMap.put(key, powSize);
                        AsDetectBean asDetectBean = new AsDetectBean(centralAs, centralAs + " " + strArray[6],
                                strArray[5], powSize);
                        asDetectBeanList.add(asDetectBean);
                    }

                    a--;
                    if(a==0)
                        break;
                }
                file.close();
            } else {
                while ((str = file.readLine()) != null) {
                    String[] strArray = str.split("\t");
                    if (strArray[0].split("/").length != 2) {
                        continue;
                    }

                    // v6 v4
                    String [] ipFor = strArray[5].split("/");
                    if(ipFor[0].contains(":")){
                        // v6
                        powSize = 128 - Integer.parseInt(ipFor[1]);
                    }else{
                        powSize = 32 - Integer.parseInt(ipFor[1]);
                    }

                    // 去重
                    String [] lineArray = strArray[13].split("|");
                    String key = centralAs + lineArray[0] + strArray[0].split("/")[0];
                    if(IPPowMap.get(key)==null || IPPowMap.get(key) < powSize){
                        IPPowMap.put(key, powSize);
                        AsDetectBean asDetectBean = new AsDetectBean(centralAs,
                                centralAs + " " + strArray[13].replace("|"," "),
                                strArray[0], powSize);
                        asDetectBeanList.add(asDetectBean);
                    }

                    a--;
                    if(a==0)
                        break;
                }
                file.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //第一步：设置输出的文件路径
        //如果该目录下不存在该文件，则文件会被创建到指定目录下。如果该目录有同名文件，那么该文件将被覆盖。
        File writeFile = new File(outPath);
        try{
            //第二步：通过BufferedReader类创建一个使用默认大小输出缓冲区的缓冲字符输出流
            BufferedWriter writeText = new BufferedWriter(new FileWriter(writeFile));
            //第三步：将文档的下一行数据赋值给lineData，并判断是否为空，若不为空则输出
            writeText.write("begin,line,ip,pow");
            for(AsDetectBean asDetectBean : asDetectBeanList){
                writeText.newLine();    //换行
                //调用write的方法将字符串写到流中
                writeText.write(asDetectBean.getStartAsId()+","+
                        asDetectBean.getAsList()+","+
                        asDetectBean.getIpArea()+","+
                        asDetectBean.getPowSize());
            }
            //使用缓冲区的刷新方法将数据刷到目的地中
            writeText.flush();
            //关闭缓冲区，缓冲区没有调用系统底层资源，真正调用底层资源的是FileWriter对象，缓冲区仅仅是一个提高效率的作用
            //因此，此处的close()方法关闭的是被缓存的流对象
            writeText.close();
        }catch (FileNotFoundException e){
            System.out.println("没有找到指定文件");
        }catch (IOException e){
            System.out.println("文件读写出错");
        }
    }

    public static void main(String[] args) {
        // 逐行读取
        boolean isChinaData = false;
        String inPath = "/Users/mac/desktop/bgpdump_full_snapshot.txt";
//        String inPath = "/Users/mac/desktop/indoor.txt";
        String outPath = "/Users/mac/desktop/snapJava1000.csv";
        String centralAs = "14315";

        ReadTextAsCsv readTextAsCsv = new ReadTextAsCsv();
        readTextAsCsv.dataPreProcess(inPath, outPath, isChinaData, centralAs);

    }

}