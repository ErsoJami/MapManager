package com.example.mapmanager;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class ProfileChangeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    ImageView profileImage;
    ImageView dateIcon;
    TextView currentDate;
    Calendar date = Calendar.getInstance();
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_change);
        profileImage = findViewById(R.id.ProfileIconChangingButton);
        dateIcon = findViewById(R.id.dateChangeIcon);
        currentDate = findViewById(R.id.dateText);
        Bitmap bt = BitmapFactory.decodeResource(getResources(), R.drawable.main_photo);
        RoundedBitmapDrawable btm = RoundedBitmapDrawableFactory.create(getResources(), bt);
        float radius = Math.min(bt.getWidth(), bt.getHeight()) / 2f;
        btm.setCornerRadius(radius);
        profileImage.setImageDrawable(btm);
        currentDate.setOnClickListener(v->{
            new DatePickerDialog(ProfileChangeActivity.this, d, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show();
        });
        dateIcon.setOnClickListener(v->{
            new DatePickerDialog(ProfileChangeActivity.this, d, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show();
        });
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
    }
    private void setInitialDateTime() {
        currentDate.setText(DateUtils.formatDateTime(this,
                date.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_DATE));
    }
    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            date.set(Calendar.YEAR, year);
            date.set(Calendar.MONTH, monthOfYear);
            date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
        }
    };
}
