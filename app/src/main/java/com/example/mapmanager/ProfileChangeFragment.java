package com.example.mapmanager;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;

import com.example.mapmanager.models.ChatsData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

public class ProfileChangeFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    ImageView profileImage;
    ImageView leaveButton;
    ImageView dateIcon;
    View saveData;
    TextView currentDate;
    EditText changeNicknameText;
    EditText changeRealNameText;
    EditText changePhoneText;
    EditText changeEmailText;
    EditText changeCountryText;
    EditText changeCityText;
    private View view1, view2, view3, view4, view5, view6, view7;
    Calendar date = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_change, container, false);
        profileImage = view.findViewById(R.id.ProfileIconChangingButton);
        dateIcon = view.findViewById(R.id.dateChangeIcon);
        currentDate = view.findViewById(R.id.dateText);
        leaveButton = view.findViewById(R.id.leaveChangingProfileButton);
        changeNicknameText = view.findViewById(R.id.nickInput);
        changeRealNameText = view.findViewById(R.id.realNameInput);
        changePhoneText = view.findViewById(R.id.phoneInput);
        changeEmailText = view.findViewById(R.id.emailInput);
        changeCountryText = view.findViewById(R.id.countryInput);
        changeCityText = view.findViewById(R.id.cityInput);
        saveData = view.findViewById(R.id.saveData);
        view1 = view.findViewById(R.id.divider1);
        view2 = view.findViewById(R.id.divider2);
        view3 = view.findViewById(R.id.divider3);
        view4 = view.findViewById(R.id.divider4);
        view5 = view.findViewById(R.id.divider5);
        view6 = view.findViewById(R.id.divider6);
        view7 = view.findViewById(R.id.changeView);
//        view7.post(new Runnable() {
//            @Override
//            public void run() {
//                view1.bringToFront();
//                view2.bringToFront();
//                view3.bringToFront();
//                view4.bringToFront();
//                view5.bringToFront();
//                view6.bringToFront();
//            }
//        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CropImage();
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        currentDate.setOnClickListener(v->{
            new DatePickerDialog(requireActivity(), d, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show();
        });
        dateIcon.setOnClickListener(v->{
            new DatePickerDialog(requireActivity(), d, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show();
        });
        leaveButton.setOnClickListener(v->{
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        dataLoad();
        saveData.setOnClickListener(v -> {
            if (MainActivity.user.getNick() != null && !changeNicknameText.getText().toString().equals(MainActivity.user.getNick()) && !MainActivity.user.getNick().isEmpty()) {
                HashMap<String, Object> data2 = new HashMap<>();
                DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference().child("chats");
                for (HashMap.Entry<String, ChatsData> item : MainActivity.user.getUserChatsData().entrySet()) {
                    String chatId = item.getKey();
                    ChatsData chatsData = item.getValue();
                    for (String messageId : chatsData.getMessageList()) {
                        data2.put("/" + chatId + "/messages/" + messageId + "/nick", changeNicknameText.getText().toString());
                    }
                }
                chatReference.updateChildren(data2);
            }
            MainActivity.user.setNick(changeNicknameText.getText().toString());
            MainActivity.user.setName(changeRealNameText.getText().toString());
            MainActivity.user.setPhoneNumber(changePhoneText.getText().toString());
            MainActivity.user.setEmail(changeEmailText.getText().toString());
            MainActivity.user.setCountry(changeCountryText.getText().toString());
            MainActivity.user.setCity(changeCityText.getText().toString());
            MainActivity.user.setBirthDayTime(date.getTimeInMillis());
            MainActivity.user.changeData(databaseReference);
        });
    }

    private void setInitialDateTime() {
        currentDate.setText(DateUtils.formatDateTime(requireContext(),
                date.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }
    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            date.set(Calendar.YEAR, year);
            date.set(Calendar.MONTH, monthOfYear);
            date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
        }
    };

    void CropImage() {
        Bitmap bt = BitmapFactory.decodeResource(getResources(), R.drawable.main_photo);
        RoundedBitmapDrawable btm = RoundedBitmapDrawableFactory.create(getResources(), bt);
        float radius = Math.min(bt.getWidth(), bt.getHeight()) / 2f;
        btm.setCornerRadius(radius);
        profileImage.setImageDrawable(btm);
    }

    public void dataLoad() {
        if (MainActivity.user.getName() != null)
            changeRealNameText.setText(MainActivity.user.getName());
        if (MainActivity.user.getNick() != null)
            changeNicknameText.setText(MainActivity.user.getNick());
        if (MainActivity.user.getPhoneNumber() != null)
            changePhoneText.setText(MainActivity.user.getPhoneNumber());
        if (MainActivity.user.getEmail() != null)
            changeEmailText.setText(MainActivity.user.getEmail());
        if (MainActivity.user.getCountry() != null)
            changeCountryText.setText(MainActivity.user.getCountry());
        if (MainActivity.user.getCity() != null)
            changeCityText.setText(MainActivity.user.getCity());
        if (MainActivity.user.getBirthDayTime() != 0)
            currentDate.setText(DateUtils.formatDateTime(requireContext(), MainActivity.user.getBirthDayTime(),
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE));
    }
}
