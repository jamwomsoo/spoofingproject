package com.example.videoex;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraDevice;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "TAG";
    private Uri videouri;
    private static final int REQUEST_CODE = 101;
    private StorageReference videoref;
    private String filename;
    private String _phone;
    private DatabaseReference mDatabase; // 네트워크 연결
    private String temp;
    public static final String CAMERA_FRONT = "1";
    public static final String CAMERA_BACK = "0";
    private String cameraId = CAMERA_BACK;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        Intent signUp_intent = getIntent();
        _phone = signUp_intent.getStringExtra("phone");
        //비디오 화면 띄워주기
        startVideo();
        //이름 네이밍
        create_Video_Name(storageRef);

    }

    private void create_Video_Name(StorageReference storageRef ) {
//        // 파일명 : 현재 시간 + 회원전화번호
//        SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
//        Date time = new Date();
//        String time1 = format1.format(time);
//        videoref =storageRef.child("/videos/" + time1 + _phone);

        // 파일명 : 회원전화번호
        videoref =storageRef.child("/Register/" + _phone);
    }

    public void checkSelfPermission() {
        temp = "";
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
        if (TextUtils.isEmpty(temp) == true) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // 테스트를 위해 5초로 설정 -> 테스트 끝나면 20초로 변경
            intent.putExtra("android.intent.extra.durationLimit", 5);
            try {
                startActivityForResult(intent, REQUEST_CODE); //startActivityForResult 새로운 액티비티 호출

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
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
        Log.e(TAG,"videoUri:"+videouri);
        filename = videouri.getLastPathSegment();
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
//                Toast.makeText (this, "Video saved to:\n" +
//                        videouri, Toast.LENGTH_LONG).show ();
                // upload 메소드. 저장 -> 업로드
                if (videouri != null) {

                    UploadTask uploadTask = videoref.putFile(videouri); // videoref 저장 경로에 비디오 저장

                    uploadTask.addOnFailureListener(new OnFailureListener () {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Upload failed
                            Toast.makeText(VideoActivity.this,
                                    "업로드에 실패했습니다.\n다시 시도해 주세요.: " + e.getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot> () {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    Toast.makeText(VideoActivity.this, "등록이 완료되었습니다.\n 승인을 기다려 주세요.",
                                            Toast.LENGTH_LONG).show();

                                    //데이터베이스에 저장된 동영상 url추가
                                    final String _url = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
//                                    Log.e(TAG, "url : "+_url);
                                    final DatabaseReference leadersRef = FirebaseDatabase.getInstance().getReference("UserList");
                                    final Query query = leadersRef.orderByChild("phone").equalTo(_phone);
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                for (DataSnapshot child: snapshot.getChildren()) {
                                                    //get the key of the child node that has to be updated
                                                    String postkey = child.getRef().getKey();
                                                    // 확인해보기
                                                    Log.d(TAG, "postkey: "+postkey);
                                                    //url update
                                                    String url = _url;
                                                    leadersRef.child(postkey).child("url").setValue(url);
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    startMypagectivity(_phone); // MyPage로 이동

                                }
                            }).addOnProgressListener (new OnProgressListener<UploadTask.TaskSnapshot> () {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            updateProgress(snapshot);
                        }
                    });        } else {
                    // Nothing to upload
                    Toast.makeText(VideoActivity.this, "업로드할 영상이 없습니다.\n다시 시도해 주세요.",
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

    //리스너에서 토스트가 안되가지고 함수로 만들어줌
    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void startMypagectivity(String phone) {
        //인텐트 객체 생성
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("phone",_phone); //휴대폰 번호 넘길 것 "매개변수명", 데이터
        startActivity(intent);
    }
}