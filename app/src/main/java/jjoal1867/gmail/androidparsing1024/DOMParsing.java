package jjoal1867.gmail.androidparsing1024;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DOMParsing extends AppCompatActivity {
    SwipeRefreshLayout swipeRefreshLayout;

    //ListView출력에 필요한 데이터를 인스턴스 변수로 선언
    //ListView에 출력될 데이터
    ArrayList<String> list;
    //출력을 위한 ListView
    ListView listView;
    //데이터와 ListView를 연결시켜줄 Adapter
    ArrayAdapter<String> adapter;


    //진행상황을 출력할 대화상자
    ProgressDialog progressDialog;

    //웹에서 다운로드 받을 스레드
    class ThreadEx extends Thread{
        @Override
        public void run() {
            //다운로드 받은 문자열을 저장할 객체
            StringBuilder sb = new StringBuilder();
            try{
                //*문자열을 다운로드 받는 코드는 거의 고정되어 있습니다.
                //문자열을 다운로드 받는 코드 영역
                URL url = new URL("http://www.hani.co.kr/rss/international/ ");

                //연결객체 생성
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                //옵션설정
                //캐시사용여부 *캐시: 로컬에 저장해 두고 사용하는 것
                //웹에선는 빨리 출력하기 위해서 캐시를 사용 - 인터넷이 안돼도 화면이 보여지는 경우가 캐시를 이용한 경우입니다.
                con.setUseCaches(false);
                //접속을 시도하는 최대시간 : 30초 동안 접속이 안되면 예외를 발생시키며, 설정을 안할경우에는 무한대기에 빠지게 됩니다.
                con.setConnectTimeout(30000);

                //문자열을 다운로드 받을 스트림생성
                BufferedReader br= new BufferedReader(new InputStreamReader(con.getInputStream()));

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
                Log.e("다운로드 받은 문자열",sb.toString());
            }catch(Exception e){
                Log.e("다운로드 실패",e.getMessage());
            }

            //XML 파싱
            try{
                //파싱을 수행할 객체 생성
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                //다운로드 받은 문자열을 InputStream으로 변환
                InputStream istream = new ByteArrayInputStream(sb.toString().getBytes("utf-8"));
                //메모리에 펼치기
                Document doc = builder.parse(istream);
                //루트 가져오기
                Element root = doc.getDocumentElement();
                //----------------------------------------여기까지는 정해져 있는 코드입니다.

                //원하는 태그의 데이터를 가져오기
                NodeList items = root.getElementsByTagName("title");
                Log.e("items",items.toString());
                //반복문으로 태그를 순회
                for(int i=1; i<items.getLength();i=i+1){
                    //태그를 하나씩 가져오기
                    Node node = items.item(i);
                    //태그 안의 문자열을 가져와서 리스트에 추가
                    Node contents = node.getFirstChild();
                    String title = contents.getNodeValue();
                    list.add(title);
                }
                //핸들러 호출
                handler.sendEmptyMessage(0);
            }
            catch(Exception e){
                Log.e("XML Parsing 실패",e.getMessage());
            }
        }
    }

    //화면을 갱신할 핸들러
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyDataSetChanged();
            progressDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domparsing);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new ThreadEx().start();
            }
        });


        //ListView를 출력하기 위한 데이터 생성
        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list);
        listView = (ListView)findViewById(R.id.listView);

        //데이터와 ListView를 연결
        listView.setAdapter(adapter);

        //진행대화상자 출력
        progressDialog = ProgressDialog.show(this,"한겨레","다운로드중");

        //스레드를 생성하고 시작
        ThreadEx th = new ThreadEx();
        th.start();
    }
}
