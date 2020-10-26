package com.example.videoex;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class NoticeActivity extends AppCompatActivity {
    Button VideoRecordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        Intent signup_intent = getIntent();
        String _phone = signup_intent.getStringExtra("phone");

        VideoRecordButton = findViewById(R.id.VideoRecordButton);

        VideoRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVideoActivity(_phone);
            }
        });
    }

    private void startVideoActivity(String _phone) {
        //인텐트 객체 생성
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra("phone", _phone); //휴대폰 번호 넘길 것 "매개변수명", 데이터
        startActivity(intent);
    }
}

