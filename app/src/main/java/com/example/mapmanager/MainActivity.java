package com.example.mapmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private ImageButton homeButton, profileButton, chatButton, mapButton;
    private Drawable homeDrawable, profileDrawable, chatDrawable, mapDrawable;
    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;
    private ChatFragment chatFragment;
    private MapFragment mapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeButton = findViewById(R.id.homeButton);
        profileButton = findViewById(R.id.profileButton);
        chatButton = findViewById(R.id.chatButton);
        mapButton = findViewById(R.id.mapButton);

        homeDrawable = homeButton.getDrawable();
        profileDrawable = profileButton.getDrawable();
        chatDrawable = chatButton.getDrawable();
        mapDrawable = mapButton.getDrawable();

        homeFragment = new HomeFragment();
        profileFragment = new ProfileFragment();
        chatFragment = new ChatFragment();
        mapFragment = new MapFragment();
        setCurrentFragment(homeFragment);

        homeButton.setOnClickListener(v -> {
            setCurrentFragment(homeFragment);
        });

        profileButton.setOnClickListener(v -> {
            setCurrentFragment(profileFragment);
        });

        mapButton.setOnClickListener(v -> {
            setCurrentFragment(mapFragment);
        });

        chatButton.setOnClickListener(v -> {
            setCurrentFragment(chatFragment);
        });
    }

    private void resetButtonsColor() {
        homeDrawable.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
        profileDrawable.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
        chatDrawable.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
        mapDrawable.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
    }
    private void setCurrentFragment(Fragment fragment) {
        resetButtonsColor();
        if (fragment == homeFragment) {
            homeDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        } else if (fragment == profileFragment) {
            profileDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        } else if (fragment == chatFragment) {
            chatDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        } else if (fragment == mapFragment) {
            mapDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}