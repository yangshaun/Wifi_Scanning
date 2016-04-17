package com.example.wifi_scanning;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.android.AndroidAuthSession;

public class MainActivity extends Activity {
    ProgressBar progress;
    Button buttonScan;
    ListView lv;
    ArrayAdapter adapter;
    Spinner spinner;
    TextView timetext;
    Button update;

    boolean mBound = false;
    Date time_now;
    DateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
    int check = 0;
    int period = 60 * 1000 * 3;
    ArrayAdapter<String> options;
    static Intent intent;

    private boolean getService = false;
    private LocationManager lms;

    public static String android_id;
    public static String dropboxname;
    Dropbox_initial dropbox;
    int current_period;
    public Wifi_service wifiservice;

    // ----------------------------------------------

    // ----------------------------------------------
    private BroadcastReceiver receiver;
    private Object[] Wifiary;
    protected Object mService;
    private View topLevelLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        topLevelLayout = findViewById(R.id.top_layout);

        if (isFirstTime()) {
            Log.e("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", "in view1");
            topLevelLayout.setVisibility(View.INVISIBLE);
        }

        Log.e("QWQWQWQWQWQWQW", "onCreate");
        lv = (ListView) findViewById(R.id.wifilist);
        buttonScan = (Button) findViewById(R.id.start);
        update = (Button) findViewById(R.id.update);
        progress = (ProgressBar) findViewById(R.id.progress);
        timetext = (TextView) findViewById(R.id.time);
        spinner = (Spinner) findViewById(R.id.spinner);
        // //////////////////////////////////////////////////////////////////////////////////
        time_now = new Date();
        android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
        timetext.setText("Current Time : " + dateFormat.format(time_now).toString() + "\n" + "Your Device ID :" + android_id);
        String[] options_str = { "3分鐘", "5分鐘", "10分鐘", "15分鐘" };
        options = new ArrayAdapter<String>(this, R.layout.spin_item, options_str);
        spinner.setAdapter(options);
        dropbox = new Dropbox_initial(MainActivity.this);
        wifiservice = new Wifi_service(MainActivity.this);
        // //////////////////////////////////////////////////////////////////////////////////
        dropbox.onCreate();

        // //////////////////////////////////////////////////////////////////////////////////
        // //////////////////////////////////////////////////////////////////////////////////
        if (isMyServiceRunning(Wifi_service.class)) {
            buttonScan.setEnabled(false);
            spinner.setEnabled(false);
            buttonScan.setText("正在掃描中.....");
            Toast.makeText(MainActivity.this, "Service 正在掃描中....", Toast.LENGTH_SHORT).show();
        }

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                // TODO Auto-generated method stub
                switch (position) {
                case 0:
                    period = 1000 * 60 * 3;
                    break;
                case 1:
                    period = 1000 * 60 * 5;
                    break;
                case 2:
                    period = 1000 * 60 * 10;
                    break;
                case 3:
                    period = 1000 * 60 * 15;
                    break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                period = 60 * 1000 * 60;
            }
        });

        buttonScan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences settings = getSharedPreferences("period", 0);
                final SharedPreferences.Editor editor = settings.edit();
                editor.putInt("period", period);
                editor.commit();
                SharedPreferences wifi_num = getSharedPreferences("wifi_num", 2);
                final SharedPreferences.Editor editor1 = wifi_num.edit();
                editor1.putInt("wifi_num", 0);
                editor1.commit();
                LocationManager locationenable = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (!locationenable.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    Toast.makeText(getBaseContext(), "嘿你幫我開一下GPS吧!!!甘溫阿^^", Toast.LENGTH_LONG).show();
                    startActivity(callGPSSettingIntent);
                } else {
                    intent = new Intent(MainActivity.this, wifiservice.getClass());
                    if (isMyServiceRunning(Wifi_service.class)) { // 又有service再跑

                        stopService(intent);
                        buttonScan.setText("開始掃描Wifi");
                    } else {
                        Log.e("QWQWQWQWQWQWQW", "inside!!!!!!!!!!!!!!!!!!!" + period);
                        // wifiservice = new Wifi_service(MainActivity.this,
                        // period);
                        dropbox.setDropbox_initial(wifiservice);

                        startService(intent);
                        buttonScan.setText("正在背景掃描中.....");
                        buttonScan.setEnabled(false);
                        update.setEnabled(false);
                        update.setText("請稍後~~");
                        spinner.setEnabled(false);
                        progress.setVisibility(1);
                        Toast.makeText(MainActivity.this, "已經開始掃描了唷!!", Toast.LENGTH_SHORT).show();
                    }

                    Log.e("QWQWQWQWQWQWQW", "Outside!!!!!!!!!!!!!!!!!!!" + " current : " + current_period + " period : " + period);
                    current_period = period;
                }

            }

        });

        update.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (isMyServiceRunning(Wifi_service.class)) {
                    AndroidAuthSession session = Dropbox_initial.dropbox.getSession();
                    if (session.authenticationSuccessful()) {
                        intent = new Intent(MainActivity.this, wifiservice.getClass());
                        stopService(intent);
                        progress.setVisibility(1);
                        dropbox.onClick(1);
                        buttonScan.setText("資料上傳中...");
                    } else {

                        dropbox.onClick(0);
                    }
                } else {

                    AndroidAuthSession session = Dropbox_initial.dropbox.getSession();
                    if (session.authenticationSuccessful()) {
                        progress.setVisibility(1);
                        dropbox.onClick(1);
                        intent = new Intent(MainActivity.this, wifiservice.getClass());
                        buttonScan.setText("資料上傳中...");
                        spinner.setEnabled(true);
                        buttonScan.setEnabled(true);
                    } else {

                        dropbox.onClick(0);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        SharedPreferences wifi_num = getSharedPreferences("wifi_num", 0);
        SharedPreferences wifi_data = getSharedPreferences("wifi_num", 0);
        Log.e("QWQWQWQWQWQWQW", "onResume");
        if (isMyServiceRunning(Wifi_service.class)) {
            Set<String> mySet = new HashSet<String>();
            mySet.add("No Data Available!!!");
            adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, wifi_data.getStringSet("wifi_data", mySet).toArray());
            adapter.notifyDataSetChanged();
            lv.setAdapter(adapter);
            Toast.makeText(this, "目前已經收集到" + wifi_num.getInt("wifi_num", 0) + "筆資料了!", Toast.LENGTH_SHORT).show();
            dropbox.onResume();

        } else {
            Toast.makeText(this, "目前尚未開始掃描!請按下開始鈕!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.e("QWQWQWQWQWQWQW", "onPause");
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        super.onDestroy();

        buttonScan.setText("停止掃描Wifi");
        Log.e("QWQWQWQWQWQWQW", "onDestroy");
    }

    // ///////////////////////////////////////////////////////////////////////////////////gps

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isFirstTime() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {
            Log.e("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", "in view");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.commit();
            topLevelLayout.setVisibility(View.VISIBLE);
            topLevelLayout.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    topLevelLayout.setVisibility(View.INVISIBLE);
                    return false;
                }
            });

        }
        return ranBefore;

    }
}
