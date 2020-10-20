package com.example.videoex.faceDetect.utils.interfaces;


import com.example.videoex.faceDetect.utils.models.RectModel;
public interface FaceDetectStatus {
    void onFaceLocated(RectModel rectModel);
    void onFaceNotLocated() ;
}
