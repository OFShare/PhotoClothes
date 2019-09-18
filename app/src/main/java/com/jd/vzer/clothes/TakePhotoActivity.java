//package com.jd.vzer.clothes;
//
//import android.app.Activity;
//import android.hardware.Camera;
//import android.os.Bundle;
//import android.view.TextureView;
//import android.view.View;
//import android.widget.TextView;
//
//public class TakePhotoActivity extends Activity implements Camera.PreviewCallback {
//  private TextureView textureView;
//  private TextView takeBtn;
//  private static Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
//  private boolean takePhoto = false;
//
//  @Override
//  public void onCreate(Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//    setContentView(R.layout.activity_takephoto);
//    initView();
//  }
//
//  private void initView() {
//    CameraConnection cameraConnection = new CameraConnection(this, this, DESIRED_PREVIEW_SIZE);
//    textureView = findViewById(R.id.textureView);
//    takeBtn = findViewById(R.id.takeBtn);
//    cameraConnection.initCamera(textureView);
//    takeBtn.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        takePhoto = true;
//        v.setClickable(false);
//      }
//    });
//  }
//
//  @Override
//  public void onPreviewFrame(byte[] data, Camera camera) {
//    if (takePhoto) {
//      savePreview(data, camera);
//      takePhoto = false;
//    }
//  }
//
////  private void savePreview(byte[] data, Camera camera) {
////    ThreadManager.getLongPool().execute(() -> {
////      Camera.Size size = camera.getParameters().getPreviewSize();
////      Bitmap bitmap = BitmapUtil.rotateBitmap(BitmapUtil.compressYUVtoBitmap(this, data, size.width, size.height), -90);
////      String filePath = FileHelper.getInstance().getUserFilePath(Constract.username);
////      BitmapUtil.saveBitmap(bitmap, filePath);
////      Facial[] facials = UserPresenter.getInstance().registerUserSync(filePath);
////      if (facials != null && facials.length > 0) {
////        Constract.facial = facials[0];
////        Constract.userLocalImg = filePath;
////        LogUtil.i("人脸识别，本地用户特征值提取, " + facials.length);
////        runOnUiThread(() -> {
////          ToastUtil.show(BaseApplication.context, "特征值提取成功");
////          finish();
////        });
////      } else {
////        Constract.facial = null;
////        LogUtil.i("人脸识别，本地用户特征值提取,error");
////        runOnUiThread(() -> {
////          ToastUtil.show(BaseApplication.context, "特征值提取失败，请重试");
////        });
////      }
////    });
////  }
//}