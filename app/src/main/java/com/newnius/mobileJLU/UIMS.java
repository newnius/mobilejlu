package com.newnius.mobileJLU;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UIMS extends AppCompatActivity {
    Handler handler;
    Handler termHandler;
    private ListView listView;
    List<UimsCourse> uimsCourses;
    List<UimsTerm> uimsTerms;
    private int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uims);

        if(AccountManager.getUimsCookie()==null) {
            Toast.makeText(this,"尚未登录",Toast.LENGTH_SHORT).show();
            finish();
        }else{

/*        if(AccountManager.getUimsCookie()==null) {
            Intent intentLogin = new Intent(UIMS.this, LoginActivity.class);
            startActivity(intentLogin);
        }*/

        //tobe fix: can not get new cookie

        listView = (ListView)findViewById(R.id.listView);
            try {
                handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                List<HashMap<String, String>> data = new ArrayList<>();
                    for (final UimsCourse course : uimsCourses) {
                        HashMap<String, String> item= new HashMap<>();
                        item.put("courseName", course.getCourse().getCourName());
                        course.getCourse().getCourName();
                        item.put("score", course.getScore() + "");
                        data.add(item);
                    }
                    //创建SimpleAdapter适配器将数据绑定到item显示控件上
                    SimpleAdapter adapter = new SimpleAdapter(UIMS.this, data, R.layout.item_course,
                            new String[]{"courseName","score"}, new int[]{R.id.courName,R.id.courseScore});
                    //实现列表的显示
                    listView.setAdapter(adapter);
                    //条目点击事件
                    listView.setOnItemClickListener(
                            new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
/*                                    ListView listView = (ListView) parent;
                                    HashMap<String, Object> data = (HashMap<String, Object>) listView.getItemAtPosition(position);
                                    String url = data.get("url").toString();

                                    Intent intent = new Intent(UIMS.this, OA_detail.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putCharSequence("url", url);
                                    intent.putExtras(bundle);
                                    startActivity(intent);*/
                                }
                            });
                super.handleMessage(msg);
            }
        };

    termHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                Spinner spinner = (Spinner) findViewById(R.id.c);
                Log.i("terms",uimsTerms.size()+"");

                List<HashMap<String, Object>> data = new ArrayList<>();
                for (final UimsTerm uimsTerm: uimsTerms) {
                    HashMap<String, Object> item= new HashMap<>();
                    item.put("termId", uimsTerm.getTermId());
                    item.put("termName", uimsTerm.getTermName());
                    data.add(item);
                }

                SimpleAdapter adapter = new SimpleAdapter(UIMS.this, data, R.layout.my_simple_spinner_item, new String[]{"termName"}, new int[]{R.id.title});

                adapter.setDropDownViewResource(R.layout.my_simple_spinner_item);
                spinner.setAdapter(adapter);
                spinner.setSelection(1,true);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        HashMap<String, Object> data = (HashMap<String, Object>)parent.getItemAtPosition(position);
                        Log.i("spinner", data.get("termId").toString());
                        getScoreByTerm(AccountManager.getUimsCookie(), Integer.parseInt(data.get("termId").toString()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                super.handleMessage(msg);
            }catch(Exception e){e.printStackTrace();}
        }
    };

            getTerms(AccountManager.getUimsCookie());
                getUserInfo(AccountManager.getUimsCookie());
        getLatestScore(AccountManager.getUimsCookie());

            }catch(Exception e){
                e.printStackTrace();
            }

        }



    }

    public void get(final String location,final String cookie){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sourceCode = "";
                String str = null;
                try {
                    URL url = new URL(location);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setConnectTimeout(3 * 1000);
                    conn.setRequestProperty("Cookie", cookie);

                    Log.i("uims", conn.getHeaderField("set-cookie"));
                    Map map = conn.getHeaderFields();
                    for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                        Object obj = i.next();
                        System.out.println("key=" + obj + " value=" + map.get(obj));
                    }
                    if(conn.getResponseCode() != 200){
                        return ;
                    }
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    while((str = br.readLine()) != null){
                        sourceCode += str;
                    }
                    Log.i("res", sourceCode);
                    conn.disconnect();

                }catch(Exception e){
                    e.printStackTrace();
                    Log.i("http", e.toString());
                }
            }
        }).start();
    }

    public void post(final String location, final String data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(location);
                    HttpURLConnection uRLConnection = (HttpURLConnection)url.openConnection();
                    uRLConnection.setDoInput(true);
                    uRLConnection.setDoOutput(true);
                    uRLConnection.setRequestMethod("POST");
                    uRLConnection.setUseCaches(false);
                    uRLConnection.setInstanceFollowRedirects(false);
                    uRLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    uRLConnection.connect();

                    DataOutputStream out = new DataOutputStream(uRLConnection.getOutputStream());
                    out.writeBytes(data);
                    out.flush();
                    out.close();

                    InputStream is = uRLConnection.getInputStream();


/*                    Map map = uRLConnection.getHeaderFields();
                    for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                        Object obj = i.next();
                        System.out.println("key=" + obj + " value=" + map.get(obj));
                    }*/

                    Log.i("jj", uRLConnection.getHeaderField("Location"));
                    Log.i("jj", uRLConnection.getHeaderField("Set-Cookie"));
                    if(uRLConnection.getHeaderField("Location").contains("index.do")){
                        post("", uRLConnection.getHeaderField("Set-Cookie"));
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String response = "";
                    String readLine = null;
                    while((readLine =br.readLine()) != null){
                        response = response + readLine;
                    }
                    is.close();
                    br.close();
                    uRLConnection.disconnect();
                    Log.i("uims_login", response);
                    return ;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }).start();
    }

    public void getLatestScore(final String cookie){
        Log.i("uims", "getlateltscore");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://uims.jlu.edu.cn/ntms/service/res.do");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setUseCaches(false);
                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Cookie", cookie);
                    conn.connect();

                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                    out.writeBytes("{\"tag\":\"archiveScore@queryCourseScore\",\"branch\":\"latest\",\"params\":{},\"rowLimit\":15}");
                    out.flush();
                    out.close();

                    InputStream is = conn.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String response = "";
                    String readLine = null;
                    while((readLine =br.readLine()) != null){
                        response = response + readLine;
                    }
                    is.close();
                    br.close();
                    conn.disconnect();
                    Log.i("getLatestScore", response);

                    UimsMsgCourse uimsMsgCourses = new Gson().fromJson(response, UimsMsgCourse.class);


/*                    List<UimsCourse> list = new Gson().fromJson(new Gson().toJson(uimsMsg.getValue()), new TypeToken<List<UimsCourse>>() {}.getType());
                    uimsMsg.setValue(list);
                    uimsCourses = list;*/
                    uimsCourses = uimsMsgCourses.getValue();
                    Message msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                    return ;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }).start();
    }

    public void getScoreByTerm(final String cookie, final int termId){
        Log.i("uims", "getScoreByTerm");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://uims.jlu.edu.cn/ntms/service/res.do");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setUseCaches(false);
                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Cookie", cookie);
                    conn.connect();

                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                    out.writeBytes("{\"tag\":\"archiveScore@queryCourseScore\",\"branch\":\"byTerm\",\"params\":{\"studId\":" + userId + ",\"termId\":"+termId+"},\"orderBy\":\"teachingTerm.termId, course.courName\"}");
                    out.flush();
                    out.close();

                    InputStream is = conn.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String response = "";
                    String readLine = null;
                    while((readLine =br.readLine()) != null){
                        response = response + readLine;
                    }
                    is.close();
                    br.close();
                    conn.disconnect();
                    Log.i("getScoreByTerm", response);

                    UimsMsgCourse uimsMsgCourses = new Gson().fromJson(response, UimsMsgCourse.class);


                    uimsCourses = uimsMsgCourses.getValue();
                    Message msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                    return ;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }).start();
    }

    public void getTerms(final String cookie) {

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i("aa", "sssssssssssss");

                        URL url = new URL("http://uims.jlu.edu.cn/ntms/service/res.do");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setUseCaches(false);
                        conn.setInstanceFollowRedirects(false);
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Cookie", cookie);
                        conn.connect();


                        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                        out.writeBytes("{\"type\":\"search\",\"res\":\"teachingTerm\",\"orderBy\":\"termName desc\",\"tag\":\"teachingTerm\",\"branch\":\"default\",\"params\":{}}");
                        out.flush();
                        out.close();

                        InputStream is = conn.getInputStream();

                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        String response = "";
                        String readLine = null;
                        while ((readLine = br.readLine()) != null) {
                            response = response + readLine;
                        }
                        is.close();
                        br.close();
                        conn.disconnect();
                        UimsMsgTerm uimsMsgTerm = new Gson().fromJson(response, UimsMsgTerm.class);
                        uimsTerms = uimsMsgTerm.getValue();
                        Message msg = termHandler.obtainMessage();
                        termHandler.sendMessage(msg);
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUserInfo(final String cookie){
        Log.i("uims", "getUserInfo");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://uims.jlu.edu.cn/ntms/action/getCurrentUserInfo.do");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    //conn.setDoInput(true);
                    //conn.setDoOutput(true);
                    //conn.setRequestMethod("GET");
                    //conn.setUseCaches(false);
                    //conn.setInstanceFollowRedirects(false);
                    //conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Cookie", cookie);
                    conn.connect();

                    InputStream is = conn.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String response = "";
                    String readLine = null;
                    while((readLine =br.readLine()) != null){
                        response = response + readLine;
                    }
                    is.close();
                    br.close();
                    conn.disconnect();
                    Log.i("userinfo", response);
                    UimsStuInfo uimsStuInfo = new Gson().fromJson(response, UimsStuInfo.class);
                    userId = uimsStuInfo.getUserId();
                    return ;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }).start();
    }
}