
package com.qa.ikemura.appbatterymonitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class AppBatteryMonitorService extends BasePeriodicService {

    public static BasePeriodicService activeService;

    static final String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator;

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
    protected long getIntervalMS() {
        String intervalString = getInterval();
        int interval = 15;
        try {
            interval = Integer.valueOf(intervalString);
        } catch (Exception e) {
            Log.d(Const.TAG, e.getMessage());
        }
        return 1000 * 60 * interval;
    }

    private String getInterval() {
        return PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString(Const.INTERVAL_KEY, "15");
    }

    @Override
    protected void execTask() {
        activeService = this;

        // ここに処理を書く
        String FILENAME = mFilePath + getFileName();
        String strRecordTime = getRecordTime();
        String batteryLevel = getBatteryLevel();
        String value = strRecordTime + batteryLevel;

        Log.d(Const.TAG, value);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME, true));
            bw.write(value);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            Log.e(Const.TAG, e.getMessage(), e);
        }
        makeNextPlan();
    }

    @Override
    public void makeNextPlan()
    {
        this.scheduleNextTime();
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

    private String getRecordTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS] ", Locale.JAPAN);
        return sdf.format(new Date());
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
        try {
            if (myReceiver != null) {
                unregisterReceiver(myReceiver);
                myReceiver = null;
            }
        } catch (Exception e) {
            Log.d(Const.TAG, e.getMessage());
        }
        return "  L: " + String.valueOf(level);
    }

    /**
     * もし起動していたら，常駐を解除する
     */
    public void stopResidentIfActive(Context context) {
        if (activeService != null)
        {
            activeService.stopResident(context);
        }
    }

}
