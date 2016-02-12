package com.qa.ikemura.appbatterymonitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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

    AppBatteryMonitorService appBatteryMonitorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        appBatteryMonitorService = new AppBatteryMonitorService();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //外部ストレージのチェック
                if (isNotWriteStorage()) {
                    return;
                }
                Log.d(Const.TAG, getString(getMessageId()));
                Snackbar.make(view, getMessageId(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                isRecord = !isRecord;
                //フローティングボタンのアイコン切り替え
                changeFabSrc();
                //ステータス文字の切り替え
                setVisibleStatusText();
                //ボタンを無効にして誤操作を防ぐ
                setEnableOrDisableForButton();

                if (isRecord) {
                    // サービス常駐を開始
                    appBatteryMonitorService.startResident(getApplicationContext());
                } else {
                    // サービス停止
                    appBatteryMonitorService.stopResidentIfActive(getApplicationContext());
                }

            }
        });
        // 設定ボタン
        findViewById(R.id.activity_main_setting_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingActivity();
            }
        });
        // ログ一覧
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
        String text = "Interval:" + getInterval();
        ((TextView) findViewById(R.id.activity_main_interval_text)).setText(text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setEnableOrDisableForButton() {
        if (isRecord) {
            findViewById(R.id.activity_main_setting_button).setEnabled(false);
            findViewById(R.id.activity_main_log_button).setEnabled(false);
        } else {
            findViewById(R.id.activity_main_setting_button).setEnabled(true);
            findViewById(R.id.activity_main_log_button).setEnabled(true);
        }
    }

    private void setVisibleStatusText() {
        if (isRecord) {
            findViewById(R.id.activity_main_log_status_text).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.activity_main_log_status_text).setVisibility(View.GONE);
        }
    }

    private int getMessageId() {
        return isRecord ? R.string.monitor_end_message : R.string.monitor_start_message;
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


    private String getInterval() {
        return PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString(Const.INTERVAL_KEY, "15");
    }

    /**
     * ログ出力可能？
     *
     * @return
     */
    public boolean isNotWriteStorage() {
        if (isNotMountSDCard()) {
            Toast.makeText(this, "SdCardがマウントされていません", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (isNotExists()) {
            Toast.makeText(this, "外部ストレージが存在していません", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (isNotWrite()) {
            Toast.makeText(this, "外部ストレージに書き込み不可能です", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
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
