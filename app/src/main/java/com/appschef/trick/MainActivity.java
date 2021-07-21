package com.appschef.trick;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.impl.VideoCaptureConfig;
import androidx.camera.core.impl.utils.CameraOrientationUtil;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private TextView video, photo;
    private ImageView captured, capture, rotate, captureV;
    private CameraSelector cameraSelector;
    private Camera camera;
    private Integer camFace;
    private Boolean flash = false, rec = false, photoMode = true;
    private VideoCapture videoCapture;
    private ImageButton gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        captured = findViewById(R.id.captured);
        capture = findViewById(R.id.capture);
        captureV = findViewById(R.id.captureVideo);
        camFace = CameraSelector.LENS_FACING_BACK;
        cameraExecutor = Executors.newSingleThreadExecutor();
        rotate = findViewById(R.id.rotate);
        gallery = findViewById(R.id.save);
        video = findViewById(R.id.video);
        photo = findViewById(R.id.photo);
        final MediaPlayer[] mp = {MediaPlayer.create(this, R.raw.click)};
        perm();
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        initializePreview();
        capture.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED ||ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                perm();
            } else {
                mp[0].start();
                takePicture();
            }
        });
        final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds
        final long[] lastClickTime = {0};
        previewView.setOnClickListener(v -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime[0] < DOUBLE_CLICK_TIME_DELTA) {
                lastClickTime[0] = 0;
                rotateCam();
                //double click
            } else {
                //single click
            }
            lastClickTime[0] = clickTime;
        });
        rotate.setOnClickListener(v -> rotateCam());
        gallery.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Gallery.class)));
        captureV.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                perm();
            } else {
                if (!rec) {

                    mp[0].start();
                    mp[0].setOnCompletionListener(mp1 -> {
                        captureV.setImageResource(R.drawable.ic_baseline_stop_circle_24);
                        startVideo();
                        rec = true;
                    });
                } else {
                    rec = false;
                    captureV.setImageResource(R.drawable.ic_baseline_video_camera_back_24);
                    stopVideo();
                }
            }

        });
        photo.setOnClickListener(v -> {
            photo.setBackgroundColor(getColor(R.color.purple_900));
            video.setBackground(null);
            mp[0].release();
            mp[0] = MediaPlayer.create(this, R.raw.click);
            photoMode = true;
            initializePreview();
            findViewById(R.id.capture).setVisibility(View.VISIBLE);
            findViewById(R.id.captureVideo).setVisibility(View.GONE);

        });

        video.setOnClickListener(v -> {
            photo.setBackground(null);
            video.setBackgroundColor(getColor(R.color.purple_900));
            mp[0].release();
            mp[0] = MediaPlayer.create(this, R.raw.video);
            photoMode = false;
            initializePreviewV();
            findViewById(R.id.capture).setVisibility(View.GONE);
            findViewById(R.id.captureVideo).setVisibility(View.VISIBLE);
        });
    }

    void perm() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)) {
            initializePreview();
        }

    }

    @SuppressLint("RestrictedApi")
    void video(@NonNull ProcessCameraProvider cameraProvider) {
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(camFace)
                .build();
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        videoCapture = new VideoCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();
        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
    }

    void initializePreview() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                video(cameraProvider);
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void initializePreviewV() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                video(cameraProvider);
//                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("RestrictedApi")
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(camFace)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture =
                new ImageCapture.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                        .setTargetRotation(getDisplay().getRotation())
                        .build();

        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);

    }

    void rotateCam() {
        if (camFace == CameraSelector.LENS_FACING_BACK) {
            camFace = CameraSelector.LENS_FACING_FRONT;
        } else {
            camFace = CameraSelector.LENS_FACING_BACK;
        }
        if (rec) {
            captureV.performClick();
        }
        if (photoMode) {
            initializePreview();
        } else {
            initializePreviewV();
        }
    }

    private Bitmap getBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        buffer.rewind();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        image.close();
        return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("MyTrick", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void saveImage(Bitmap bitmap) {

        gallery.setEnabled(false);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            String sb = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator +
                    "Tricks";
            File file = new File(sb);
            if (!file.exists()) {
                file.mkdir();
            }

            String sb2 = System.currentTimeMillis() +
                    ".png";
            File fileTosave = new File(file, sb2);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(fileTosave);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
//            this.imageToSave.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                gallery.setEnabled(true);
                Log.d("saveImage", fileTosave.getAbsolutePath());
                MediaScannerConnection.scanFile(MainActivity.this,
                        new String[]{fileTosave.toString()},
                        null, null);
            });
        });

    }

    void takePicture() {
//                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + System.currentTimeMillis() + ".png");
//            File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".png");
        String sb = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator +
                "Tricks";
        File file = new File(sb);
        if (!file.exists()) {
            file.mkdir();
        }
        String sb2 = System.currentTimeMillis() + ".png";
        File file1 = new File(file, sb2);
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(file1).build();
        imageCapture.takePicture(outputFileOptions, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NotNull ImageCapture.OutputFileResults outputFileResults) {
                        MediaScannerConnection.scanFile(MainActivity.this,
                                new String[]{file1.toString()},
                                null, null);
                    }

                    @Override
                    public void onError(ImageCaptureException error) {
                        error.printStackTrace();
                        // insert your code here.
                    }
                }
        );
