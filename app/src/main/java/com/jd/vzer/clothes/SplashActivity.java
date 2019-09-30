package com.jd.vzer.clothes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

  private static final String[] requestPermissions = {
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.CAMERA,
      Manifest.permission.INTERNET
  };
  private int mRequestCode = 001;
  private List<String> mPermissionList = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    initPermission();
  }

  private void initPermission() {
    mPermissionList.clear();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      for (String permission : requestPermissions) {
        if (ActivityCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED) {
          mPermissionList.add(permission);
        }
      }
      if (mPermissionList.size() > 0) {
        ActivityCompat.requestPermissions(this, requestPermissions, mRequestCode);
      } else {
        startMain();
      }
    } else {
      startMain();
    }
  }

  private void startMain() {
    startActivity(new Intent(this, MainActivity.class));
    finish();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    startMain();
  }
}
