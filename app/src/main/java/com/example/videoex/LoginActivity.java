package com.example.videoex;
/*
OTP -> 로그인하기 위한 얼굴 영상 촬영
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "TAG";
    private Uri videouri;
    private static final int REQUEST_CODE = 101;
    private StorageReference videoref;
    private String filename;
    private String _phone;
    private DatabaseReference mDatabase; // 네트워크 연결




    static final int REQUEST_VIDEO_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent intent = getIntent();
        _phone = intent.getStringExtra("phone");
        Log.e("TAG","Phone : "+_phone);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        //비디오 화면 띄워주기
        startVideo();
        //dispatchTakeVideoIntent();
        //이름 네이밍
        create_Video_Name(storageRef);
    }

    private void create_Video_Name(StorageReference storageRef ) {
        // 파일명 : 회원전화번호
        videoref =storageRef.child("/Login/" + _phone);
    }

    public void checkSelfPermission() {
        String temp = "";
        //파일 읽기 권한 확인
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.CAMERA+ " ";
        }


        if (TextUtils.isEmpty(temp) == false) {
            //권한 요청
            ActivityCompat.requestPermissions(this, temp.trim().split(" "),1); }
        else {
            //모두 허용 상태
            Toast.makeText(this, "권한을 모두 허용", Toast.LENGTH_SHORT).show(); }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //권한을 허용 했을 경우
        if(requestCode == 1){
            int length = permissions.length;
            for (int i = 0; i < length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // 동의
                    Log.d("MainActivity","권한 허용 : " + permissions[i]);
                }
            }
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // 테스트를 위해 5초로 설정 -> 테스트 끝나면 20초로 변경
            intent.putExtra("android.intent.extra.durationLimit",5);
            try {
                startActivityForResult(intent, REQUEST_CODE); //startActivityForResult 새로운 액티비티 호출

            }catch (Exception e){
                Log.e(TAG,e.getMessage());
            }
        }
    }

    private void startVideo() {
        checkSelfPermission();
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // 테스트를 위해 5초로 설정 -> 테스트 끝나면 20초로 변경
        intent.putExtra("android.intent.extra.durationLimit",3);
        try {
            startActivityForResult(intent, REQUEST_CODE); //startActivityForResult 새로운 액티비티 호출

        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
    }





    public void updateProgress(UploadTask.TaskSnapshot taskSnapshot) {

        @SuppressWarnings("VisibleForTests") long fileSize =
                taskSnapshot.getTotalByteCount();

        @SuppressWarnings("VisibleForTests")
        long uploadBytes = taskSnapshot.getBytesTransferred();

        long progress = (100 * uploadBytes) / fileSize;

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.pbar);
        progressBar.setProgress((int) progress);
    }


    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult (requestCode, resultCode, data);

        videouri = data.getData ();
        filename = videouri.getLastPathSegment();
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (videouri != null) {

                    UploadTask uploadTask = videoref.putFile(videouri); // videoref 저장 경로에 비디오 저장

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Upload failed
                            Toast.makeText(LoginActivity.this,
                                    "업로드에 실패했습니다.\n다시 시도해 주세요.: " + e.getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    mDatabase = FirebaseDatabase.getInstance().getReference();
                                    mDatabase.child("UserList").child(_phone).child("state").setValue("Login");
                                    Toast.makeText(LoginActivity.this, "촬영이 완료되었습니다.",
                                            Toast.LENGTH_LONG).show();


                                    //데이터베이스에 저장된 동영상 url추가
                                    final String _url = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                                    final DatabaseReference leadersRef = FirebaseDatabase.getInstance().getReference("UserList");
                                    final Query query = leadersRef.orderByChild("phone").equalTo(_phone);
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                for (DataSnapshot child: snapshot.getChildren()) {
                                                    //get the key of the child node that has to be updated
                                                    String postkey = child.getRef().getKey();
                                                    //url update
                                                    String url = _url;
                                                    leadersRef.child(postkey).child("loginUrl").setValue(url);
                                                }
                                            }
                                        }
                                        //이런...
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    startMainpagectivity(_phone); // Main로 이동
                                }
                            }).addOnProgressListener (new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            updateProgress(snapshot);
                        }
                    });        } else {
                    // Nothing to upload
                    Toast.makeText(LoginActivity.this, "업로드할 영상이 없습니다.\n다시 시도해 주세요.",
                            Toast.LENGTH_LONG).show();
                }

            } else if (resultCode == RESULT_CANCELED) {
                // Video recording cancelled.
                Toast.makeText (this, "촬영이 취소되었습니다.",
                        Toast.LENGTH_LONG).show ();
            } else {
                // Failed to record video
                Toast.makeText (this, "촬영에 실패했습니다.\n다시 시도해 주세요.",
                        Toast.LENGTH_LONG).show ();
            }
        }
    }

    private void startMainpagectivity(String phone) {
        //인텐트 객체 생성
        Intent intent = new Intent(this, MyPageActivity.class);
        intent.putExtra("phone",phone); //휴대폰 번호 넘길 것 "매개변수명", 데이터
        startActivity(intent);
    }
}