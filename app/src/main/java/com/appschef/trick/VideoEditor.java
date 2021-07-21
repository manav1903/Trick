package com.appschef.trick;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

public class VideoEditor extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);
        VideoView videoView = findViewById(R.id.video);
        String path=getIntent().getStringExtra("file");
        videoView.setVideoURI(Uri.parse(path));
        MediaController mediaController = new MediaController(this);
        videoView.start();
        videoView.setMediaController(mediaController);
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