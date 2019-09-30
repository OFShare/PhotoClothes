package com.jd.vzer.clothes.http;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Base64;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONObject;
public class ImageUploader {

  public static String convertIconToString(Bitmap bitmap) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
    byte[] bytes = baos.toByteArray();// 转为byte数组
    return Base64.encodeToString(bytes, Base64.DEFAULT);

  }
  public static Bitmap convertStringToIcon(String str) {
    // OutputStream out;
    Bitmap bitmap = null;
    try {
      // out = new FileOutputStream("/sdcard/aa.jpg");
      byte[] bytes= Base64.decode(str, Base64.DEFAULT);
      bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
      // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
      return bitmap;
    } catch (Exception e) {
      return null;
    }
  }


  public static Bitmap upLoad(Bitmap cloth, Bitmap human) {
    Bitmap result_bmp = human;
    try {
      String urlpath = "http://172.18.160.13/cloth";
//      String urlpath = "http://192.168.43.151:5000/cloth";
      URL url = new URL(urlpath);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setUseCaches(false);
      connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");//设置参数类型是json格式
      connection.connect();
      long start1 = System.currentTimeMillis();
      String cloth_string = convertIconToString(cloth);
      String human_string = convertIconToString(human);
      long end1 = System.currentTimeMillis();
      Log.d("convertIconToString", (end1 -start1) + "ms");
      String body =
              "{\"image_person\":\"" + human_string + "\",\"image_cloth\":\"" + cloth_string + "\",\"uuid\":\"1\",\"format\":\"jpg\"}";
      BufferedWriter writer =
              new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
      writer.write(body);
      writer.close();
      long start2 = System.currentTimeMillis();
      int responseCode = connection.getResponseCode();
      long end2 = System.currentTimeMillis();
      Log.d("network time ", (end2 -start2) + "ms");
      if (responseCode == HttpURLConnection.HTTP_OK) {
        InputStream inputStream = connection.getInputStream();

        StringBuilder sb = new StringBuilder();
        String line;

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = br.readLine()) != null) {
          sb.append(line);
        }
        String str = sb.toString();
        JSONObject json = new JSONObject(str);
        String output_image = json.getString("output_image");
        String status = json.getString("status");
//        Log.d("result str========" , str);
        Log.d("result status========" , status);
        long start = System.currentTimeMillis();
        result_bmp = convertStringToIcon(output_image);
        long end = System.currentTimeMillis();
        Log.d("convertStringToIcon", (end -start) + "ms");
        if (status.equals(new String("ok"))){
          Log.d("REUTRN outputimage ====" , status);
          return result_bmp;
        }
        return null;

//                String result = is2String(inputStream);//将流转换为字符串。
//                Log.d("kwwl","result============="+result);
      }

    } catch (Exception e) {
      Log.e("http", "Error:" + e.getMessage());
    }
    return null;
  }

}
