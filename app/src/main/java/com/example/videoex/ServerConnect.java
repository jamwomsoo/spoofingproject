package com.example.videoex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.videoex.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ServerConnect extends AppCompatActivity  {
    //private static final String DEFAULT_PATTERN = "%d%%";

    //ProgressBar circleProgressBar;

    private static final String TAG = "TAG";
    private ProgressDialog dialog;
    private String phone;
    private int TIMEOUT_VALUE = 1000000;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connect);


        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");
        connect_server(phone);

    }

    public void connect_server(String phone){

        String phone_data = "";
        phone_data = "{\"phone\":\""+ phone + "\"}";
//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("phone",phone);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        NetworkTask networkTask = new NetworkTask(phone_data);
        networkTask.execute();


    }


    //--------------------------------
    /* 게시글 정보를 서버에 보내는 Class*/
    //--------------------------------
    public class NetworkTask extends AsyncTask<Void, Void, String> {
        String values;

        NetworkTask(String values) {
            this.values = values;
        }//생성자

        final ProgressBar progressBar = findViewById(R.id.progressBar);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);

        }//실행 이전 작업 정의 함수

        @Override
        protected String doInBackground(Void... params) {
            String result = "";

            try {
                //서버에 정보를 입력하는 함수 호출
                result = sendPhoneNum(values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result; // 결과가 여기에 담깁니다. 아래 onPostExecute()의 파라미터로 전달됩니다.
        } // 백그라운드 작업 함수

        @Override
        protected void onPostExecute(String result) {
            // 통신이 완료되면 호출됩니다.
            // 결과에 따른 UI 수정 등은 여기서 합니다.
            try {
                progressBar.setVisibility(View.GONE);
                set_result(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public String sendPhoneNum(String values) throws JSONException {
        String result = "";
        try {
            //--------------------------
            //   URL 설정하고 접속하기
            //--------------------------

            String str_URL = "http://" + RequestHttpURLConnection.server_ip + ":" + RequestHttpURLConnection.server_port + "/mobile";

            URL url = new URL(str_URL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();   // 접속
            Log.e(TAG,"URL Connection");
            //--------------------------
            //   전송 모드 설정 - 기본적인 설정이다
            //--------------------------
            http.setDefaultUseCaches(false);
            http.setDoInput(true);                         // 서버에서 읽기 모드 지정
            http.setDoOutput(true);                         // 서버로 쓰기 모드 지정
            http.setConnectTimeout(TIMEOUT_VALUE);//연결 딜레이 기다림
            http.setReadTimeout(TIMEOUT_VALUE);//

            http.setRequestMethod("POST");         // 전송 방식은 POST

            // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");            //--------------------------
            //   서버로 값 전송
            //--------------------------
            StringBuffer buffer = new StringBuffer();
            String data = "phone=" + values;
            buffer.append(data);                 // php 변수에 값 대입
            Log.e(TAG,"data:"+data);
            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(buffer.toString());
            writer.flush();

            //--------------------------
            //   서버에서 전송받기
            //--------------------------
            InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(tmp);
            StringBuilder builder = new StringBuilder();
            http.disconnect();
            String str;
            while ((str = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                builder.append(str);                     // View에 표시하기 위해 라인 구분자 추가
            }
            result = builder.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    } // HttpPostDat

    private void set_result(String data) throws JSONException {
        String str_res = data;
        System.out.println("str_res:"+str_res+"shit");
//        JSONObject jsonObject =  new JSONObject(str_res);
//        String authenResult = jsonObject.getString("result");

        if(str_res.equals("accept")) {
            System.out.print("accept page success");
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("UserList").child(phone).child("approval").setValue("T");
            Intent intent = new Intent(this, MyPageActivity.class);
            intent.putExtra("phone",phone); //휴대폰 번호 넘길 것 "매개변수명", 데이터
            startActivity(intent);
            finish();

        }
        else {

            System.out.print("accept page denied");
            Intent intent = new Intent(ServerConnect.this,MainActivity.class);
            startActivity(intent);
            finish();

        }
        finish();
    }

}