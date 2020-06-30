package com.verlif.nelson_library;

import java.util.HashMap;

public class LinkedData {

    private HashMap<String, Data> hashMap;

    public LinkedData() {
        hashMap = new HashMap<>();
    }

    public void addData(Data data) {
        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        hashMap.put(data.title, data);
    }

    public Data getData(String dataTitle) {
        return hashMap == null ? null : hashMap.get(dataTitle);
    }

    public HashMap<String, Data> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<String, Data> hashMap) {
        this.hashMap = hashMap;
    }

    public static class Data {
        /**
         * 数据标题, 用于标识数据
         */
        String title;
        /**
         * 数据类型, 与Data中的静态变量对应
         */
        String type;
        /**
         * 实际data数据
         */
        String data;
    }
}
