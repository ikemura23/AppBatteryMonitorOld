package com.qa.ikemura.appbatterymonitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LauncherActivity extends AppCompatActivity {

    boolean isRecord = false;
    Handler mHandler = new Handler();
    // タイマー処理用
    private Timer mTimer = null;
    private int scale;
    private int level;
    private String fileName = null;
    static final String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator;

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
                Log.d("LauncherActivity", getString(getMessageId()));
                Snackbar.make(view, getMessageId(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                isRecord = !isRecord;
                changeFabSrc();
                setVisibleStatusText();
                setEnableOrDisableForButton();

                if (isRecord) {
                    startMonitor();
                } else {
                    stopMonitor();
                }

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

    @Override
    protected void onResume() {
        super.onResume();
        ((TextView) findViewById(R.id.activity_main_interval_text)).append(getInterval());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    private void setEnableOrDisableForButton() {
        if (isRecord) {
            findViewById(R.id.activity_main_setting_button).setEnabled(false);
            findViewById(R.id.activity_main_log_button).setEnabled(false);
        }
        else {
            findViewById(R.id.activity_main_setting_button).setEnabled(true);
            findViewById(R.id.activity_main_log_button).setEnabled(true);
        }
    }

    private void setVisibleStatusText() {
        if (isRecord) {
            findViewById(R.id.activity_main_log_status_text).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.activity_main_log_status_text).setVisibility(View.GONE);
        }
    }

    private int getMessageId() {
        return isRecord ? R.string.monitor_end_message : R.string.monitor_start_message;
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
        return "  L: " + String.valueOf(level);
    }

    /**
     * change FAB src
     */
    private void changeFabSrc() {
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

        // マウント状態を確認
        if (isNotMountSDCard()) {
            Toast.makeText(this, "外部ストレージはマウントされてません",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // 外部ストレージの有無を確認
        if (isNotExists()) {
            Toast.makeText(this, "外部ストレージは存在しません", Toast.LENGTH_SHORT).show();
            return;
        }
        // 外部ストレージの状態を確認
        if (isNotWrite()) {
            Toast.makeText(this, "外部ストレージは書き込みできません", Toast.LENGTH_SHORT).show();
            return;
        }
        String intervalString = getInterval();
        int interval = 15;
        try {
            interval = Integer.valueOf(intervalString);
        } catch (Exception e) {
            Log.d("LauncherActivity", e.getMessage());
        }

        // タイマー処理
        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        // ここに処理を書く
                        String FILENAME = mFilePath + getFileName();
                        String strRecordTime = getRecordTime();
                        String batteryLevel = getBatteryLevel();
                        String value = strRecordTime + batteryLevel;

                        // Log.d("LauncherActivity", FILENAME);
                        Log.d("LauncherActivity", value);
                        try {
                            BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME, true));
                            bw.write(value);
                            bw.newLine();
                            bw.close();
                        } catch (Exception e) {
                            Log.e("LauncherActivity", e.getMessage(), e);
                        }
                    }
                });
            }
        }, 0, 1000 * 60 * interval); // 0秒後から, 設定 > intervalで設定した間隔で実行
    }

    private String getInterval() {
        return PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString(Const.interval_key, "15");
    }

    private String getRecordTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS] ", Locale.JAPAN);
        return sdf.format(new Date());
    }

    /**
     * SdCardのマウント状態をチェック
     *
     * @return true:マウントされてる、false:マウントされてない
     */
    private boolean isNotMountSDCard() {
        return !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 外部ストレージ存在チェック
     *
     * @return true:存在する, false:存在しない
     */
    private boolean isNotExists() {
        return !Environment.getExternalStorageDirectory().exists();
    }

    /**
     * 外部ストレージに書き込み可能か？
     *
     * @return true:可能, false:不可能
     */
    private boolean isNotWrite() {
        return !Environment.getExternalStorageDirectory().canWrite();
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
        fileName = f.format(now) + ".log";
        return fileName;
    }

    /**
     * 記録ストップ
     */
    private void stopMonitor() {
        if (mTimer == null) {
            return;
        }
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
}
