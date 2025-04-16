package com.example.mapmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.mapmanager.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yandex.mapkit.MapKitFactory;

public class MainActivity extends AppCompatActivity implements MessengerFragment.OnChatSelectChat, ProfileFragment.ProfileChangeEnterListener {
    public static User user = new User();
    private FirebaseAuth jAuth;
    private DatabaseReference databaseReference;
    private ImageButton homeButton, profileButton, chatButton, mapButton;
    private Drawable homeDrawable, profileDrawable, chatDrawable, mapDrawable;
    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;
    private MessengerFragment messengerFragment;
    private ProfileChangeFragment profileChangeFragment;
    private MapFragment mapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            MapKitFactory.setApiKey("83f579bc-1158-4bb1-895a-794de12cbf29");
            MapKitFactory.initialize(this);
        } catch (AssertionError e) {

        }

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
        messengerFragment = new MessengerFragment();
        mapFragment = new MapFragment();
        profileFragment = new ProfileFragment();
        profileChangeFragment = new ProfileChangeFragment();

        setCurrentFragment(homeFragment);

        jAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(jAuth.getCurrentUser().getUid());

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
            setCurrentFragment(messengerFragment);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            user.loadData(snapshot);
                            if (profileChangeFragment.isAdded()) profileChangeFragment.dataLoad();
                            if (profileFragment.isAdded()) profileFragment.dataLoad();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
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
        } else if (fragment == messengerFragment) {
            chatDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        } else if (fragment == mapFragment) {
            mapDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onSelectChat(String id) {
        ChatFragment chatFragment = ChatFragment.updateChat(id);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, chatFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
    @Override
    public void startChangingProfile() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, profileChangeFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}