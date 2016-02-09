
package com.qa.ikemura.appbatterymonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

final class PreferenceUtils {

    private static SharedPreferences getDefaultSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static Context getContext() {
        return ApplicationController.getInstance().getApplicationContext();
    }

    public static void saveString(String key, String value) {
        SharedPreferences.Editor editor = getDefaultSharedPreferences(getContext()).edit();
        editor.putString(key, value);
        editor.apply();
    }

    protected static String loadString(String key) {
        return getDefaultSharedPreferences(getContext()).getString(key, null);
    }

    private PreferenceUtils() {
    }
}
