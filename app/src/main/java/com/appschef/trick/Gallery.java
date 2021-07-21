package com.appschef.trick;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Gallery extends AppCompatActivity implements AdapterView.OnItemClickListener {
    File[] totalFiles;
//    MyRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        findViewById(R.id.back).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        TextView floatingActionButton = findViewById(R.id.add);
        floatingActionButton.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 111);
        });

initial();
    }

//    @Override
//    public void onItemClick(View view, int position) {
//        String files = totalFiles[position].getAbsolutePath();
//        Intent intent;
//        if (files.contains("mp4")) {
//            intent = new Intent(this, VideoEditor.class);
//        } else {
//            intent = new Intent(this, ImageEditor.class);
//        }
//        intent.putExtra("file", files);
//        startActivity(intent);
//    }
void initial(){

    GridView gridView = findViewById(R.id.select);
    String sb = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator +
            "Tricks";
    this.totalFiles = new File(sb).listFiles();

    if (totalFiles != null) {
//        for (int i = 0; i < this.totalFiles.length / 2; i++) {
//            File temp = this.totalFiles[i];
            Arrays.sort(this.totalFiles, Collections.reverseOrder());
//            this.totalFiles[i] = this.totalFiles[this.totalFiles.length - i - 1];
//            this.totalFiles[this.totalFiles.length - i - 1] = temp;
//        }
        gridView.setAdapter(new GalleryAdapter(this, this.totalFiles));
        gridView.setOnItemClickListener(Gallery.this);
    }
}
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == RESULT_OK && null != data) {
//progressDialog=ProgressDialog.show(Gallery.this,"");

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            saveImage(bitmap);
        }
    }

    private void saveImage(Bitmap bitmap) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        LinearLayout linearLayout=findViewById(R.id.progress);
        executor.execute(() -> {

            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.post(() -> linearLayout.setVisibility(View.VISIBLE));

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
                linearLayout.setVisibility(View.GONE);
                Log.d("saveImage", fileTosave.getAbsolutePath());
                MediaScannerConnection.scanFile(Gallery.this,
                        new String[]{fileTosave.toString()},
                        null, null);
                initial();
            });
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String files = totalFiles[position].getAbsolutePath();
        Intent intent;
        if (files.contains("mp4")) {
            intent = new Intent(this, VideoEditor.class);
        } else {
            intent = new Intent(this, ImageEditor.class);
        }
        intent.putExtra("file", files);
        startActivity(intent);
    }
}