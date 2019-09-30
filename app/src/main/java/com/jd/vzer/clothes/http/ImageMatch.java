package com.jd.vzer.clothes.http;


import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class ImageMatch {
    public static HashMap<String, String> match(String skuId) {
        HashMap<String, String> matches = new HashMap<String, String>();
        try {
            String urlpath = "http://172.18.160.13/match";
            URL url = new URL(urlpath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");//设置参数类型是json格式
            connection.connect();
            JSONObject json = new JSONObject();
            json.put("skuId", skuId);
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(json.toString());
            writer.close();
//      Log.d("request string is ", json.toString());
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();

                StringBuilder sb = new StringBuilder();
                String line;

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                String str = sb.toString();
//        Log.d("result mathes str is ", str);
                json = new JSONObject(str);
                Iterator iterator = json.keys();
                while (iterator.hasNext()) {
                    String skuid = (String) iterator.next();
                    String main_url = json.getString(skuid);
                    matches.put(skuid, main_url);
                }
//        Log.d("result mathes is =====", matches.toString());
            }
        } catch (Exception e) {
            Log.e("http", "Error:" + e.getMessage());
            return null;
        }
        return matches;
    }
}
