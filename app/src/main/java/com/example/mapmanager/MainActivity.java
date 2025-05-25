package com.example.mapmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mapmanager.adapters.ChatAdapter;
import com.example.mapmanager.models.Route;
import com.example.mapmanager.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.transport.TransportFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MessengerFragment.OnChatSelectChat, ProfileFragment.ProfileChangeEnterListener, HomeFragment.HomeFragmentListener, ChatAdapter.OnSelectMedia {
    public static User user;
    private FirebaseAuth jAuth;
    private DatabaseReference databaseReference;
    private ImageButton homeButton, profileButton, chatButton, mapButton;
    private TextView homeText, profileText, chatText, mapText;
    private ConstraintLayout homeLayout, profileLayout, chatLayout, mapLayout;
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
            DirectionsFactory.initialize(this);
            TransportFactory.initialize(this);
        } catch (AssertionError e) {

        }

        user = new User();
        homeButton = findViewById(R.id.homeButton);
        profileButton = findViewById(R.id.profileButton);
        chatButton = findViewById(R.id.chatButton);
        mapButton = findViewById(R.id.mapButton);

        homeLayout = findViewById(R.id.homeLayout);
        profileLayout = findViewById(R.id.profileLayout);
        chatLayout = findViewById(R.id.chatLayout);
        mapLayout = findViewById(R.id.mapLayout);

        homeText = findViewById(R.id.homeText);
        profileText = findViewById(R.id.profileText);
        chatText = findViewById(R.id.chatText);
        mapText = findViewById(R.id.mapText);

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

        homeLayout.setOnClickListener(v -> {
            setCurrentFragment(homeFragment);
        });

        profileLayout.setOnClickListener(v -> {
            setCurrentFragment(profileFragment);
        });

        mapLayout.setOnClickListener(v -> {
            setCurrentFragment(mapFragment);
        });

        chatLayout.setOnClickListener(v -> {
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
                            if (mapFragment.isAdded()) mapFragment.dataLoad();
                            if (messengerFragment.isAdded()) messengerFragment.dataLoad();
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
        homeText.setTextColor(getResources().getColor(R.color.grey));
        profileDrawable.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
        profileText.setTextColor(getResources().getColor(R.color.grey));
        chatDrawable.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
        chatText.setTextColor(getResources().getColor(R.color.grey));
        mapDrawable.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
        mapText.setTextColor(getResources().getColor(R.color.grey));
    }
    private void setCurrentFragment(Fragment fragment) {
        resetButtonsColor();
        if (fragment == homeFragment) {
            homeDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            homeText.setTextColor(getResources().getColor(R.color.black));
        } else if (fragment == profileFragment) {
            profileDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            profileText.setTextColor(getResources().getColor(R.color.black));
        } else if (fragment == messengerFragment) {
            chatDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            chatText.setTextColor(getResources().getColor(R.color.black));
        } else if (fragment == mapFragment) {
            mapDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            mapText.setTextColor(getResources().getColor(R.color.black));
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onSelectChat(String id, String lastReadMessageId) {
        ChatFragment chatFragment = ChatFragment.updateChat(id, lastReadMessageId);
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

    @Override
    public void showInMap(Route route) {
        Bundle args = new Bundle();
        args.putSerializable("ROUTE_TO_SHOW", route);
        mapFragment.setArguments(args);
        setCurrentFragment(mapFragment);
//        mapFragment.routeOnMap(route);
    }

    @Override
    public void onSelectMedia(Object url) {
        PlayerViewFragment playerViewFragment = PlayerViewFragment.updatePlayerViewFragment(url);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, playerViewFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    @Override
    public void onSelectViewAllMedia(ArrayList<String> uriArrayList) {
        MediaViewFragment mediaViewFragment = MediaViewFragment.updateMediaViewFragment(uriArrayList);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mediaViewFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}