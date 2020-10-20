package com.example.videoex.faceDetect.utils.interfaces;

import android.graphics.Bitmap;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import com.example.videoex.faceDetect.utils.common.FrameMetadata;
import com.example.videoex.faceDetect.utils.common.GraphicOverlay;
public interface FrameReturn{
    void onFrame(
            Bitmap image,
            FirebaseVisionFace face,
            FrameMetadata frameMetadata,
            GraphicOverlay graphicOverlay
    );
}