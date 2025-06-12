package com.example.mapmanager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextView textViewTitle;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private TextView loginTextView;
    private ImageView passStatusView1, passStatusView2;
    private boolean showPass1 = false;
    private boolean showPass2 = false;
    //объявление

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        textViewTitle = findViewById(R.id.textViewTitle);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        loginTextView = findViewById(R.id.loginTextView);
        passStatusView1 = findViewById(R.id.passStatusView1);
        passStatusView2 = findViewById(R.id.passStatusView2);
        //инициализация
        buttonRegister.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String confirmPassword = editTextConfirmPassword.getText().toString();
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "@string/fill_all_fields_text", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(email, password, confirmPassword);
        });//регистрация нового пользователя
        String loginText = getResources().getString(R.string.want_to_login_text);
        SpannableString ss = new SpannableString(loginText);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra("CLEAR_DATA", true);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.BLUE);
            }
        };//переход на LoginActivity
        ss.setSpan(clickableSpan, getResources().getInteger(R.integer.login_left_border), getResources().getInteger(R.integer.login_right_border), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        loginTextView.setText(ss);
        loginTextView.setMovementMethod(LinkMovementMethod.getInstance());
        loginTextView.setHighlightColor(Color.TRANSPARENT);
        passStatusView1.setOnClickListener(v -> {
            if (!showPass1) {
                passStatusView1.setImageResource(R.drawable.hide_pass);
                showPass1 = true;
                editTextPassword.setTransformationMethod(null);
            } else {
                passStatusView1.setImageResource(R.drawable.show_pass);
                showPass1 = false;
                editTextPassword.setTransformationMethod(new PasswordTransformationMethod());
            }
            editTextPassword.setSelection(editTextPassword.getText().length());
        });//показать/скрывать пароль
        passStatusView2.setOnClickListener(v -> {
            if (!showPass2) {
                passStatusView2.setImageResource(R.drawable.hide_pass);
                showPass2 = true;
                editTextConfirmPassword.setTransformationMethod(null);
            } else {
                passStatusView2.setImageResource(R.drawable.show_pass);
                showPass2 = false;
                editTextConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());
            }
            editTextConfirmPassword.setSelection(editTextConfirmPassword.getText().length());
        });//показывать/скрывать подтверждение пароля
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            intent.putExtra("CLEAR_DATA", true);
            startActivity(intent);
            finish();
        }
    }
    private void registerUser(String email, String password, String confirmPassword) {
        if (password.equals(confirmPassword)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", email);
                                userData.put("name", "123");
                                userData.put("nick", "123");
                                userData.put("dateBirthday", "213");
                                usersRef.child(user.getUid()).setValue(userData)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(RegisterActivity.this, R.string.successful_registration, Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                    intent.putExtra("CLEAR_DATA", true);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.save_data_error) + " " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                            } else {
                                Toast.makeText(RegisterActivity.this, "@string/register_error_text" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // TODO доделать сообщение об исключении
                        }
                    });
        } else {
            Toast.makeText(RegisterActivity.this, "@string/different_passwords_text", Toast.LENGTH_SHORT).show();
        }
    }//регистрация юзера

}