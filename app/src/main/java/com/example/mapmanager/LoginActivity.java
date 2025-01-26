package com.example.mapmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView registerTextView;

    private TextView passRecoveryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        registerTextView = findViewById(R.id.loginTextView);
        passRecoveryTextView = findViewById(R.id.passRecovery);
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Пожалуйста, заполните все поля.", Toast.LENGTH_SHORT).show();
                return;
            }
            loginUser(email, password);
        });
        String registerText = "Don't have an account? Register";
        String passRecovery = "Forgot password?";
        SpannableString rt = new SpannableString(registerText);
        SpannableString pr = new SpannableString(passRecovery);
        ClickableSpan clickableRegText = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
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
        };
        ClickableSpan clickablePassRecovery = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String email = editTextEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Пожалуйста, введите ваш email.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Письмо для восстановления пароля отправлено на " + email, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Ошибка отправки письма: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.BLUE);
            }
        };
        rt.setSpan(clickableRegText, 23, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        pr.setSpan(clickablePassRecovery, 0, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerTextView.setText(rt);
        registerTextView.setMovementMethod(LinkMovementMethod.getInstance());
        registerTextView.setHighlightColor(Color.TRANSPARENT);
        passRecoveryTextView.setText(pr);
        passRecoveryTextView.setMovementMethod(LinkMovementMethod.getInstance());
        passRecoveryTextView.setHighlightColor(Color.TRANSPARENT);
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Вход выполнен успешно.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("CLEAR_DATA", true);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка входа: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
}