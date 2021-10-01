package com.dt.s200128.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.dt.s200128.LoginActivity;
import com.dt.s200128.R;
import com.dt.s200128.adapter.PagerAdapter;
import com.dt.s200128.foreground_service.ForegroundService;
import com.dt.s200128.helper.ObjectInOut;
import com.dt.s200128.menu.Menu1;
import com.dt.s200128.menu.Menu3;
import com.dt.s200128.menu.Menu4;
import com.dt.s200128.menu.Menu5;
import com.dt.s200128.message.MessageBox;
import com.dt.s200128.message.Message_List;
import com.dt.s200128.model.Order;
import com.dt.s200128.model.Store;
import com.google.android.material.tabs.TabLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {
    public static boolean stop;

    public static Context current_context;
    public static Context main_context;
    public static TextView textView_netpay;
    public static TextView textView_notice;
    public static TextView text_stop;

    DrawerLayout drawer;
    Activity activity;
    TabLayout tabs;
    ViewPager pager;
    TextView textView_name;
    AsyncHttpClient client;

    List<Order> list_card_return;

    int notices_count;
    int text_count;
    List<String> list_memo;
    int cycle = 0;

    public static Button button_msg;
    public static TabLayout.Tab tab1;
    public static TabLayout.Tab tab2;
    public static TabLayout.Tab tab3;
    public static boolean restart0;
    public static boolean restart1; // 화면 구성상 쓰레드를 재시작하고 싶을때 true, 평소에 false;
    public static boolean restart2;
    public static boolean restart3;

    public static int selected_tap = 0;

    LinearLayout layout_b1, layout_b2, layout_b3, layout_b4, layout_b5, layout_b6;

    Button button_exit;

    MainFragment1 mainFragment1;
    MainFragment2 mainFragment2;
    MainFragment3 mainFragment3;

    MainFragment1_menu mainFragment1_menu;
    MainFragment2_menu mainFragment2_menu;
    TabLayout.Tab tab1_menu;
    TabLayout.Tab tab2_menu;
    TabLayout tabs_menu;
    ViewPager pager_menu;

    MainThread1 mainThread1;
    MainThread2 mainThread2;
    MainThread3 mainThread3;
    Notice_thread notice_thread;

    public static String notice_memo = "";

    AlertDialog alertDialog;
    String save_text;

    private static final long MIN_CLICK_INTERVAL=600;
    private long mLastClickTime;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);

        if(!Menu5.display_onoff){
            // 화면 꺼짐 방지
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if(Store.store_nm == null) {
            MainActivity.process_stop(this);
            return;
        }
        if (ForegroundService.first) {
            MainActivity.stop = true;
            restart0 = true;
            restart1 = true;
            restart2 = true;
            restart3 = true;
            // 메인화면에 대기, 진행, 완료 갯수를 표시할 때
            // 초기화가 안되있으면 에러가 나서 이렇게함
            MainFragment1.main_listA = new ArrayList<>();
            MainFragment1.mainAdapterA = new MainAdapter_A(this, R.layout.main_list_a, MainFragment1.main_listA);
            MainFragment2.main_listBC = new ArrayList<>();
            MainFragment2.mainAdapterBC = new MainAdapter_BC(this, R.layout.main_list_b, MainFragment2.main_listBC);
            MainFragment3.main_listD = new ArrayList<>();
            MainFragment3.mainAdapterD = new MainAdapter_DE(this, R.layout.main_list_d, MainFragment3.main_listD);
        }
        declare();

        addTab_menu();

        addTab();

        if (ForegroundService.first) {
            start_app();
        }
    }
    @Override
    protected void onDestroy() {
        for(int i = 0; i < ForegroundService.list_dialog.size(); i++) {
            ForegroundService.list_dialog.get(i).cancel();
            ForegroundService.list_dialog.remove(i);
        }
        MainActivity.stop = true;
        ForegroundService.first = true;

        super.onDestroy();
    }

    @SuppressLint("RestrictedApi")
    private void declare() {
        current_context = MainActivity.this;
        main_context = MainActivity.this;
        textView_netpay = findViewById(R.id.textView_netpay);
        textView_notice = findViewById(R.id.text_notice);
        textView_notice.setSelected(true);
        text_stop = findViewById(R.id.text_stop);

        button_msg = findViewById(R.id.button_msg);
        button_msg.setOnClickListener(this);
        button_exit = findViewById(R.id.button_exit);
        button_exit.setOnClickListener(this);

        activity = this;

        textView_name = findViewById(R.id.textView_name);
        textView_name.setText(Store.store_nm);

        layout_b1 = findViewById(R.id.layout_b1);
        layout_b2 = findViewById(R.id.layout_b2);
        layout_b3 = findViewById(R.id.layout_b3);
        layout_b4 = findViewById(R.id.layout_b4);
        layout_b5 = findViewById(R.id.layout_b5);
        layout_b6 = findViewById(R.id.layout_b6);

        layout_b1.setOnClickListener(this);
        layout_b2.setOnClickListener(this);
        layout_b3.setOnClickListener(this);
        layout_b4.setOnClickListener(this);
        layout_b5.setOnClickListener(this);
        layout_b6.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        list_memo = new ArrayList<>();
    }
    private void addTab_menu() {
        mainFragment1_menu = new MainFragment1_menu();
        mainFragment2_menu = new MainFragment2_menu();

        pager_menu = (ViewPager) findViewById(R.id.viewpager_menu);

        // 프레그먼트 리스트에 추가
        List<Fragment> listFragments = new ArrayList<>();
        listFragments.add(mainFragment1_menu);
        listFragments.add(mainFragment2_menu);

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), listFragments);
        pager_menu.setAdapter(adapter);

        tabs_menu = (TabLayout) findViewById(R.id.tabs_menu);

        tab1_menu = new TabLayout.Tab();
        tab2_menu = new TabLayout.Tab();

        tab1_menu = tabs_menu.newTab().setText("공지사항");
        tab2_menu = tabs_menu.newTab().setText("알림");

        tabs_menu.addTab(tab1_menu);
        tabs_menu.addTab(tab2_menu);
        // 여기 아래로는 페이지 변경 시 이벤트
        pager_menu.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs_menu));

        tabs_menu.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        pager_menu.setCurrentItem(0);
                        break;
                    case 1:
                        pager_menu.setCurrentItem(1);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void addTab() {
        mainFragment1 = new MainFragment1();
        mainFragment2 = new MainFragment2();
        mainFragment3 = new MainFragment3();

        pager = (ViewPager) findViewById(R.id.viewpager);

        // viewpage가 3개까지는 지워지지 않고 사용됨
        pager.setOffscreenPageLimit(3);

        // 프레그먼트 리스트에 추가
        List<Fragment> listFragments = new ArrayList<>();
        listFragments.add(mainFragment1);
        listFragments.add(mainFragment2);
        listFragments.add(mainFragment3);

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), listFragments);
        pager.setAdapter(adapter);

        tabs = (TabLayout) findViewById(R.id.tabs);

        tab1 = new TabLayout.Tab();
        tab2 = new TabLayout.Tab();
        tab3 = new TabLayout.Tab();

        tab1 = tabs.newTab().setText("준비 [ " + MainFragment1.main_listA.size() + " ]");
        tab2 = tabs.newTab().setText("진행 [ " + MainFragment2.main_listBC.size() + " ]");
        tab3 = tabs.newTab().setText("완료 [ " + MainFragment3.main_listD.size() + " ]");

        tabs.addTab(tab1);
        tabs.addTab(tab2);
        tabs.addTab(tab3);
        // 여기 아래로는 페이지 변경 시 이벤트
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selected_tap = tab.getPosition();
                invalidateOptionsMenu();
                switch (tab.getPosition()) {
                    case 0:
                        pager.setCurrentItem(0);
                        break;
                    case 1:
                        pager.setCurrentItem(1);
                        break;
                    case 2:
                        pager.setCurrentItem(2);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {

                }
            }
        });

        switch (selected_tap) {
            case 0:
                pager.setCurrentItem(0);
                break;
            case 1:
                pager.setCurrentItem(1);
                break;
            case 2:
                pager.setCurrentItem(2);
                break;
        }
    }
    private void start_app() {
        Store.msg_pk_m = 0;
        // Thread 를 돌리기 위해서 기본설정 후 실행
        MainActivity.stop = false;

        MainActivity.restart0 = false;
        MainActivity.restart1 = false;
        MainActivity.restart2 = false;
        MainActivity.restart3 = false;

        if (!isLaunchingService_fore(this)) {
            Intent serviceIntent = new Intent(this, ForegroundService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }

        mainThread1 = new MainThread1(this);
        mainThread1.start();

        mainThread2 = new MainThread2();
        mainThread2.start();

        mainThread3 = new MainThread3();
        mainThread3.start();

        notice_thread = new Notice_thread();
        notice_thread.start();

        MainActivity.restart0 = true;
        MainActivity.restart1 = true;
        MainActivity.restart2 = true;
        MainActivity.restart3 = true;

        ForegroundService.first = false;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        long currentClickTime= SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime-mLastClickTime;
        if(elapsedTime<=MIN_CLICK_INTERVAL){
            return;
        }
        switch (view.getId()) {
            case R.id.layout_b1:
                if (elapsedTime <= MIN_CLICK_INTERVAL) {
                    return;
                }
                intent = new Intent(this, Menu1.class); // 기사정보
                startActivity(intent);
                break;//
            case R.id.layout_b2:
                if (elapsedTime <= MIN_CLICK_INTERVAL) {
                    return;
                }
                intent = new Intent(this, Menu3.class); // 운행내역
                startActivity(intent);
                break;
            case R.id.layout_b3:
                if (elapsedTime <= MIN_CLICK_INTERVAL) {
                    return;
                }
                intent = new Intent(this, Message_List.class);
                startActivity(intent);
                break;//
            case R.id.layout_b4:
                if (elapsedTime <= MIN_CLICK_INTERVAL) {
                    return;
                }
                intent = new Intent(this, Menu4.class); // 적립금내역
                startActivity(intent);
                break;
            case R.id.layout_b5:
                if (elapsedTime <= MIN_CLICK_INTERVAL) {
                    return;
                }
                String[] items = {"카드결제", "결제목록(취소)"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setItems(items, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            card_dialog();
                        } else if (item == 1) {
                            list_card_return = new ArrayList<>();

                            Response_card_return response1 = new Response_card_return();
                            RequestParams params1 = new RequestParams();
                            params1.put("STORE_PK", Store.log_id);

                            client.get(Store.store_url + "/fnList_card_return.do", params1, response1);
                        }
                    }

                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.layout_b6:
                if (elapsedTime <= MIN_CLICK_INTERVAL) {
                    return;
                }
                intent = new Intent(this, Menu5.class); // 환경설정
                startActivity(intent);
                break;
            case R.id.button_exit:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("프로그램 종료");
                alertDialogBuilder.setMessage("업무를 종료하시겠습니까?")
                        .setNeutralButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(android.os.Build.VERSION.SDK_INT >= 29) {
                                    File savefile = new File(LoginActivity.file_dir_login_ing);
                                    try{
                                        FileOutputStream fos = new FileOutputStream(savefile);
                                        fos.write("0".getBytes());
                                        fos.close();
                                    } catch(IOException e){}
                                } else {
                                    boolean result1 = ObjectInOut.getInstance().write(LoginActivity.file_dir_login_ing, "0");
                                }

                                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                notificationManager.cancelAll();

                                activity.finishAffinity();
                                System.exit(0);
                            }
                        })
                        .setCancelable(false); // 백버튼으로 팝업창이 닫히지 않도록 한다.

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                break;
            case R.id.button_msg:
                intent = new Intent(this, MessageBox.class);
                startActivity(intent);
                button_msg.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        getMenuInflater().inflate(R.menu.menu1, menu);

        return super.onCreateOptionsMenu(menu);
    }
    public void onMenuClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu:
                long currentClickTime= SystemClock.uptimeMillis();
                long elapsedTime=currentClickTime-mLastClickTime;
                mLastClickTime=currentClickTime;

                if(elapsedTime<=MIN_CLICK_INTERVAL){
                    return;
                }
                if(Store.pro_yn.equals("Y")){
                    Intent intent = new Intent(this, Order_SearchActivity.class);
                    intent.putExtra("divi", "0");
                    startActivity(intent);
                } else {
                    Toast.makeText(current_context,"주문접수 정지중입니다.",Toast.LENGTH_LONG).show();
                }


                break;
        }
    }
    public void card_dialog() { // 카드결제 누르면 금액입력 창 dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.current_context);
        View view1 = View.inflate(MainActivity.current_context, R.layout.dialog_edit, null);
        alertDialogBuilder.setView(view1);

        LinearLayout layout_main = (LinearLayout) view1.findViewById(R.id.layout_main);
        final LinearLayout layout_progress = (LinearLayout) view1.findViewById(R.id.layout_progress);
        layout_progress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        TextView textView_title=(TextView) view1.findViewById(R.id.textView_title);
        final EditText editText_content=(EditText) view1.findViewById(R.id.editText_content);
        TextView textView_cancel=(TextView) view1.findViewById(R.id.textView_cancel);
        TextView textView_ok=(TextView) view1.findViewById(R.id.textView_ok);

        textView_title.setText("결제금액 입력");
        textView_cancel.setText("취소");
        textView_ok.setText("결제");

        editText_content.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        editText_content.selectAll();
        editText_content.requestFocus();
        final DecimalFormat df = new DecimalFormat("###,###.####");
        final String[] result = {""};
        editText_content.addTextChangedListener(new TextWatcher(){

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(result[0])){     // StackOverflow를 막기위해,
                    if (s.length() <= 11) {
                        if (s.length() < 1) {
                            result[0] = "0";
                        } else {
                            result[0] = df.format(Long.parseLong(s.toString().replaceAll(",", "")));   // 에딧텍스트의 값을 변환하여, result에 저장.
                        }
                        editText_content.setText(result[0]);    // 결과 텍스트 셋팅.
                        editText_content.setSelection(result[0].length());     // 커서를 제일 끝으로 보냄.

                        save_text = result[0];
                        editText_content.setText(result[0]);    // 결과 텍스트 셋팅.
                        editText_content.setSelection(result[0].length());     // 커서를 제일 끝으로 보냄.
                    } else {
                        editText_content.setText(save_text);
                        editText_content.setSelection(save_text.length());
                        Toast.makeText(MainActivity.this,"설정하신 금액이 잘못되었습니다.",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        editText_content.setText("0");
        editText_content.selectAll();

        textView_cancel.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        alertDialog.cancel();
                    }
                }
        );
        textView_ok.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) { // ok
                        String et1_string = editText_content.getText().toString().trim();
                        String tot = et1_string.replaceAll(",","");
                        double tt = Double.parseDouble(tot) ;
                        double ttt = tt-(tt/1.1);

                        SimpleDateFormat dt = new SimpleDateFormat("yyMMddHHmmss");
                        Date date = new Date();
                        String now = dt.format(date);

                        ComponentName compName = new ComponentName("kr.co.kicc.ecm","kr.co.kicc.ecm.SmartCcmsMain");

                        Intent intent = new Intent(Intent.ACTION_MAIN);

                        intent.putExtra("TRAN_NO", "1");
                        intent.putExtra("APPCALL_TRAN_NO", now);
                        intent.putExtra("TRAN_TYPE", "credit");
                        intent.putExtra("TOTAL_AMOUNT",tot);
                        intent.putExtra("TAX",String.valueOf(Math.round(ttt)));
                        intent.putExtra("TIP","0");
                        intent.putExtra("INSTALLMENT","0");
                        intent.putExtra("SHOP_TID", Store.tid);
                        intent.putExtra("SHOP_BIZ_NUM",Store.shop_biz_num.replace("-",""));
                        intent.putExtra("SHOP_NAME",Store.bus_nm);
                        intent.putExtra("SHOP_OWNER",Store.region_nm);
                        intent.putExtra("SHOP_ADDRESS",Store.addr);
                        intent.putExtra("SHOP_TEL",Store.tel1);
                        intent.putExtra("ORDER_NUM",Store.log_id);
                        intent.putExtra("CUSTOMER_CODE",Store.log_id);

                        System.out.println(now);
                        System.out.println(tot);
                        System.out.println(Math.round(ttt));
                        System.out.println(Store.tid);
                        System.out.println(Store.shop_biz_num);
                        System.out.println(Store.bus_nm);
                        System.out.println(Store.region_nm);
                        System.out.println(Store.addr);
                        System.out.println(Store.tel1);
                        System.out.println(Store.log_id);
                        System.out.println(Store.log_id);

                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.setComponent(compName);

                        startActivityForResult(intent, 1);
                    }
                }
        );
        alertDialogBuilder.setCancelable(false);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {
            if(requestCode==1) {
                String result_code = data.getStringExtra("RESULT_CODE");

                AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
                asyncHttpClient.setMaxRetriesAndTimeout(20,3000); // 핸드폰 인터넷이 끊어졋을떄 50초
                asyncHttpClient.setTimeout(60000); // 서버가 끊어졋을떄 10초

                if(result_code.equals("0000")) {
                    alertDialog.cancel();

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.current_context);
                    View view1 = View.inflate(MainActivity.current_context, R.layout.dialog_yn, null);
                    alertDialogBuilder.setView(view1);

                    LinearLayout layout_main = (LinearLayout) view1.findViewById(R.id.layout_main);
                    final LinearLayout layout_progress = (LinearLayout) view1.findViewById(R.id.layout_progress);
                    layout_progress.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return true;
                        }
                    });

                    TextView textView_title=(TextView) view1.findViewById(R.id.textView_title);
                    TextView textView_content=(TextView) view1.findViewById(R.id.textView_content);
                    TextView textView_cancel=(TextView) view1.findViewById(R.id.textView_cancel);
                    TextView textView_ok=(TextView) view1.findViewById(R.id.textView_ok);

                    layout_progress.setVisibility(View.VISIBLE);

                    textView_title.setText("카드결제");
                    textView_content.setText("카드결제 완료되었습니다.");
                    textView_cancel.setVisibility(View.GONE);
                    textView_ok.setText("확인");

                    textView_cancel.setOnClickListener(
                            new View.OnClickListener() {
                                public void onClick(View v) {

                                }
                            }
                    );
                    textView_ok.setOnClickListener(
                            new View.OnClickListener() {
                                public void onClick(View v) { // ok
                                    alertDialog.cancel();
                                }
                            }
                    );

                    alertDialogBuilder.setCancelable(false);
                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    Response_insert_card_return response = new Response_insert_card_return(layout_progress);
                    RequestParams params = new RequestParams();

                    params.put("ORDER_NUM", data.getStringExtra("ORDER_NUM"));
                    params.put("CUSTOMER_CODE", data.getStringExtra("CUSTOMER_CODE"));
                    params.put("TRAN_NO", data.getStringExtra("TRAN_NO"));
                    params.put("TRAN_TYPE", data.getStringExtra("TRAN_TYPE"));
                    params.put("CARD_NUM", data.getStringExtra("CARD_NUM"));
                    params.put("CARD_NAME", data.getStringExtra("CARD_NAME"));
                    params.put("TOTAL_AMOUNT", data.getStringExtra("TOTAL_AMOUNT"));
                    params.put("TAX", data.getStringExtra("TAX"));
                    params.put("TIP", data.getStringExtra("TIP"));
                    params.put("INSTALLMENT", data.getStringExtra("INSTALLMENT"));
                    params.put("RESULT_CODE", data.getStringExtra("RESULT_CODE"));
                    params.put("RESULT_MSG", data.getStringExtra("RESULT_MSG"));
                    params.put("APPROVAL_NUM", data.getStringExtra("APPROVAL_NUM"));
                    params.put("RIDER_NM", data.getStringExtra("RIDER_NM"));
                    params.put("APPROVAL_DATE", data.getStringExtra("APPROVAL_DATE"));
                    params.put("ACQUIRER_CODE", data.getStringExtra("ACQUIRER_CODE"));
                    params.put("ACQUIRER_NAME", data.getStringExtra("ACQUIRER_NAME"));
                    params.put("MERCHANT_NUM", data.getStringExtra("MERCHANT_NUM"));
                    params.put("SHOP_TID", data.getStringExtra("SHOP_TID"));
                    params.put("SHOP_BIZ_NUM", data.getStringExtra("SHOP_BIZ_NUM"));
                    params.put("SHOP_NAME", data.getStringExtra("SHOP_NAME"));
                    params.put("SHOP_OWNER", data.getStringExtra("SHOP_OWNER"));
                    params.put("SHOP_ADDRESS", data.getStringExtra("SHOP_ADDRESS"));
                    params.put("SHOP_TEL", data.getStringExtra("SHOP_TEL"));
                    params.put("ADD_FIELD", data.getStringExtra("ADD_FIELD"));
                    params.put("TRAN_SERIALNO", data.getStringExtra("TRAN_SERIALNO"));
                    params.put("APPCALL_TRAN_NO", data.getStringExtra("APPCALL_TRAN_NO"));
                    params.put("RESPONSE_TYPE", data.getStringExtra("RESPONSE_TYPE"));
                    params.put("TPK", data.getStringExtra("TPK"));
                    params.put("TMK", data.getStringExtra("TMK"));

                    asyncHttpClient.get(Store.store_url + "/fnInsert_card_return_info.do", params, response);
                }
            }
            else if(requestCode==2) {
                String result_code = data.getStringExtra("RESULT_CODE");

                AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
                asyncHttpClient.setMaxRetriesAndTimeout(20,3000); // 핸드폰 인터넷이 끊어졋을떄 50초
                asyncHttpClient.setTimeout(60000); // 서버가 끊어졋을떄 10초

                if(result_code.equals("0000")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.current_context);
                    View view1 = View.inflate(MainActivity.current_context, R.layout.dialog_yn, null);
                    alertDialogBuilder.setView(view1);

                    LinearLayout layout_main = (LinearLayout) view1.findViewById(R.id.layout_main);
                    final LinearLayout layout_progress = (LinearLayout) view1.findViewById(R.id.layout_progress);
                    layout_progress.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return true;
                        }
                    });

                    TextView textView_title=(TextView) view1.findViewById(R.id.textView_title);
                    TextView textView_content=(TextView) view1.findViewById(R.id.textView_content);
                    TextView textView_cancel=(TextView) view1.findViewById(R.id.textView_cancel);
                    TextView textView_ok=(TextView) view1.findViewById(R.id.textView_ok);

                    layout_progress.setVisibility(View.VISIBLE);

                    textView_title.setText("카드결제");
                    textView_content.setText("카드결제취소 완료되었습니다.");
                    textView_cancel.setVisibility(View.GONE);
                    textView_ok.setText("확인");

                    textView_cancel.setOnClickListener(
                            new View.OnClickListener() {
                                public void onClick(View v) {

                                }
                            }
                    );
                    textView_ok.setOnClickListener(
                            new View.OnClickListener() {
                                public void onClick(View v) { // ok
                                    alertDialog.cancel();
                                }
                            }
                    );

                    alertDialogBuilder.setCancelable(false);
                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    Response_insert_card_return response = new Response_insert_card_return(layout_progress);
                    RequestParams params = new RequestParams();

                    params.put("ORDER_NUM", data.getStringExtra("ORDER_NUM"));
                    params.put("CUSTOMER_CODE", data.getStringExtra("CUSTOMER_CODE"));
                    params.put("TRAN_NO", data.getStringExtra("TRAN_NO"));
                    params.put("TRAN_TYPE", data.getStringExtra("TRAN_TYPE"));
                    params.put("CARD_NUM", data.getStringExtra("CARD_NUM"));
                    params.put("CARD_NAME", data.getStringExtra("CARD_NAME"));
                    params.put("TOTAL_AMOUNT", data.getStringExtra("TOTAL_AMOUNT"));
                    params.put("TAX", data.getStringExtra("TAX"));
                    params.put("TIP", data.getStringExtra("TIP"));
                    params.put("INSTALLMENT", data.getStringExtra("INSTALLMENT"));
                    params.put("RESULT_CODE", data.getStringExtra("RESULT_CODE"));
                    params.put("RESULT_MSG", data.getStringExtra("RESULT_MSG"));
                    params.put("APPROVAL_NUM", data.getStringExtra("APPROVAL_NUM"));
                    params.put("RIDER_NM", data.getStringExtra("RIDER_NM"));
                    params.put("APPROVAL_DATE", data.getStringExtra("APPROVAL_DATE"));
                    params.put("ACQUIRER_CODE", data.getStringExtra("ACQUIRER_CODE"));
                    params.put("ACQUIRER_NAME", data.getStringExtra("ACQUIRER_NAME"));
                    params.put("MERCHANT_NUM", data.getStringExtra("MERCHANT_NUM"));
                    params.put("SHOP_TID", data.getStringExtra("SHOP_TID"));
                    params.put("SHOP_BIZ_NUM", data.getStringExtra("SHOP_BIZ_NUM"));
                    params.put("SHOP_NAME", data.getStringExtra("SHOP_NAME"));
                    params.put("SHOP_OWNER", data.getStringExtra("SHOP_OWNER"));
                    params.put("SHOP_ADDRESS", data.getStringExtra("SHOP_ADDRESS"));
                    params.put("SHOP_TEL", data.getStringExtra("SHOP_TEL"));
                    params.put("ADD_FIELD", data.getStringExtra("ADD_FIELD"));
                    params.put("TRAN_SERIALNO", data.getStringExtra("TRAN_SERIALNO"));
                    params.put("APPCALL_TRAN_NO", data.getStringExtra("APPCALL_TRAN_NO"));
                    params.put("RESPONSE_TYPE", data.getStringExtra("RESPONSE_TYPE"));
                    params.put("TPK", data.getStringExtra("TPK"));
                    params.put("TMK", data.getStringExtra("TMK"));

                    asyncHttpClient.get(Store.store_url + "/fnInsert_card_return_info.do", params, response);
                }
            }
        }
    }
    public class Response_insert_card_return extends AsyncHttpResponseHandler {
        LinearLayout layout_progress;
        Response_insert_card_return (LinearLayout layout_progress) {
            this.layout_progress = layout_progress;
        }
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String content = new String(responseBody);
            try {
                JSONObject json = new JSONObject(content);
                String rt = "";
                rt = json.getString("rt");
                if (rt.equals("YES")) {
                    layout_progress.setVisibility(View.GONE);
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(MainActivity.this, "서버 연결 실패/ 관리자님께 문의해주세요", Toast.LENGTH_LONG).show();
            alertDialog.cancel();
        }
    }
    public class Response_card_return extends AsyncHttpResponseHandler {

        // 통신시작시, 자동호출
        @Override
        public void onStart() {
        }
        // 통신종료시, 자동호출
        @Override
        public void onFinish() {

        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            String content = new String(responseBody);
            try {
                JSONObject json = new JSONObject(content);
                String rt = "";
                rt = json.getString("rt");
                int row = 0;
                if (json.has("row")) row = json.getInt("row");
                if (rt.equals("YES")) {
                    JSONArray item = json.getJSONArray("item");
                    for (int i = 0; i < item.length(); i++) {
                        JSONObject jsonObject = item.getJSONObject(i);
                        Order order22 = new Order();

                        if(jsonObject.has("TOTAL_AMOUNT"))order22.setTotal_amount(jsonObject.getString("TOTAL_AMOUNT"));
                        if(jsonObject.has("TAX"))order22.setTax(jsonObject.getString("TAX"));
                        if(jsonObject.has("TIP"))order22.setTip(jsonObject.getString("TIP"));
                        if(jsonObject.has("INSTALLMENT"))order22.setInstallment(jsonObject.getString("INSTALLMENT"));
                        if(jsonObject.has("APPROVAL_NUM"))order22.setApproval_num(jsonObject.getString("APPROVAL_NUM"));
                        if(jsonObject.has("APPROVAL_DATE"))order22.setApproval_date(jsonObject.getString("APPROVAL_DATE"));
                        if(jsonObject.has("ORDER_NUM"))order22.setOrder_num(jsonObject.getString("ORDER_NUM"));
                        if(jsonObject.has("CUSTOMER_CODE"))order22.setCustomer_code(jsonObject.getString("CUSTOMER_CODE"));
                        if(jsonObject.has("SHOP_TID"))order22.setShop_tid(jsonObject.getString("SHOP_TID"));
                        if(jsonObject.has("SHOP_BIZ_NUM"))order22.setShop_biz_num(jsonObject.getString("SHOP_BIZ_NUM"));
                        if(jsonObject.has("SHOP_NAME"))order22.setShop_name(jsonObject.getString("SHOP_NAME"));
                        if(jsonObject.has("SHOP_OWNER"))order22.setShop_owner(jsonObject.getString("SHOP_OWNER"));
                        if(jsonObject.has("SHOP_ADDRESS"))order22.setShop_address(jsonObject.getString("SHOP_ADDRESS"));
                        if(jsonObject.has("SHOP_TEL"))order22.setShop_tel(jsonObject.getString("SHOP_TEL"));
                        if(jsonObject.has("CARD_NAME"))order22.setCard_name(jsonObject.getString("CARD_NAME"));
                        if(jsonObject.has("CARD_NUM"))order22.setCard_num(jsonObject.getString("CARD_NUM"));
                        if(jsonObject.has("TRAN_TYPE"))order22.setTran_type(jsonObject.getString("TRAN_TYPE"));

                        list_card_return.add(order22);
                    }
                    fn_card_cancel();
                } else {
                    Toast.makeText(MainActivity.this, "당일 결제내역이 없습니다.", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(MainActivity.this, "서버 연결 실패", Toast.LENGTH_LONG).show();
        }
    }
    private void fn_card_cancel() {
        if(list_card_return.size() > 0) {
            String[] items = new String [list_card_return.size()];

            for(int i = 0; i < list_card_return.size(); i++){
                String b= "";
                if(list_card_return.get(i).getTran_type().equals("credit")) {
                    b= "승인";
                } else {
                    b = "취소";
                }
                String c = String .valueOf(i + 1);
                String a = c + ". " + list_card_return.get(i).getCard_name() + b
                        + String.format("(%,d원)",Integer.parseInt(list_card_return.get(i).getTotal_amount()))
                        + " 카드앞6자리 : " + list_card_return.get(i).getCard_num();
                items[i] = a;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setItems(items, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int item) {



                    SimpleDateFormat dt = new SimpleDateFormat("yyMMddHHmmss");
                    Date date = new Date();
                    String now = dt.format(date);

                    ComponentName compName = new ComponentName("kr.co.kicc.ecm","kr.co.kicc.ecm.SmartCcmsMain");

                    Intent intent = new Intent(Intent.ACTION_MAIN);

                    intent.putExtra("TRAN_NO", "1");
                    intent.putExtra("APPCALL_TRAN_NO", now);
                    intent.putExtra("TRAN_TYPE", "credit_cancel");
                    intent.putExtra("TOTAL_AMOUNT", list_card_return.get(item).getTotal_amount());
                    intent.putExtra("TAX",list_card_return.get(item).getTax());
                    intent.putExtra("TIP",list_card_return.get(item).getTip());
                    intent.putExtra("INSTALLMENT",list_card_return.get(item).getInstallment());
                    intent.putExtra("APPROVAL_NUM",list_card_return.get(item).getApproval_num());
                    intent.putExtra("APPROVAL_DATE",list_card_return.get(item).getApproval_date());
                    intent.putExtra("ORDER_NUM",list_card_return.get(item).getOrder_num());
                    intent.putExtra("CUSTOMER_CODE",list_card_return.get(item).getCustomer_code());
                    intent.putExtra("SHOP_TID", list_card_return.get(item).getShop_tid());
                    intent.putExtra("SHOP_BIZ_NUM",list_card_return.get(item).getShop_biz_num());
                    intent.putExtra("SHOP_NAME",list_card_return.get(item).getShop_name());
                    intent.putExtra("SHOP_OWNER",list_card_return.get(item).getShop_owner());
                    intent.putExtra("SHOP_ADDRESS",list_card_return.get(item).getShop_address());
                    intent.putExtra("SHOP_TEL",list_card_return.get(item).getShop_tel());

                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(compName);

                    startActivityForResult(intent, 2);
                }

            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Toast.makeText(MainActivity.this,"결제정보가 없습니다.",Toast.LENGTH_SHORT).show();
        }
    }

    // 뒤로가기시 들어오는 함수
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            // 이 구문의 의미는 슬라이딩 메뉴바를 클릭했을때 메뉴를 닫을 것인가 아닌가를 구분하는 구문인거 같다.
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("프로그램 종료");
            alertDialogBuilder.setMessage("업무를 종료하시겠습니까?")
                    .setNeutralButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(android.os.Build.VERSION.SDK_INT >= 29) {
                                File savefile = new File(LoginActivity.file_dir_login_ing);
                                try{
                                    FileOutputStream fos = new FileOutputStream(savefile);
                                    fos.write("0".getBytes());
                                    fos.close();
                                } catch(IOException e){}
                            } else {
                                boolean result1 = ObjectInOut.getInstance().write(LoginActivity.file_dir_login_ing, "0");
                            }

                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            notificationManager.cancelAll();
                            activity.finishAffinity();
                            System.exit(0);
                        }
                    })
                    .setCancelable(false); // 백버튼으로 팝업창이 닫히지 않도록 한다.

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
    public static void save_setting(){
        String setting = String.format("%d/%d/%f/%d/%b/%b/%b/%d/%b/%b/%s/%s/%b/%b/%s/%s/%s"  //17
                ,Menu5.select_sound, Menu5.set_progress, Menu5.set_volume, Menu5.textsize
                , ForegroundService.sound, ForegroundService.vibrate, Menu5.display_onoff, Menu5.select_map
                , Menu5.addr_tot, Menu5.call_touch, Store.group_pk, Store.log_id, true, true,Store.msg_pk_mm,"1","1");

        if(android.os.Build.VERSION.SDK_INT >= 29) {
            File savefile = new File(LoginActivity.file_dir_setting);
            try{
                FileOutputStream fos = new FileOutputStream(savefile);
                fos.write(setting.getBytes());
                fos.close();
            } catch(IOException e){}
        } else {
            boolean result1 = ObjectInOut.getInstance().write(LoginActivity.file_dir_setting, setting);
        }
    }
    public static void process_stop(Context context) {
        for(int i = 0; i < ForegroundService.list_dialog.size(); i++) {
            ForegroundService.list_dialog.get(i).cancel();
            ForegroundService.list_dialog.remove(i);
        }
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    public static Boolean isLaunchingService_fore(Context mContext){ // 실행중인 서비스가 있는지 확인하는 함수
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ForegroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return  false;
    }

    public class Notice_thread extends Thread {

        @Override
        public void run() {
            while (true) {
                String [] memo_array = Store.notice.split("\n");
                try {
                    if(memo_array.length > 0)
                    {
                        if(memo_array.length == 1)
                        {
                            if(memo_array[0].equals(""))
                            {
                                notice_memo = "";
                                handler.sendEmptyMessage(1);
                                Thread.sleep(10000);
                            }
                        }
                        else
                        {
                            handler.sendEmptyMessage(0);

                            for(int i = 0; i < memo_array.length; i++)
                            {
                                notice_memo = memo_array[i];

                                handler.sendEmptyMessage(1);

                                Thread.sleep(3000);
                            }
                        }
                    }
                    else
                    {
                        notice_memo = "";
                        handler.sendEmptyMessage(1);
                        Thread.sleep(10000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) { //만약 메소드에서 보낸 메세지의 내용이 0이면 아래 코드를 수행 합니다.
                    textView_notice.setVisibility(View.VISIBLE);
                } else if(msg.what == 1) { //만약 메소드에서 보낸 메세지의 내용이 0이면 아래 코드를 수행 합니다.
                    textView_notice.setText(notice_memo);
                }
            }
        };
    }
}
