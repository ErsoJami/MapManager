package com.example.mapmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.mapmanager.FirstFragment;
import com.example.mapmanager.R;
import com.example.mapmanager.SecondFragment;

public class MainActivity extends AppCompatActivity {

    ImageButton buttonFirstFragment, buttonSecondFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonFirstFragment = findViewById(R.id.imageButton6);
        buttonSecondFragment = findViewById(R.id.imageButton5);

        final FirstFragment firstFragment = new FirstFragment();
        final SecondFragment secondFragment = new SecondFragment();

        setCurrentFragment(firstFragment);

        buttonFirstFragment.setOnClickListener(v -> {
            setCurrentFragment(firstFragment);
        });

        buttonSecondFragment.setOnClickListener(v -> {
            setCurrentFragment(secondFragment);
        });
    }

    private void setCurrentFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}