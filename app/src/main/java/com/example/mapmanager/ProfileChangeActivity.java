package com.example.mapmanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

public class ProfileChangeActivity extends AppCompatActivity {
    ImageView profileImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_change);
        profileImage = findViewById(R.id.ProfileIconChangingButton);
        Bitmap bt = BitmapFactory.decodeResource(getResources(), R.drawable.main_photo);
        RoundedBitmapDrawable btm = RoundedBitmapDrawableFactory.create(getResources(), bt);
        float radius = Math.min(bt.getWidth(), bt.getHeight()) / 2f;  // Радиус для округления
        btm.setCornerRadius(radius);
        profileImage.setImageDrawable(btm);
    }
}
