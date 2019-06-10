package com.kdh.ftpfilemodified;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity
{
    private EditText edFtpUrl; //FTP server url
    private EditText edFtpId; //FTP server id
    private EditText edFtpPw; //FTP server pw
    private EditText edModi; //수정될 값
    private Button btnGet; //현재 FTP server 파일 내용을 가져오는 버튼
    private Button btnPush; //FTP server 파일을 수정하여 보내는 버튼

    private String fileContent = null; //FTP server 파일 내용을 저장할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init(); //UI setting

        btnGet.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                GetResultAsyncTask getTask = new GetResultAsyncTask();
                getTask.execute();
            }
        });

        btnPush.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PushResultAsyncTask pushTask = new PushResultAsyncTask();
                pushTask.execute();
            }
        });
    }

    public void init() //UI setting
    {
        edFtpUrl = findViewById(R.id.ed_ftp_url);
        edFtpId = findViewById(R.id.ed_ftp_id);
        edFtpPw = findViewById(R.id.ed_ftp_pw);
        edModi = findViewById(R.id.ed_modi);
        btnGet = findViewById(R.id.btn_get);
        btnPush = findViewById(R.id.btn_push);
    }

    private class GetResultAsyncTask extends AsyncTask<Void, Void, String>
    {
        ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute()
        {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("로딩중 입니다..");
            asyncDialog.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params)
        {
            try
            {
                @SuppressLint("AuthLeak")
                URL url = new URL("ftp://" + edFtpId.getText().toString() + ":" + edFtpPw.getText().toString() + "@" + edFtpUrl.getText().toString());
                //입력받은 아이디와 비밀번호, url 주소를 혼합하여 ftp 접속 주소를 만듭니다.
                // ftp://아이디:비밀번호@주소(파일명까지) 이런 형식을 나눠준겁니다.

                InputStream in = url.openStream(); //url 주소(해당파일)를 InputStream 으로 열기
                InputStreamReader reader = new InputStreamReader(in); //InputStreamReader 로 파일내용 읽어오기

                BufferedReader br = new BufferedReader(reader); //버퍼로 넘겨서 변수에 반환된 값들을 저장

                fileContent = br.readLine(); //이 코드는 첫줄 라인만 String 변수에 저장하는데, for문 쓰셔서 다른 줄까지 저장하시면 됩니다.

                br.close(); //BufferedReader 닫기
                reader.close(); //InputStreamReader 닫기
                in.close(); //InputStream 닫기

                return "성공";
            } catch (IOException e)
            {
                e.printStackTrace();
                return "실패";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            edModi.setText(fileContent); //EditText 에 받아온 값 넣어주기
            asyncDialog.dismiss();

            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show(); //성공 또는 실패 여부 토스트로 띄우기
        }
    }

    private class PushResultAsyncTask extends AsyncTask<Void, Void, String>
    {
        ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute()
        {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("수정중 입니다..");
            asyncDialog.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params)
        {
            try
            {
                @SuppressLint("AuthLeak")
                URL url = new URL("ftp://" + edFtpId.getText().toString() + ":" + edFtpPw.getText().toString() + "@" + edFtpUrl.getText().toString());
                //입력받은 아이디와 비밀번호, url 주소를 혼합하여 ftp 접속 주소를 만듭니다.
                // ftp://아이디:비밀번호@주소(파일명까지) 이런 형식을 나눠준겁니다.

                URLConnection urlc = url.openConnection(); //OutputStream 쓰려면 조합된 url을 URLConnection 으로 변수화 시켜줍니다.

                OutputStream os = urlc.getOutputStream(); //내용을 출력형태로 가져옵니다.
                OutputStream buffer = new BufferedOutputStream(os); //내용을 버퍼에 저장합니다.

                PrintStream output = new PrintStream(buffer);
                //ObjectOutput을 썼었는데 이 메서드는 텍스트 수정용도로 사용하는게 아닙니다.
                //PrintStream 을 쓴 이유의 관한 링크는 https://stackoverflow.com/questions/22222724/unable-to-write-to-text-file-exist-in-ftp-server 여기 참조

                output.print(edModi.getText().toString()); //수정할 내용을 문서에 넣습니다.

                buffer.flush(); //버퍼 값 비워줘야만 내용이 적용됩니다.(중요)

                output.close(); //PrintStream 닫기
                buffer.close(); //OutputStream 닫기
                os.close(); //OutputStream 닫기

                return "성공";
            } catch (IOException e)
            {
                e.printStackTrace();
                return "실패";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            asyncDialog.dismiss();

            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show(); //성공 또는 실패 여부 토스트로 띄우기
        }
    }
}
