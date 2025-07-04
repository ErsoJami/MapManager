package com.example.mapmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    public static interface ProfileChangeEnterListener {
        void startChangingProfile();
    } //связь между profileFragment и profileChangeFragment
    private ProfileChangeEnterListener profileChangeEnterListener;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private TextView userTextView, emailTextView, dateTextView, quitAccountText, deleteAccountText, changePassText, changePersonalDataText;
    private TextView infoTextView;
    private View view1, view2, view3, view6, view7, view8, view9;
    private ImageView profileImage;
    private ImageView deleteAccountView;
    private FirebaseAuth.AuthStateListener authStateListener;//объявление
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        profileChangeEnterListener = (ProfileChangeEnterListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    Toast.makeText(requireActivity(), R.string.enter_success_text, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.putExtra("CLEAR_DATA", true);
                    startActivity(intent);
                    requireActivity().finish();
                }
            }
        };// проверка наличия акканута пользователя(для удаления и выхода из аккаунта)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        userTextView = view.findViewById(R.id.userTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        profileImage = view.findViewById(R.id.ProfileIconChangingButton);
        dateTextView = view.findViewById(R.id.dateTextView);
        deleteAccountView = view.findViewById(R.id.deleteAccountView);
        quitAccountText = view.findViewById(R.id.quitAccountText);
        deleteAccountText = view.findViewById(R.id.deleteAccountText);
        changePassText = view.findViewById(R.id.changePassText);
        changePersonalDataText = view.findViewById(R.id.changePersonalDataText);
        infoTextView = view.findViewById(R.id.infoTextView);
        view1 = view.findViewById(R.id.view);
        view2 = view.findViewById(R.id.view2);
        view3 = view.findViewById(R.id.view3);
        view6 = view.findViewById(R.id.view6);
        view7 = view.findViewById(R.id.view7);
        view8 = view.findViewById(R.id.view8);
        view9 = view.findViewById(R.id.view9);
        dataLoad();
        view1.post(new Runnable() {
            @Override
            public void run() {
                view2.bringToFront();
                view3.bringToFront();
            }
        });
        view6.post(new Runnable() {
            @Override
            public void run() {
                view8.bringToFront();
            }
        });
        view7.post(new Runnable() {
            @Override
            public void run() {
                view9.bringToFront();
                deleteAccountView.bringToFront();
            }
        }); // инициализация view
        return view;

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = mAuth.getCurrentUser();
        quitAccountText.setOnClickListener(v -> {
            signOut();
        });//выход из аккаунта
        deleteAccountText.setOnClickListener(v -> {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.account_delete_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogView);
//            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            View deleteView = dialogView.findViewById(R.id.accountDeleteView);
            View cancelView = dialogView.findViewById(R.id.cancelView);
            deleteView.setOnClickListener(v1 -> {
                deleteAccount();
                dialog.dismiss();
            });

            cancelView.setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            dialog.show();

        }); // удаление аккаунта
        changePassText.setOnClickListener(v ->{ //изменение пароля пользователя
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.change_pass_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogView);
//            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            View changeView = dialogView.findViewById(R.id.applyChangePassView);
            View cancelView = dialogView.findViewById(R.id.cancelView);
            EditText editPasswordText, confirmPasswordText;
            editPasswordText = dialogView.findViewById(R.id.editPasswordText);
            confirmPasswordText = dialogView.findViewById(R.id.confirmPasswordText);

            changeView.setOnClickListener(v1 -> {
                String password = editPasswordText.getText().toString();
                String confirmPassword = confirmPasswordText.getText().toString();
                if (password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireActivity(), R.string.fill_all_fields_text, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.equals(confirmPassword)) {
                    changeUserPass(password);
                } else {
                    Toast.makeText(requireActivity(), R.string.different_passwords_text, Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
            });

            cancelView.setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            dialog.show();
        });
        changePersonalDataText.setOnClickListener(v->{
            profileChangeEnterListener.startChangingProfile();
        }); //переход в изменение профиля
        infoTextView.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), InfoActivity.class);
            startActivity(intent);
        }); //меню информации о приложении
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    void changeUserPass(String newPass) {
        FirebaseUser user = mAuth.getCurrentUser();
        user.updatePassword(newPass)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(requireContext(), R.string.pass_change, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), R.string.pass_not_change, Toast.LENGTH_SHORT).show();
                    }
                });
    }// изменение пароля пользователя
    void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference userData = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                            userData.removeValue();
                            Toast.makeText(requireActivity(), R.string.enter_success_text, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(requireActivity(), LoginActivity.class);
                            intent.putExtra("CLEAR_DATA", true);
                            startActivity(intent);
                            requireActivity().finish();
                        } else {
                            Toast.makeText(requireContext(), R.string.error_account_delete, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), R.string.error_account_delete + e.getLocalizedMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    } //удаление акаунта пользователя
    void signOut() {
        mAuth.signOut();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public void dataLoad() {
        if (MainActivity.user.getName() != null)
            Glide.with(requireContext())
                    .load(MainActivity.user.getAvatarUrl())
                    .placeholder(R.drawable.user_icon)
                    .into(profileImage);
        if (MainActivity.user.getName() != null)
            userTextView.setText(MainActivity.user.getName());
        if (MainActivity.user.getEmail() != null)
            emailTextView.setText(MainActivity.user.getEmail());
        if (MainActivity.user.getBirthDayTime() != 0)
            dateTextView.setText(DateUtils.formatDateTime( requireActivity(), MainActivity.user.getBirthDayTime(),
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE));
    }//выгрузка данных из класса юзера в поля изменения данных
}