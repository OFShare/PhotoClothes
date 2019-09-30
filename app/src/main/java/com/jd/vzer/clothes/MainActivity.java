package com.jd.vzer.clothes;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
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
import com.jd.vzer.clothes.http.ImageMatch;
import com.jd.vzer.clothes.http.ImageUploader;

import org.xml.sax.ErrorHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements Camera.PreviewCallback {
    private static final int REQUEST_CODE_FILECHOOSER = 1;
    //  private Button mSelectBtn;
//  private ImageView mShowIv;
    //  用户选择的图片放到的布局
    private LinearLayout mClothesView;
    // 用户选择的图片
    private ImageView mClothesIV;
    // 用户是否点击
    private boolean isClickFinish = false;
    // 相机预览
    private TextureView textureView;
    // 算法返回的试穿图片
    private ImageView imageView;
    // 算法返回的推荐图片放到的布局
    private LinearLayout mClothesRecommendedLayout;
    // 推荐图片的ImageView
    private ImageView mClothesRecommendedView;
    // 本地预先存的图片
    private String prefixDir = "/sdcard/imba/images/";
    // 本地图片数据
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
    private final float sizeRatio = 480.0f / 640.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        startCamera();
    }

    private void match() {
        // 子线程处理http网络, 再到UI主线程显示ImageView
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String urlTmp = getCurClothUrl();
                // 通过网络http将衣服的sku_id传输给后台
                String[] words = urlTmp.split("/|\\.");
                String skuId = words[words.length - 2];
                Log.d("## skuId", skuId);
                HashMap<String, String> matches = ImageMatch.match(skuId);
                final Set<String> keys = matches.keySet();
                Log.d("## keys ", keys.toString());

                // UI主线程处理ImageView
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 先清空，再读取本地图片
                        mClothesRecommendedLayout.removeAllViewsInLayout();
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mClothesRecommendedView.getLayoutParams();
                        for (String path : keys) {
                            String urlTmp = prefixDir + path + ".jpg";
                            Log.d("## urlTmp", urlTmp);
                            ImageView recommend = new ImageView(MainActivity.this);
                            recommend.setLayoutParams(params);
                            recommend.setImageURI(Uri.fromFile(new File(urlTmp)));
                            mClothesRecommendedLayout.addView(recommend);
                        }
                    }
                });
            }
        });
        thread.start();
    }

    private void startCamera() {
        CameraConnection cameraConnection = new CameraConnection(this, this, DESIRED_PREVIEW_SIZE);
        cameraConnection.initCamera(textureView);
    }

    private void initView() {
        mClothesRecommendedLayout = findViewById(R.id.sv_clothes_recommended);
        mClothesRecommendedView = findViewById(R.id.iv_clothes_show_recommend);

        textureView = findViewById(R.id.textureView);
        //view绘制完成后回调此方法
        textureView.post(new Runnable() {
            @Override
            public void run() {
                // float height = textureView.getHeight();
                // float width = height * sizeRatio;
                // textureView.getLayoutParams().width = (int) width;
                Log.d("### width", textureView.getWidth() + "宽");
                Log.d("### height", textureView.getHeight() + "高");
            }
        });
        imageView = findViewById(R.id.imageView);
//        imageView.post(new Runnable() {
//            @Override
//            public void run() {
//                // float height = textureView.getHeight();
//                // float width = height * sizeRatio;
//                // textureView.getLayoutParams().width = (int) width;
//                imageView.getLayoutParams().height = textureView.getHeight();
//                imageView.getLayoutParams().width = textureView.getWidth();
//            }
//        });
//    mSelectBtn = findViewById(R.id.btn_select_picture);
//    mShowIv = findViewById(R.id.iv_user_show);
        mClothesView = findViewById(R.id.sv_clothes_show);
        mClothesIV = findViewById(R.id.iv_clothes_show);

//    mSelectBtn.setOnClickListener(selectListener);
    }

    private void initData() {
        colorSelect = getResources().getColor(R.color.colorPrimaryDark);
        colorUnSelect = getResources().getColor(R.color.colorWhite);
        imgList.add(prefixDir + "100000416881.jpg");
        imgList.add(prefixDir + "100002516951.jpg");
        imgList.add(prefixDir + "100002534987.jpg");
        imgList.add(prefixDir + "100002601331.jpg");
        imgList.add(prefixDir + "100002763411.jpg");
        imgList.add(prefixDir + "100002876563.jpg");
        imgList.add(prefixDir + "100002889858.jpg");
        imgList.add(prefixDir + "100003105257.jpg");
        imgList.add(prefixDir + "100003801816.jpg");
        imgList.add(prefixDir + "100004177932.jpg");
        imgList.add(prefixDir + "100004178038.jpg");
        imgList.add(prefixDir + "100004199669.jpg");
        imgList.add(prefixDir + "100004203747.jpg");
        imgList.add(prefixDir + "100004336342.jpg");
        imgList.add(prefixDir + "100004339624.jpg");
        imgList.add(prefixDir + "100004370131.jpg");
        imgList.add(prefixDir + "100004373726.jpg");
        imgList.add(prefixDir + "100004652706.jpg");
        imgList.add(prefixDir + "100005567910.jpg");
        imgList.add(prefixDir + "100005714558.jpg");
        imgList.add(prefixDir + "100005750904.jpg");
        imgList.add(prefixDir + "100005845226.jpg");
        imgList.add(prefixDir + "100006410900.jpg");
        imgList.add(prefixDir + "100006555878.jpg");
        imgList.add(prefixDir + "100007273834.jpg");
        imgList.add(prefixDir + "100007307312.jpg");
        imgList.add(prefixDir + "100008251256.jpg");
        imgList.add(prefixDir + "44818587254.jpg");
        imgList.add(prefixDir + "45134493631.jpg");
        imgList.add(prefixDir + "4807482.jpg");
        imgList.add(prefixDir + "48640816437.jpg");
        imgList.add(prefixDir + "5157444.jpg");
        imgList.add(prefixDir + "53812177159.jpg");
        imgList.add(prefixDir + "54322272983.jpg");
        imgList.add(prefixDir + "56003468528.jpg");
        imgList.add(prefixDir + "56064800585.jpg");
        imgList.add(prefixDir + "56588754543.jpg");
        imgList.add(prefixDir + "6531895.jpg");
        imgList.add(prefixDir + "6564584.jpg");
        imgList.add(prefixDir + "6676229.jpg");

//        imgList.add( prefixDir + "100000416881.jpg");

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mClothesIV.getLayoutParams();
        for (int i = 0; i < imgList.size(); i++) {
            String url = imgList.get(i);
            ImageView itemView = new ImageView(this);
            itemView.setTag(R.id.iv_clothes_show, url);
            itemView.setLayoutParams(params);
            // 网络图片加载
//            Glide.with(MainActivity.this)
//                    .load(url)
//                    .into(itemView);
            // 读取本地图片
            itemView.setImageURI(Uri.fromFile(new File(url)));
            itemView.setOnClickListener(clothesListener);
            mClothesView.addView(itemView);
        }
        mClothesIV.setVisibility(View.GONE);
    }

    // 用户选择图片时的点击事件
    private View.OnClickListener clothesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String url = (String) v.getTag(R.id.iv_clothes_show);
            if (url != null) {
                // 记录当前选中衣服url
                clothesUrl = url;
                // 置空上一次选中背景色
                if (preView != null) {
                    preView.setBackgroundColor(colorUnSelect);
                }
                v.setBackgroundColor(colorSelect);
                preView = v;
                isClickFinish = true;

                match();
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
//        mShowIv.setImageBitmap(getBitmapFormUri(MainActivity.this, result));

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

    public Bitmap getBitmap(String s) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(s);
            bitmap = BitmapFactory.decodeStream(url.openStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    boolean isFinish = true;

    // 摄像头回调,数据格式是(camera API 1)yuv默认格式NV21
    // onPreviewFrame会一直回调，所以需要用个isFinish变量判断是否当前的执行完
    // bitmap需要手动主动释放
    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        try {
            if (!isFinish) return;
            isFinish = false;

            // 在一个子线程里处理bitmap，http网络传输
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Your code goes here
                        Camera.Size size = camera.getParameters().getPreviewSize();
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                        Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());

//                        Matrix matrix = new Matrix();
//                        matrix.setRotate(270);
//                        // 围绕原地进行旋转
//                        final Bitmap newBM =
//                                Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
//                        //对位图进行处理，如显示，保存等


                        // upload image
                        //将bitmap保存为本地文件
                        // File PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + "imba");//设置保存路径
                        // File avaterFile = new File(PHOTO_DIR, "avater.jpg");//设置文件名称
//                        File avaterFile = new File("/sdcard/imba/test.jpg");
//                        if (avaterFile.exists()) {
//                            avaterFile.delete();
//                        }
//                        try {
//                            avaterFile.createNewFile();
//                            FileOutputStream fos = new FileOutputStream(avaterFile);
//                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                            fos.flush();
//                            fos.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        // 通过网络传输本地的图片
                        String path = getCurClothUrl();
                        FileInputStream fis = new FileInputStream(path);
                        Bitmap choosedCloth = BitmapFactory.decodeStream(fis);

//                        Bitmap choosedCloth = getBitmap("http://172.18.160.13/static/cloth.jpg");
                        Bitmap bitmap = ImageUploader.upLoad(choosedCloth, bmp);

                        if (bitmap == null) {
                            isFinish = true;
                            String logoPath = "/sdcard/imba/images/imba.jpg";
                            FileInputStream fiss = new FileInputStream(logoPath);
                            Bitmap Logo = BitmapFactory.decodeStream(fiss);
                            bitmap = Logo;

                            // 获得图片的宽高
                            int width = bitmap.getWidth();
                            int height = bitmap.getHeight();
                            // 设置想要的大小
                            int newWidth = imageView.getWidth();
                            int newHeight = imageView.getHeight();
                            // 计算缩放比例
                            float scaleWidth = ((float) newWidth) / width;
                            float scaleHeight = ((float) newHeight) / height;
                            // 取得想要缩放的matrix参数
                            Matrix matrixx = new Matrix();
                            matrixx.postScale(scaleWidth, scaleHeight);
                            // 得到缩放后新的图片
                            final Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrixx, true);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageBitmap(newBitmap);
                                }
                            });

                            return;
                        }
