package com.example.distributedsystemsapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;

import androidx.annotation.Nullable;

import java.io.File;

public class ImageActivity extends Activity {

    ImageView image;
    Button backBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_dialog);

        Intent intent = getIntent();
        String filename = intent.getStringExtra("filename");

        image = findViewById(R.id.picture);

        File path = getApplicationContext().getFilesDir();
        File readFrom = new File(path, filename);

        Bitmap bitmap = BitmapFactory.decodeFile(readFrom.getAbsolutePath());

        image.setImageBitmap(bitmap);

        backBtn = findViewById(R.id.imageBackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
