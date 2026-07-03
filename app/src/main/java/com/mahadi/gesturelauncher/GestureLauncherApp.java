package com.mahadi.gesturelauncher;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class GestureLauncherApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Dynamic Material You Themes
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
