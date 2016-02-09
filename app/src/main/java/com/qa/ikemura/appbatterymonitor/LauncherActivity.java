package com.qa.ikemura.appbatterymonitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class LauncherActivity extends AppCompatActivity {

    boolean isRecord = false;
    Handler mHandler = new Handler();
    // タイマー処理用
    private Timer mTimer = null;
    private int scale;
    private int level;
    private String fileName = null;

    // 受信機
    public BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                // 電池残量の最大値
                scale = intent.getIntExtra("scale", 0);
                // 電池残量
                level = intent.getIntExtra("level", 0);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRecord = !isRecord;
                setFabSrc();

                if (isRecord) {
                    startMonitor();
                }
                else {
                    stopMonitor();
                }

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        findViewById(R.id.activity_main_setting_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingActivity();
            }
        });

        findViewById(R.id.activity_main_log_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogListActivity();
            }
        });
    }

    /**
     * 残バッテリー取得
     * 
     * @return 残バッテリー文字
     */
    private String getBatteryLevel() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(myReceiver, filter);
        if (batteryStatus == null) {
            return "failed";
        }
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        String batteryInfo = "Level: " + String.valueOf(level) + "%";
        Log.d("LauncherActivity", batteryInfo);
        return String.valueOf(level);
    }

    /**
     * change FAB src
     */
    private void setFabSrc() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        int resource;
        if (isRecord) {
            resource = android.R.drawable.ic_media_pause;
        } else {
            resource = android.R.drawable.ic_media_play;
        }
        fab.setImageResource(resource);
    }

    /**
     * 記録開始
     */
    private void startMonitor() {
        Log.d("LauncherActivity", "start monitor");
        // タイマー処理
        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        // ここに処理を書く
                        String FILENAME = getFileName();
                        String batteryLevel = getBatteryLevel();
                        String value = batteryLevel + "\n";

                        FileOutputStream fos = null;
                        try {
                            fos = openFileOutput(FILENAME, Context.MODE_APPEND);
                            fos.write(value.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            assert fos != null;
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 0, 2000); // 0秒後から2秒間隔で実行
    }

    /**
     * ファイル名取得
     * 
     * @return
     */
    private String getFileName() {
        if (fileName != null) {
            return fileName;
        }
        Date now = new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.getDefault());
        return f.format(now) + ".log";
    }

    /**
     * 記録ストップ
     */
    private void stopMonitor() {
        if (mTimer == null) {
            return;
        }
        Log.d("LauncherActivity", "stop monitor");
        mTimer.cancel();
        fileName = null;
    }

    // start activity ---------------------------------------------------------
    private void startSettingActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startLogListActivity() {
        Intent intent = new Intent(this, LogListActivity.class);
        startActivity(intent);
    }

    // OptionsMenu --------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
