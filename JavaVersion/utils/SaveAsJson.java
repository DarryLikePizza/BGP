package com.example.bgpm.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SaveAsJson {
    public boolean saveJsonData(String jsonString, String jsonFilePath) {
        boolean result = false;
        BufferedWriter bufferedWriter = null;
        try {
            File file = new File(jsonFilePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            bufferedWriter.write(jsonString);
            result = true;
        } catch (IOException e) {
            System.out.println("保存数据到json文件异常！" + e);
        } finally {
            if (null != bufferedWriter) {
                try {
                    bufferedWriter.close();
                } catch (IOException exception) {
                    System.out.println("保存数据到json文件后，关闭流异常" + exception);
                }
            }
        }
        return result;
    }
}