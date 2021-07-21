package com.appschef.trick;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class ImageEditor extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);

        String path = getIntent().getStringExtra("file");
        ImageView imageView = findViewById(R.id.image);
        imageView.setImageURI(Uri.parse(path));
        findViewById(R.id.edit).setOnClickListener(v -> {
                    Intent intent = new Intent(this, Editor.class);
                    intent.putExtra("file", path);
                    startActivity(intent);
                }
        );
findViewById(R.id.share).setOnClickListener(v -> {
    Intent intent = new Intent("android.intent.action.SEND");
    Uri parse = Uri.parse(path);
    intent.setType("*/*");
    intent.putExtra("android.intent.extra.STREAM", parse);
    startActivity(Intent.createChooser(intent, "Share using"));
});

findViewById(R.id.delete).setOnClickListener(v -> {
    assert path != null;
    File file=new File(path);
    if (file.delete()) {
        Toast.makeText(this, "File Deleted", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, Gallery.class));
    } else {
        Toast.makeText(this, "File not Deleted", Toast.LENGTH_SHORT).show();
    }

});
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, Gallery.class));
    }
}