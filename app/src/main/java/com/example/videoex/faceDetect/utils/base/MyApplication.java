package com.example.videoex.faceDetect.utils.base;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
