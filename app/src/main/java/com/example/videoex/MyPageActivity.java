package com.example.videoex;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MyPageActivity extends AppCompatActivity {
    private String _phone;
    private DatabaseReference mDatabase; // 네트워크 연결
    private String myName;
    private String myPhoneNum;
    private String myapproval;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        Intent signUp_intent = getIntent();
        _phone = signUp_intent.getStringExtra("phone");

        final DatabaseReference leadersRef = FirebaseDatabase.getInstance().getReference("UserList");

        final Query query = leadersRef.orderByChild("phone").equalTo(_phone);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    User myUser = dataSnapshot1.getValue(User.class);
                    Log.d("유저이름", myUser.getName());
                    Log.d("유저 폰 번호", myUser.getPhone());
                    Log.d("유저 승인 상태", myUser.getApproval());

                    myName = myUser.getName();
                    myPhoneNum = myUser.getPhone();
                    myapproval = myUser.getApproval();

                    if (myapproval.equals("F")){
                        myapproval = "승인 전";
                    } else {
                        myapproval = "승인 완료";
                    }

                    set_view();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void set_view() {
        ((TextView) findViewById(R.id.mypage_name)).setText(myName);
        ((TextView) findViewById(R.id.mypage_phone)).setText(myPhoneNum);
        ((TextView) findViewById(R.id.mypage_approval)).setText(myapproval);
    }
}
