package com.jd.vzer.clothes;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jd.vzer.clothes.http.ImageUploader;

import org.xml.sax.ErrorHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Camera.PreviewCallback {
  private static final int REQUEST_CODE_FILECHOOSER = 1;
  private Button mSelectBtn;
  private ImageView mShowIv;
  private LinearLayout mClothesView;
  private ImageView mClothesIV;
  private TextureView textureView;
  private ImageView imageView;

  /*随便搞的假数据*/
  private ArrayList<String> imgList = new ArrayList<>();

  /*记录上一次点击的View*/
  private View preView;
  /*选中颜色值*/
  private int colorUnSelect;
  private int colorSelect;
  /*当前选中衣服url*/
  private String clothesUrl;

  /*权限相关*/
  private final String CAMERA_PREMISSION = "android.permission.CAMERA";
  private final String WRITE_PREMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
  private final int CAMERA_CODE = 10;
  private static Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initView();
    initData();
    startCamera();
  }

  private void startCamera() {
    CameraConnection cameraConnection = new CameraConnection(this, this, DESIRED_PREVIEW_SIZE);
    cameraConnection.initCamera(textureView);
  }

  private void initView() {
    textureView = findViewById(R.id.textureView);
    imageView = findViewById(R.id.imageView);
    mSelectBtn = findViewById(R.id.btn_select_picture);
    mShowIv = findViewById(R.id.iv_user_show);
    mClothesView = findViewById(R.id.sv_clothes_show);
    mClothesIV = findViewById(R.id.iv_clothes_show);
    mSelectBtn.setOnClickListener(selectListener);
  }

  private void initData() {
    colorSelect = getResources().getColor(R.color.colorPrimaryDark);
    colorUnSelect = getResources().getColor(R.color.colorWhite);
    imgList.add("http://pic8.nipic.com/20100726/4955600_124426101612_2.jpg");
    imgList.add("http://pic7.nipic.com/20100429/809626_103543002699_2.jpg");
    imgList.add("http://pic26.nipic.com/20121213/10143726_111843041304_2.jpg");
    imgList.add("http://pic78.nipic.com/file/20150916/21196032_182144578000_2.jpg");
    imgList.add("http://pic39.nipic.com/20140312/6608733_214616579000_2.jpg");
    imgList.add("http://img1.cache.netease.com/catchpic/A/A9/A99354C0C691252F77F6F724FF9EC5A3.jpg");
    imgList.add("http://pic111.nipic.com/file/20161001/14764927_164235222032_2.jpg");

    imgList.add("http://pic8.nipic.com/20100726/4955600_124426101612_2.jpg");
    imgList.add("http://pic7.nipic.com/20100429/809626_103543002699_2.jpg");
    imgList.add("http://pic26.nipic.com/20121213/10143726_111843041304_2.jpg");
    imgList.add("http://pic78.nipic.com/file/20150916/21196032_182144578000_2.jpg");
    imgList.add("http://pic39.nipic.com/20140312/6608733_214616579000_2.jpg");
    imgList.add("http://img1.cache.netease.com/catchpic/A/A9/A99354C0C691252F77F6F724FF9EC5A3.jpg");
    imgList.add("http://pic111.nipic.com/file/20161001/14764927_164235222032_2.jpg");

    imgList.add("http://pic8.nipic.com/20100726/4955600_124426101612_2.jpg");
    imgList.add("http://pic7.nipic.com/20100429/809626_103543002699_2.jpg");
    imgList.add("http://pic26.nipic.com/20121213/10143726_111843041304_2.jpg");
    imgList.add("http://pic78.nipic.com/file/20150916/21196032_182144578000_2.jpg");
    imgList.add("http://pic39.nipic.com/20140312/6608733_214616579000_2.jpg");
    imgList.add("http://img1.cache.netease.com/catchpic/A/A9/A99354C0C691252F77F6F724FF9EC5A3.jpg");
    imgList.add("http://pic111.nipic.com/file/20161001/14764927_164235222032_2.jpg");

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mClothesIV.getLayoutParams();
    for (int i = 0; i < imgList.size(); i++) {
      String url = imgList.get(i);
      ImageView itemView = new ImageView(this);
      itemView.setTag(R.id.btn_select_picture, url);
      itemView.setLayoutParams(params);
      //网络图片加载
      Glide.with(MainActivity.this)
          .load(url)
          .into(itemView);
      itemView.setOnClickListener(clothesListener);
      mClothesView.addView(itemView);
    }
    mClothesIV.setVisibility(View.GONE);
  }

  private View.OnClickListener clothesListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      String url = (String) v.getTag(R.id.btn_select_picture);
      if (url != null) {
        //记录当前选中衣服url
        clothesUrl = url;
        //置空上一次选中背景色
        if (preView != null) {
          preView.setBackgroundColor(colorUnSelect);
        }
        v.setBackgroundColor(colorSelect);
        preView = v;
      }
    }
  };

  /*拿到当前选择的 衣服Url*/
  public String getCurClothUrl() {
    return clothesUrl;
  }

  private View.OnClickListener selectListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      //判断是否有权限，没有就申请
      if (PackageManager.PERMISSION_GRANTED
          == ActivityCompat.checkSelfPermission(MainActivity.this, CAMERA_PREMISSION)) {
        //有权限，打开相机、相册选择器
        startSelectPhoto();
      } else {
        //申请权限
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{CAMERA_PREMISSION,
                                                                          WRITE_PREMISSION}, CAMERA_CODE);
      }

    }
  };

  /*跳转打开相机、相册选择器*/
  private void startSelectPhoto() {
    Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
    imageIntent.addCategory(Intent.CATEGORY_OPENABLE);
    Intent chooser = createChooserIntent(createCameraIntent());
    imageIntent.setType("image/*");
    chooser.putExtra(Intent.EXTRA_INTENT, imageIntent);
    startActivityForResult(chooser, REQUEST_CODE_FILECHOOSER);
  }

  /**
   * 创建IntentChooser
   *
   * @param intents
   * @return
   */
  private static Intent createChooserIntent(Intent... intents) {
    Intent chooser = new Intent(Intent.ACTION_CHOOSER);
    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
    chooser.putExtra(Intent.EXTRA_TITLE, "选择图片");
    return chooser;
  }

  /**
   * 创建跳转系统相机的intent
   *
   * @return
   */
  public Intent createCameraIntent() {
    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    // 添加运行时权限
    cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, createCameraTempFile());
    return cameraIntent;
  }

  /**
   * 创建相机拍照的临时文件Uri
   *
   * @return
   */
  public Uri createCameraTempFile() {
    Uri uri;
    File imgFile =
        new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "vzer/cameraTempPic.jpg");
    if (!imgFile.exists()) {
      imgFile.getParentFile().mkdirs();
    }
    if (Build.VERSION.SDK_INT >= 24) {
      uri = FileProvider.getUriForFile(MainActivity.this, "com.jd.vzer.clothes", imgFile);
    } else {
      uri = Uri.parse(imgFile.toString());
    }
    return uri;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == REQUEST_CODE_FILECHOOSER) {
      Uri result;
      if (intent != null) {
        result = intent.getData();
        if (result == null) {
          // 返回的Uri为空即代表通过相机拍照上传
          result = createCameraTempFile();
        }
      } else {
        result = createCameraTempFile();
      }
      try {
        mShowIv.setImageBitmap(getBitmapFormUri(MainActivity.this, result));

      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == CAMERA_CODE) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        //跳转相册选
        startSelectPhoto();
      } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
        Toast.makeText(this, "权限申请失败，不给权限打不开相机哦~", Toast.LENGTH_SHORT).show();
      }
    }
  }

  /*通过Uri拿到bitmap*/
  public Bitmap getBitmapFormUri(Context context, Uri uri) throws FileNotFoundException, IOException {
    InputStream input = context.getContentResolver().openInputStream(uri);

    //这一段代码是不加载文件到内存中也得到bitmap的真是宽高，主要是设置inJustDecodeBounds为true
    BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
    onlyBoundsOptions.inJustDecodeBounds = true;//不加载到内存
    onlyBoundsOptions.inDither = true;//optional
    onlyBoundsOptions.inPreferredConfig = Bitmap.Config.RGB_565;//optional
    BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
    input.close();
    int originalWidth = onlyBoundsOptions.outWidth;
    int originalHeight = onlyBoundsOptions.outHeight;
    if ((originalWidth == -1) || (originalHeight == -1))
      return null;

    //图片分辨率以1080p为标准
    float hh = 1920f;
    float ww = 1080f;
    //缩放比，由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
    int be = 1;//be=1表示不缩放
    if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
      be = (int) (originalWidth / ww);
    } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
      be = (int) (originalHeight / hh);
    }
    if (be <= 0)
      be = 1;
    //比例压缩
    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
    bitmapOptions.inSampleSize = be;//设置缩放比例
    bitmapOptions.inDither = true;
    bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
    input = context.getContentResolver().openInputStream(uri);
    Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
    input.close();

    return compressImage(bitmap);//再进行质量压缩
  }

  /*图片质量压缩*/
  public static Bitmap compressImage(Bitmap image) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
    int options = 100;
    while (baos.toByteArray().length / 1024 > 1000) {  //循环判断如果压缩后图片是否大于1000kb,大于继续压缩
      baos.reset();//重置baos即清空baos
      //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
      image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options，把压缩后的数据存放到baos中
      options -= 10;//每次都减少10
      if (options <= 0)
        break;
    }
    ByteArrayInputStream isBm =
        new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
    Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
    return bitmap;
  }

  //摄像头回调,数据格式是(camera API 1)yuv默认格式NV21
  @Override
  public void onPreviewFrame(byte[] data, Camera camera) {
    try {
      Camera.Size size = camera.getParameters().getPreviewSize();
      YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
      if (image != null) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);

        Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
        //对位图进行处理，如显示，保存等
        final Bitmap bitmap = bmp;

        Thread thread = new Thread(new Runnable() {
          @Override
          public void run() {
            try  {
              //Your code goes here
              ImageUploader.upLoad();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
        thread.start();

        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            imageView.setImageBitmap(bitmap);
          }
        });

        stream.close();
      }
    } catch (Exception ex) {
      Log.e("yuv convert to bitmap", "Error:" + ex.getMessage());
    }

  }
}