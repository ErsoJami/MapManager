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
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
    private ImageView passStatusView;
    private TextView passRecoveryTextView;
    private boolean showPass = false;
    //объявление

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        passStatusView = findViewById(R.id.passStatusView);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        registerTextView = findViewById(R.id.loginTextView);
        passRecoveryTextView = findViewById(R.id.passRecovery);
        //инициализация
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, R.string.fill_all_fields_text, Toast.LENGTH_SHORT).show();
                return;
            }
            if (mAuth.getCurrentUser() != null) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("CLEAR_DATA", true);
                startActivity(intent);
                finish();
            }
            loginUser(email, password);
        }); //вход пользователя с аккаунтом
        String registerText = getResources().getString(R.string.want_to_register_text);
        String passRecovery = getResources().getString(R.string.forgot_password_text);
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
        }; //переход на RegisterActivity
        ClickableSpan clickablePassRecovery = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String email = editTextEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, R.string.enter_email_text, Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, R.string.recovery_mail_send_text + email, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, R.string.mail_send_error_text + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
        }; //Восстановление пароля
        rt.setSpan(clickableRegText, getResources().getInteger(R.integer.register_left_border), getResources().getInteger(R.integer.register_right_border), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        pr.setSpan(clickablePassRecovery, getResources().getInteger(R.integer.password_left_border), getResources().getInteger(R.integer.password_right_border), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerTextView.setText(rt);
        registerTextView.setMovementMethod(LinkMovementMethod.getInstance());
        registerTextView.setHighlightColor(Color.TRANSPARENT);
        passRecoveryTextView.setText(pr);
        passRecoveryTextView.setMovementMethod(LinkMovementMethod.getInstance());
        passRecoveryTextView.setHighlightColor(Color.TRANSPARENT);
        passStatusView.setOnClickListener(v -> {
            if (!showPass) {
                passStatusView.setImageResource(R.drawable.hide_pass);
                showPass = true;
                editTextPassword.setTransformationMethod(null);
            } else {
                passStatusView.setImageResource(R.drawable.show_pass);
                showPass = false;
                editTextPassword.setTransformationMethod(new PasswordTransformationMethod());
            }
            editTextPassword.setSelection(editTextPassword.getText().length());
        });//показывать/скрывать пароль
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, R.string.enter_success_text, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("CLEAR_DATA", true);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(LoginActivity.this, R.string.enter_error_text + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO доделать сообщение об исключении
                    }
                });
    } //вход пользователя в аккаунт
}