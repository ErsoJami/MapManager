package com.example.mapmanager;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class ProfileChangeFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    ImageView profileImage;
    ImageView leaveButton;
    ImageView dateIcon;
    TextView currentDate;
    EditText changeNicknameText;
    EditText changeRealNameText;
    EditText changePhoneText;
    EditText changeEmailText;
    EditText changeCountryText;
    EditText changeCityText;
    Calendar date = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.e("Halo", "SUCCES");
        View view = inflater.inflate(R.layout.activity_profile_change, container, false);
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
            MainActivity.user.changeData(databaseReference);

        });
        dataLoad();
        changeNicknameText.addTextChangedListener(new AfterChangedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                MainActivity.user.setNick(changeNicknameText.getText().toString());
            }
        });
        changeRealNameText.addTextChangedListener(new AfterChangedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                MainActivity.user.setName(changeRealNameText.getText().toString());
            }
        });
        changePhoneText.addTextChangedListener(new AfterChangedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                MainActivity.user.setPhoneNumber(changePhoneText.getText().toString());
            }
        });
        changeEmailText.addTextChangedListener(new AfterChangedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                MainActivity.user.setEmail(changeEmailText.getText().toString());
            }
        });
        changeCountryText.addTextChangedListener(new AfterChangedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                MainActivity.user.setCountry(changeCountryText.getText().toString());
            }
        });
        changeCityText.addTextChangedListener(new AfterChangedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                MainActivity.user.setCity(changeCityText.getText().toString());
            }
        });
    }

    private void setInitialDateTime() {
        currentDate.setText(DateUtils.formatDateTime(requireContext(),
                date.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_DATE));
    }
    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            date.set(Calendar.YEAR, year);
            date.set(Calendar.MONTH, monthOfYear);
            date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            MainActivity.user.setBirthDayTime(date.getTimeInMillis());
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
    public abstract class AfterChangedTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
