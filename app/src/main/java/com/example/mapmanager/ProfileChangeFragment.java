package com.example.mapmanager;

import static android.app.Activity.RESULT_OK;
import static com.yandex.mapkit.search.SortOrigin.REQUEST;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.mapmanager.models.ChatsData;
import com.example.mapmanager.models.LoadMedia;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileChangeFragment extends Fragment {
    private static final int MIN_AGE = 16;
    private static final int MAX_NICKNAME_LENGTH = 20;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ImageView profileImage;
    private ImageView leaveButton;
    private ImageView dateIcon;
    private View saveData;
    private TextView currentDate;
    private EditText changeNicknameText;
    private EditText changeRealNameText;
    private EditText changePhoneText;
    private EditText changeEmailText;
    private EditText changeCountryText;
    private EditText changeCityText;
    private View view1, view2, view3, view4, view5, view6, view7;
    private ActivityResultLauncher<String[]> getMediaPermissions;
    private ActivityResultLauncher<String[]> mediaPicker;
    Calendar date = Calendar.getInstance();
    private View.OnClickListener saveDataListener;
    private Uri uri; //объявление локальных переменных
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPicker = registerForActivityResult(new ActivityResultContracts.OpenDocument(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri o) {
                uri = o;
                Glide.with(requireContext())
                        .load(uri)
                        .placeholder(R.drawable.user_icon)
                        .into(profileImage);
            }
        }); //загрузка аватара пользователя
        getMediaPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> o) {
                mediaPicker.launch(new String[]{"image/*"});
            }
        });
        saveDataListener = v -> {
            if (!isAgeValid(date)) {
                Toast.makeText(getContext(), getResources().getString(R.string.age_need_less) + " " + MIN_AGE + " " + getResources().getString(R.string.age), Toast.LENGTH_LONG).show();
                saveData.setOnClickListener(saveDataListener);
                return;
            }
            String nickname = changeNicknameText.getText().toString();
            if (nickname.length() > MAX_NICKNAME_LENGTH) {
                Toast.makeText(getContext(), getResources().getString(R.string.nick_need_less) + " " + MAX_NICKNAME_LENGTH + " " + getResources().getString(R.string.chars), Toast.LENGTH_LONG).show();
                saveData.setOnClickListener(saveDataListener);
                return;
            }
            if (nickname.trim().isEmpty()) {
                Toast.makeText(getContext(), getResources().getString(R.string.nick_not_be_empty), Toast.LENGTH_LONG).show();
                saveData.setOnClickListener(saveDataListener);
                return;
            }
            String phoneNumber = changePhoneText.getText().toString().replaceAll("[^0-9+]", "");
            if (!phoneNumber.isEmpty() && !isValidPhoneNumber(phoneNumber)) {
                Toast.makeText(getContext(), getResources().getString(R.string.enter_correct_number), Toast.LENGTH_LONG).show();
                saveData.setOnClickListener(saveDataListener);
                return;
            }

            saveData.setOnClickListener(null);
            if (MainActivity.user.getNick() != null && !changeNicknameText.getText().toString().equals(MainActivity.user.getNick()) && !MainActivity.user.getNick().isEmpty()) {
                HashMap<String, Object> data2 = new HashMap<>();
                DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference().child("chats");
                for (HashMap.Entry<String, ChatsData> item : MainActivity.user.getUserChatsData().entrySet()) {
                    String chatId = item.getKey();
                    ChatsData chatsData = item.getValue();
                    if (chatsData.getMessageList() == null) {
                        chatsData.setMessageList(new ArrayList<>());
                    }
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
            if (uri != null) {
                StorageReference fileReference = FirebaseStorage.getInstance().getReference().child("avatars").child(MainActivity.user.getUserId());
                UploadTask uploadTask = fileReference.putFile(uri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, com.google.android.gms.tasks.Task<Uri>>() {
                    @Override
                    public com.google.android.gms.tasks.Task<Uri> then(@NonNull com.google.android.gms.tasks.Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            // TODO доделать сообщение об исключении
                            throw task.getException();
                        }
                        return fileReference.getDownloadUrl();
                    }
                } ).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            if (downloadUri != null) {
                                uri = null;
                                MainActivity.user.setAvatarUrl(String.valueOf(downloadUri));
                                MainActivity.user.changeData(databaseReference);
                                saveData.setOnClickListener(saveDataListener);
                                Toast.makeText(getContext(), getResources().getString(R.string.successful_save), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), getResources().getString(R.string.error_file_load), Toast.LENGTH_SHORT).show();
                            MainActivity.user.changeData(databaseReference);
                            saveData.setOnClickListener(saveDataListener);
                        }
                    }
                });
            } else {
                MainActivity.user.changeData(databaseReference);
                saveData.setOnClickListener(saveDataListener);
                Toast.makeText(getContext(), getResources().getString(R.string.successful_save), Toast.LENGTH_SHORT).show();
            }
        }; // загрузка данных пользователя
    }

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
//      инициализация view
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        currentDate.setOnClickListener(v->{
            new DatePickerDialog(requireActivity(), d, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show();
        });
        dateIcon.setOnClickListener(v->{
            new DatePickerDialog(requireActivity(), d, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show();
        }); // установка листнеров на изменение даты в поле
        leaveButton.setOnClickListener(v->{
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }); //выход из активити
        dataLoad();
        saveData.setOnClickListener(saveDataListener); //сохранение данных пользователя
        profileImage.setOnClickListener(v -> { //
            openMediaPicker();
        });//загрузка изображения из хранилища пользователя
    }
    private void openMediaPicker() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_VIDEO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (permissionsToRequest.isEmpty()) {
            mediaPicker.launch(new String[]{"image/*"});
        } else {
            getMediaPermissions.launch(permissionsToRequest.toArray(new String[0]));
        }
    } //открытие медиа загрузки
    private boolean isAgeValid(Calendar birthDate) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age >= MIN_AGE;
    }//проверка валидности возраста
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true;
        }
        Pattern pattern = Pattern.compile("^\\+?[0-9\\s-()]{7,20}$");
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }//проверка валидности номера телефона
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
    }; //работа с выбором даты

    public void dataLoad() {
        if (MainActivity.user.getName() != null && uri == null)
            Glide.with(requireContext())
                    .load(MainActivity.user.getAvatarUrl())
                    .placeholder(R.drawable.user_icon)
                    .into(profileImage);
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
    }//выгрузка данных из класса юзера в поля изменения данных
}