//        imageCapture.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
//            @Override
//            @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
//            public void onCaptureSuccess(@NonNull ImageProxy image) {
//                Bitmap bitmap = getBitmap(image);
//                Handler mHandler = new Handler(Looper.getMainLooper());
//                image.close();
//                mHandler.post(() -> {
//                    Matrix matrix = new Matrix();
//                    matrix.postRotate(90);
//                    if (camFace == CameraSelector.LENS_FACING_FRONT)
//                        matrix.preScale(-1.0f, 1.0f);
//                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//                    saveImage(rotatedBitmap);
//                });
//            }
//
//            @Override
//            public void onError(@NonNull ImageCaptureException exception) {
//                super.onError(exception);
//            }
//        });
    }

    @SuppressLint("RestrictedApi")
    void startVideo() {
        String sb = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator +
                "Tricks";
        File file = new File(sb);
        if (!file.exists()) {
            file.mkdir();
        }
        String sb2 = System.currentTimeMillis() +
                ".mp4";
        File file1 = new File(file, sb2);
        VideoCapture.OutputFileOptions outputFileOptions =
                new VideoCapture.OutputFileOptions.Builder(file1).build();
        try {
            videoCapture.startRecording(outputFileOptions, cameraExecutor, new VideoCapture.OnVideoSavedCallback() {

                @Override
                public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                    MediaScannerConnection.scanFile(MainActivity.this,
                            new String[]{file1.toString()},
                            null, null);
                }

                @Override
                public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {

                }
            });
        } catch (SecurityException securityException) {
            securityException.printStackTrace();
        }
    }

    @SuppressLint("RestrictedApi")
    void stopVideo() {
        videoCapture.stopRecording();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.flash);
        String str = "android.intent.action.VIEW";
        menuItem.setOnMenuItemClickListener(item -> {
            if (flash) {
                menuItem.setIcon(R.drawable.ic_baseline_flash_off_24);
                flash = false;
                camera.getCameraControl().enableTorch(false);
            } else {
                menuItem.setIcon(R.drawable.ic_baseline_flash_on_24);
                flash = true;
                camera.getCameraControl().enableTorch(true);
            }
            return false;
        });
//        MenuItem menuItem2 = menu.findItem(R.id.share_app);
//        menuItem2.setOnMenuItemClickListener(item -> {
//            Intent intent2 = new Intent(str);
//            intent2.setData(Uri.parse("https://play.google.com/store/apps/developer?id=Everyday+Apps+by+Appytome+Tech"));
//            startActivity(intent2);
//            return false;
//        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}