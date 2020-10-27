package com.example.videoex;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase; // 네트워크 연결
    User user;
    // 체크박스 체크여부
    public int TERMS_AGREE_1 = 0; // No Check = 0, Check = 1
    public int TERMS_AGREE_2 = 0; // No Check = 0, Check = 1
    public int TERMS_AGREE_3 = 0; // No Check = 0, Check = 1
    // 체크박스
    AppCompatCheckBox check1; // 첫번쨰 동의
    AppCompatCheckBox check2; // 두번쨰 동의
    AppCompatCheckBox check3; // 전체 동의

    private Button terms1Btn;
    private Button terms2Btn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);

        //초기화
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.signUpButton).setOnClickListener(onClickListener);
        terms1Btn = findViewById(R.id.terms1);
        terms2Btn = findViewById(R.id.terms2);

        check1 = (AppCompatCheckBox)findViewById(R.id.check1);
        check2 = (AppCompatCheckBox)findViewById(R.id.check2);
        check3 = (AppCompatCheckBox)findViewById(R.id.check3);

        //이용약관1
        terms1Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), TermsActivity1.class);
                startActivity(intent);
            }
        });
        //이용약관2
        terms2Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), TermsActivity2.class);
                startActivity(intent);
            }
        });
        //이용약관 체크
        checkOne();
        checktwo();
        allCheck();

    }



    private void checkOne() {
        check1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (check1.isChecked() == true) {
                    TERMS_AGREE_1 = 1;

                } else {
                    TERMS_AGREE_1 = 0;
                }
                if(check1.isChecked() && check2.isChecked()){
                    check3.setChecked(true);
                    TERMS_AGREE_3 = 1;
                }
                else {
                    check3.setChecked(false);
                    TERMS_AGREE_3 = 0;
                }
            }
        });
    }
    private void checktwo() {
        // 2항동의
        check2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (check2.isChecked() == true) {
                    TERMS_AGREE_2 = 1;

                } else {
                    TERMS_AGREE_2 = 0;
                }
                if(check1.isChecked() && check2.isChecked()){
                    check3.setChecked(true);
                    TERMS_AGREE_3 = 1;
                }
                else {
                    check3.setChecked(false);
                    TERMS_AGREE_3 = 0;
                }
            }
        });
    }
    private void allCheck() {
        check3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (check3.isChecked() == true) {
                    check1.setChecked(true);
                    check2.setChecked(true);
                    TERMS_AGREE_3 = 1;
                } else {
                    check1.setChecked(false);
                    check2.setChecked(false);
                    TERMS_AGREE_3 = 0;
                }
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.signUpButton:
//                    if((TERMS_AGREE_1 == 1 && TERMS_AGREE_2 == 1) || TERMS_AGREE_3 == 1){
//                        signUp();
//                    }else{
//
//                    }
                    signUp();
                    break;

            }
        }
    };

    private void signUp() {
        final String name = ((EditText) findViewById(R.id.nameEditText)).getText().toString();
        final String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
        String phone = ((EditText) findViewById(R.id.phoneEditText)).getText().toString();

        //User 클래스를 이용하여 빈 객체 만든다
        final User user = new User();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("UserList"); //userList 라는 키를 가진 값들을 참조한다.
        if (TextUtils.isEmpty(name)) {
            startToast("이름을 입력해주세요");
            return;
        } else if (TextUtils.isEmpty(email)) {
            startToast("e-mail을 입력해주세요");
            return;
        } else if (TextUtils.isEmpty(phone)) {
            startToast("휴대폰 번호를 입력해주세요");
            return;
        }

        // 휴대폰 번호 정규식 검사 & 중복 검사
        String regExp = "^01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$";
        if(phone.matches(regExp) == false){
            startToast("핸드폰 번호를 확인해주세요.");
        }else if(phone.matches(regExp) == true){
            if (phone.startsWith("0")) {
                phone = phone.substring(1);
            }
            phone = "+82" + phone;
            Query query = FirebaseDatabase.getInstance().getReference().child("UserList").orderByChild("phone").equalTo(phone);
            final String finalPhone = phone;
            query.addListenerForSingleValueEvent(new ValueEventListener(){
                @Override
                public void onDataChange(DataSnapshot datasnapshot){
                    boolean phoneIsExist = datasnapshot.exists();
                    System.out.print(phoneIsExist);
                    if(phoneIsExist){
                        Toast.makeText(getApplicationContext(), "이미 가입된 휴대폰 번호입니다\n다시 확인해 주세요.",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if (name.length() > 0 && email.length() > 0 && finalPhone.length() > 0) {
                            SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
                            Date time = new Date();
                            String time1 = format1.format(time);
                            user.setName(name);
                            user.setEmail(email);
                            user.setPhone(finalPhone);
                            user.setApproval("F"); // 회원에게 있어서는 승인여부 관리자 입장에서는 학습여부 디폴트 값 F -> 승인 시 T 로 변경
                            user.setRegisterDate(time1);
                            user.setState("Register");
                            // 전체 약관 체크여부
                            if (TERMS_AGREE_3 != 1) {
                                // 첫번째 약관 체크여부
                                if (TERMS_AGREE_2 == 1) {
                                    // 두번쨰 약관 체크 여부
                                    if (TERMS_AGREE_1 == 1) {
                                    } else {
                                        Toast.makeText(getApplicationContext(), "이용약관을 체크해주세요", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "개인정보취약관을 체크해주세요", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }else{
                                mDatabase.child(finalPhone).setValue(user);  // 유저 휴대시폰 번호으로 UserList 하위 경로 생성 정보 저장
                                startVideoActivity(finalPhone); // NoticeActivity로 이동 (비디오 촬영)
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }



    }

    //리스너에서 토스트가 안되가지고 함수로 만들어줌
    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void startVideoActivity(String phone) {
        //인텐트 객체 생성
        Intent intent = new Intent(this, NoticeActivity.class);
        intent.putExtra("phone",phone); //휴대폰 번호 넘길 것 "매개변수명", 데이터
        startActivity(intent);
        finish();
    }




}