//                        mClothesRecommended.addView();
                        // get image from guanglei
//                        Bitmap bitmap = null;
//                        try {
//                            // File avaterFile = new File(PHOTO_DIR, "avater.jpg");
//                            // File avaterFile = new File("/sdcard/imba/test_return.jpg");
//                            // if(avaterFile.exists()) { bitmap = BitmapFactory.decodeFile(PicTool.PHOTO_DIR+"/avater.jpg"); }
//                            if (avaterFile.exists()) {
//                            }
//                            bitmap = BitmapFactory.decodeFile("/sdcard/imba/return_test.jpg");
//
//                        } catch (Exception e) {
//                        }

                        // 获得图片的宽高
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        // 设置想要的大小
                        int newWidth = imageView.getWidth();
                        int newHeight = imageView.getHeight();
                        // 计算缩放比例
                        float scaleWidth = ((float) newWidth) / width;
                        float scaleHeight = ((float) newHeight) / height;
                        // 取得想要缩放的matrix参数
                        Matrix matrixx = new Matrix();
                        matrixx.postScale(scaleWidth, scaleHeight);
                        // 得到新的图片
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrixx, true);

                        final Bitmap newBM2 = bitmap;
                        // show result
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(newBM2);
                            }
                        });
                        stream.close();
                        isFinish = true;
                    } catch (Exception e) {
                        isFinish = true;
                        Log.e("onPreviewFrame", "Error:" + e.getMessage());
                    }
                }
            });
            thread.start();

        } catch (Exception e) {
            Log.e("onPreviewFrame", "Error:" + e.getMessage());
        }

    }
}