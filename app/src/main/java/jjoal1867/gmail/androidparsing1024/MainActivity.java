package jjoal1867.gmail.androidparsing1024;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    //인스턴스 변수선언
    EditText id, passwd;
    LinearLayout linearLayout;
    Button button;
    ProgressDialog progressDialog;


    //스레드로 작업 한 후, 화면 갱신을 위한 핸들러객체
    //1개만 있으면 Message의 what 속성을 이용해서 계속 사용할 수 있기 때문에 바로 인스턴스를 생성해도 됩니다.
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //진행대화상자 닫아주는 코드
            progressDialog.dismiss();
            if(msg.what==1){
                linearLayout.setBackgroundColor(Color.RED);
            }else if(msg.what==2){
                linearLayout.setBackgroundColor(Color.GREEN);
            }
        }
    };

    //비동기적으로 작업을 수행하기 위한 스레드 클래스
    //스레드는 재사용이 불가능하기 때문에 필요할 때마다 인스턴스를 만들어서 사용해야 합니다.
    class ThreadEx extends Thread{
        @Override
        public void run() {
            try{
                String addr = "http://192.168.0.251:8080/androidserver/login?id=";
                String logid =id.getText().toString();
                String logpw = passwd.getText().toString();
                //파라미터가 두개일 경우 반드시 &를 넣어야합니다. "&이름=" 누락되지 않도록 주의! - 특히 & 잊지않도록 주의합니다.
                addr = addr + logid +"&pw="+logpw;

                //*문자열을 다운로드 받는 코드는 거의 고정되어 있습니다.
                //문자열 주소를 URL로 변경
                URL url = new URL(addr);
                //연결객체 생성
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                //옵션설정
                //캐시사용여부 *캐시: 로컬에 저장해 두고 사용하는 것
                //웹에선는 빨리 출력하기 위해서 캐시를 사용 - 인터넷이 안돼도 화면이 보여지는 경우가 캐시를 이용한 경우이다.
                con.setUseCaches(false);
                //접속을 시도하는 최대시간 : 30초 동안 접속이 안되면 예외를 발생시키며, 설정을 안할경우에는 무한대기에 빠진다.
                con.setConnectTimeout(30000);

                //문자열을 다운로드 받을 스트림생성
                BufferedReader br= new BufferedReader(new InputStreamReader(con.getInputStream()));

                //다운로드 받을 때는 StringBuilder를 이용합니다.
                StringBuilder sb = new StringBuilder();
                while(true){
                    String line = br.readLine();
                    if(line==null){
                        break;
                    }
                    sb.append(line+"\n");
                }
                br.close();
                con.disconnect();
                //제대로 다운로드 받았는지 확인해봅니다.
                Log.e("다운로드 받은 데이터",sb.toString());

                //Parsing작업
                //{"nickname":"관리","id":"root"} 데이터에 중괄호가 있으니 JSONObject를 이용합니다.
                JSONObject result = new JSONObject(sb.toString());
                //속성인 id를 가져옵니다.
                String x = result.getString("id");

                //파싱한 결과를 가지고 로그인실패/성공했을 때 각각의 Message의 what을 다르게 설정해 핸들러에게 전송합니다.
                Message msg = new Message();
                //로그인실패했을 때
                if(x.equals("null")){
                    Log.e("로그인여부","실패");
                    msg.what=1;
                }//로그인성공했을 때
                else{
                    Log.e("로그인여부","성공");
                    msg.what=2;
                }
                //핸들러에게 메시지를 전송합니다.
                handler.sendMessage(msg);
            }catch (Exception e){
                Log.e("다운로드실패",e.getMessage());
            }
        }
    }

    //Activity가 만들어질 때, onCreate 메소드가 호출됩니다.
    //*만약 Activity가 실행될 때 작업을 하고 싶다면, onResume 메소드를 사용합니다.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //layout파일을 읽어 메모리에 로드한 후, 화면출력을 위해 준비하는 메소드를 호출합니다.
        setContentView(R.layout.activity_main);

        id=(EditText)findViewById(R.id.id);
        passwd=(EditText)findViewById(R.id.passwd);
        linearLayout=(LinearLayout)findViewById(R.id.layout01);
        button=(Button)findViewById(R.id.loginButton);

        //버튼의 클릭이벤트 처리 : 버튼을 누르면 수행할 내용
        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                //ProgressDialog(진행대화상자) 출력
                progressDialog = ProgressDialog.show(MainActivity.this,"로그인","로그인 처리 중");

                //스레드를 만들어서 실행
                ThreadEx th = new ThreadEx();
                th.start();
            }
        });
    }
}
