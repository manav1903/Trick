package com.appschef.trick;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;

import com.xiaopo.flying.sticker.DrawableSticker;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.TextSticker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Editor extends AppCompatActivity {
    StickerView stickerView;
    String textColor = "White";
    int[] category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        stickerView = findViewById(R.id.sticker_view);
        String path = getIntent().getStringExtra("file");
        ImageView imageView = findViewById(R.id.image);
        imageView.setImageURI(Uri.parse(path));
        onClick();
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);


    }

    void onClick() {
        findViewById(R.id.addtext).setOnClickListener(v -> showAddTextDialog());
        findViewById(R.id.addsticker).setOnClickListener(v -> showSelector());
    }

    void showSelector() {
        final Dialog dialog = new Dialog(Editor.this);
        dialog.setContentView(R.layout.dialog_category);
        dialog.setCancelable(true);
        dialog.show();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        dialog.getWindow().setLayout((6 * width) / 7, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.findViewById(R.id.emoji).setOnClickListener(v -> {
            category = StaticValues.stickerList;
            dialog.dismiss();
            showStickerSelectDialog();
        });
        dialog.findViewById(R.id.acc).setOnClickListener(v -> {
            category = StaticValues.acc;
            dialog.dismiss();
            showStickerSelectDialog();
        });
        dialog.findViewById(R.id.hair).setOnClickListener(v -> {
            category = StaticValues.hairStyle;
            dialog.dismiss();
            showStickerSelectDialog();
        });
        dialog.findViewById(R.id.all).setOnClickListener(v -> {
            category = StaticValues.all;
            dialog.dismiss();
            showStickerSelectDialog();
        });

    }

    void showStickerSelectDialog() {
        final Dialog dialog = new Dialog(Editor.this);
        dialog.setContentView(R.layout.dialog_sticker);
        dialog.setCancelable(true);
        dialog.show();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        dialog.getWindow().setLayout((6 * width) / 7, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        GridView gridView = dialog.findViewById(R.id.select);
        gridView.setAdapter(new StickerAdapter(this, category));
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            stickerView.addSticker(new DrawableSticker(ContextCompat.getDrawable(Editor.this, category[position])));
            dialog.dismiss();
        });
    }

    void showAddTextDialog() {
        final Dialog dialog = new Dialog(Editor.this);
        dialog.setContentView(R.layout.dialog_add_text);
        dialog.setCancelable(true);
        dialog.show();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        dialog.getWindow().setLayout((6 * width) / 7, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        String[] colors = {"White", "Black", "Purple", "Blue", "Green", "Pink", "Red"};
        Spinner spinner = dialog.findViewById(R.id.color);
        ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colors);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinner.setAdapter(aa);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                textColor = colors[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.add).setOnClickListener(v -> {
            EditText editText = dialog.findViewById(R.id.textarea);
            TextSticker myTextSticker = new TextSticker(this);
            myTextSticker.setText(editText.getText().toString());
            myTextSticker.setTextAlign(Layout.Alignment.ALIGN_CENTER);
            switch (textColor) {
                case "White":
                    myTextSticker.setTextColor(getColor(R.color.white));
                    break;
                case "Black":
                    myTextSticker.setTextColor(getColor(R.color.black));
                    break;
                case "Purple":
                    myTextSticker.setTextColor(getColor(R.color.purple_700));
                    break;
                case "Blue":
                    myTextSticker.setTextColor(getColor(R.color.blue));
                    break;
                case "Green":
                    myTextSticker.setTextColor(getColor(R.color.green));
                    break;
                case "Red":
                    myTextSticker.setTextColor(getColor(R.color.red));
                    break;
                case "Pink":
                    myTextSticker.setTextColor(getColor(R.color.pink));
                    break;
            }
            myTextSticker.resizeText();
            stickerView.addSticker(myTextSticker);
            dialog.dismiss();
        });
    }

    private void saveImage(Bitmap bitmap) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            String sb = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator +
                    "Tricks";
            File file = new File(sb);

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
                Log.d("saveImage", fileTosave.getAbsolutePath());
                MediaScannerConnection.scanFile(Editor.this,
                        new String[]{fileTosave.toString()},
                        null, null);
                Intent intent = new Intent(Editor.this, ImageEditor.class);
                intent.putExtra("file", fileTosave.getAbsolutePath());
                startActivity(intent);


            });
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.save);
        menuItem.setOnMenuItemClickListener(item -> {
            saveImage(stickerView.createBitmap());
            return false;
        });
        return super.onCreateOptionsMenu(menu);
    }
}