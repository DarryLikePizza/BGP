package com.example.bgpm.utils;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JsonArraysSorted {
    public static JSONArray jsonArraySort(JSONArray jsonArr, String sortKey, boolean is_desc) throws JSONException {
        //存放排序结果json数组
        JSONArray sortedJsonArray = new JSONArray();
        //用于排序的list
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        //将参数json数组每一项取出，放入list
        for (int i = 0; i < jsonArr.length(); i++) {
            jsonValues.add(jsonArr.getJSONObject(i));
        }
        //快速排序，重写compare方法，完成按指定字段比较，完成排序
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            //排序字段
            private  final String KEY_NAME = sortKey;
            //重写compare方法
            @Override
            public int compare(JSONObject a, JSONObject b) {
                //如果用String接会导致一位数和两位数混合比对的时候不能准确比出来，要用int类型接
                //String valA = new String();
                //String valB = new String();
                Integer valA = 0;
                Integer valB = 0;
                try {
                    valA = Integer.parseInt(a.getString(KEY_NAME));
                    valB = Integer.parseInt(b.getString(KEY_NAME));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //是升序还是降序
                if (is_desc){
                    return -valA.compareTo(valB);
                } else {
                    return -valB.compareTo(valA);
                }

            }
        });
        //将排序后结果放入结果jsonArray
        for (int i = 0; i < jsonArr.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }
        return sortedJsonArray;
    }
}
