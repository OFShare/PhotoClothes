package com.jd.vzer.clothes;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

import java.util.List;

class Size {
  int width;
  int height;
  Size(int w_, int h_) {
    width = w_;
    height = h_;
  }
  public int getHeight() {
    return height;
  }
  public int getWidth() {
    return width;
  }
}

class Constants {
  static int DisplayOrientation = 0;
}

public class CameraConnection {
  private Camera camera;
  private Camera.PreviewCallback imageListener;
  private Size desiredSize;
  static int cameraId;
  Activity activity;
  private SurfaceTexture surfaceTexture;

  public CameraConnection(Activity activity, final Camera.PreviewCallback imageListener, final Size desiredSize) {
    this.imageListener = imageListener;
    this.desiredSize = desiredSize;
    this.activity = activity;
  }

  public boolean initCamera(TextureView textureView) {
    cameraId = getCameraId();
    if (textureView.isAvailable()) {
      surfaceTexture = textureView.getSurfaceTexture();
      startPreview();
    } else {
      textureView.setSurfaceTextureListener(surfaceTextureListener);
    }
    if (cameraId < 0) {
      Toast.makeText(activity, "No Camera Detected", Toast.LENGTH_SHORT).show();
    }
    return true;
  }

  private void startPreview() {
    boolean init = false;
    try {
      camera = Camera.open(cameraId);
      init = true;
    } catch (Exception e) {
      e.printStackTrace();
      Log.e("camera connect error","Fail to connect to camera service");
    }
    if (!init) {
      Toast.makeText(activity, "open Camera failed", Toast.LENGTH_LONG).show();
      activity.finish();
      return;
    }
    try {
      Camera.Parameters parameters = camera.getParameters();
      List<String> focusModes = parameters.getSupportedFocusModes();
      if (focusModes != null
          && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
      }
      List<Camera.Size> cameraSizes = parameters.getSupportedPreviewSizes();
      Size[] sizes = new Size[cameraSizes.size()];
      int i = 0;
      for (Camera.Size size : cameraSizes) {
        sizes[i++] = new Size(size.width, size.height);
      }
      parameters.setPreviewSize(desiredSize.getWidth(), desiredSize.getHeight());
      camera.setDisplayOrientation(Constants.DisplayOrientation);
      camera.setParameters(parameters);
      camera.setPreviewTexture(surfaceTexture);
      camera.setErrorCallback(new Camera.ErrorCallback() {
        @Override
        public void onError(int error, Camera camera) {
          stopCamera();
          Log.e("Exception", "相机自己停止error：" + error);
        }
      });
      camera.setPreviewCallback(imageListener);
      camera.startPreview();
      Log.d("desiredSize","width:" + desiredSize.getWidth() + " height:" + desiredSize.getHeight());
    } catch (Exception exception) {
      exception.printStackTrace();
      Log.e("Exception", exception.toString());
      stopCamera();
    }
  }

  public void stopCamera() {
    if (camera != null) {
      camera.stopPreview();
      camera.setPreviewCallback(null);
      camera.release();
      camera = null;
    }
  }

  private int getCameraId() {
    Camera.CameraInfo ci = new Camera.CameraInfo();
    int size = Camera.getNumberOfCameras();
    if (size >= 2) {
      for (int i = 0; i < size; i++) {
        Camera.getCameraInfo(i, ci);
        if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
          return i;
        }
      }
    } else if (size == 1) {
      return 0;
    }
    return -1; // 没有摄像头
  }

  private final TextureView.SurfaceTextureListener surfaceTextureListener =
      new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture texture, final int width, final int height) {
          surfaceTexture = texture;
          startPreview();
        }

        @Override
        public void onSurfaceTextureSizeChanged(
            final SurfaceTexture texture, final int width, final int height) {
//                    System.out.println("------");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
//                    System.out.println("------");

          return true;
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture texture) {
//                    System.out.println("------");

        }
      };
}