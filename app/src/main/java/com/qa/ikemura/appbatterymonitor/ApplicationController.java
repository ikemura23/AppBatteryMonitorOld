
package com.qa.ikemura.appbatterymonitor;

import android.app.Application;

public class ApplicationController extends Application {

    private static ApplicationController mApplicationController;

    @Override
    public void onCreate() {
        super.onCreate();

        if (mApplicationController == null) {
            mApplicationController = this;
        }
    }

    public static synchronized ApplicationController getInstance() {
        return mApplicationController;
    }
}
