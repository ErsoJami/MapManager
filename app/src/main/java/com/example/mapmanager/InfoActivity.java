package com.example.mapmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {

    private TextView yandexMapInstruction;
    private ImageView exitImageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        yandexMapInstruction = findViewById(R.id.textView13);
        exitImageView = findViewById(R.id.exitImageView);
        String htmlString = "YandexMapSdk: <a href=\"" + getResources().getString(R.string.yandex_api_url) + "\">" + getResources().getString(R.string.yandex_api_url) + "</a>";
        Spanned spannedText;
        spannedText = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        yandexMapInstruction.setText(spannedText);
        yandexMapInstruction.setMovementMethod(LinkMovementMethod.getInstance());
        exitImageView.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
    }
}
