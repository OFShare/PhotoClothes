package com.jd.vzer.clothes.http;

import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUploader {

  public static void upLoad() {
    try {
      String urlpath = "http://172.20.10.3:5000/cloth";
      URL url = new URL(urlpath);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setUseCaches(false);
      connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");//设置参数类型是json格式
      connection.connect();

      String body =
          "{\"image_person\":\"zhangsan\",\"image_cloth\":\"123456\",\"uuid\":\"1\",\"format\":\"jpg\"}";
      BufferedWriter writer =
          new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
      writer.write(body);
      writer.close();

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        InputStream inputStream = connection.getInputStream();
//                String result = is2String(inputStream);//将流转换为字符串。
//                Log.d("kwwl","result============="+result);
      }

    } catch (Exception e) {
      Log.e("http", "Error:" + e.getMessage());
    }

  }

}