package com.mahadi.gesturelauncher;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.provider.Settings;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.EditText;
import android.widget.Toast;
import com.example.R;

public class NavbarAccessibilityService extends AccessibilityService {

    private static final String PREFS_NAME = "GestureLauncherPrefs";
    private static final String ACTION_CONFIG_CHANGED = "com.mahadi.gesturelauncher.CONFIG_CHANGED";

    private SharedPreferences sharedPreferences;

    // Master state configurations
    private boolean bottomEnabled = true;
    private boolean topEnabled = true;

    // Geometry thresholds
    private int bottomThresholdTop = 20;
    private int bottomThresholdLeft = 0;
    private int bottomThresholdRight = 0;

    private int topThresholdHeight = 20;
    private int topThresholdLeft = 0;
    private int topThresholdRight = 0;

    // Isolated Bottom Delays
    private int bottomDelayLongPress = 500;
    private int bottomDelaySingleTap = 250;
    private int bottomDelayDoubleTapWindow = 300;

    // Isolated Top Delays
    private int topDelayLongPress = 500;
    private int topDelaySingleTap = 250;
    private int topDelayDoubleTapWindow = 300;

    // Action strings
    private String bottomActionSingleTap = "none_disabled";
    private String bottomActionDoubleTap = "screenshot";
    private String bottomActionLongPress = "launch_app";
    private String bottomActionSwipeLeft = "volume_down";
    private String bottomActionSwipeRight = "volume_up";

    private String topActionSingleTap = "none_disabled";
    private String topActionDoubleTap = "notifications";
    private String topActionLongPress = "none_disabled";
    private String topActionSwipeLeft = "brightness_down";
    private String topActionSwipeRight = "brightness_up";

    // Target packages
    private String bottomPackageSingleTap = "com.android.chrome";
    private String bottomPackageDoubleTap = "com.android.chrome";
    private String bottomPackageLongPress = "com.android.chrome";

    private String topPackageSingleTap = "com.android.chrome";
    private String topPackageDoubleTap = "com.android.chrome";
    private String topPackageLongPress = "com.android.chrome";

    private int screenWidth = 0;
    private int screenHeight = 0;
    private float touchSlop = 0;

    // Touch Machine State variables
    private String touchedZone = "none";
    private float downX = 0;
    private float downY = 0;
    private long downTime = 0;
    private long lastUpTime = 0;
    private boolean isGesturePending = false;
    private boolean isLongPressTriggered = false;
    private String lastTappedZone = "none";

    private WindowManager windowManager;
    private View bottomOverlayView;
    private View topOverlayView;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (!"none".equals(touchedZone)) {
                isLongPressTriggered = true;
                performLongPressFeedback(touchedZone);
                triggerGestureAction(touchedZone, "long_press");
            }
        }
    };

    private String singleTapPendingZone = "none";
    private final Runnable singleTapRunnable = new Runnable() {
        @Override
        public void run() {
            if (!"none".equals(singleTapPendingZone)) {
                triggerGestureAction(singleTapPendingZone, "single_tap");
                singleTapPendingZone = "none";
                lastUpTime = 0;
                lastTappedZone = "none";
            }
        }
    };

    private final BroadcastReceiver configReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CONFIG_CHANGED.equals(intent.getAction())) {
                loadConfiguration();
                updateOverlayViews();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadConfiguration();

        IntentFilter filter = new IntentFilter(ACTION_CONFIG_CHANGED);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            registerReceiver(configReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(configReceiver, filter);
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        updateScreenDimensions();
        ViewConfiguration vc = ViewConfiguration.get(this);
        touchSlop = vc.getScaledTouchSlop();

        setupOverlayWindow();
    }

    private void updateScreenDimensions() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;
    }

    private void loadConfiguration() {
        if (sharedPreferences != null) {
            boolean masterEnabled = sharedPreferences.getBoolean("master_enabled", true);
            bottomEnabled = masterEnabled && sharedPreferences.getBoolean("bottom_enabled", true);
            topEnabled = masterEnabled && sharedPreferences.getBoolean("top_enabled", true);

            bottomThresholdTop = sharedPreferences.getInt("bottom_threshold_top", 20);
            bottomThresholdLeft = sharedPreferences.getInt("bottom_threshold_left", 0);
            bottomThresholdRight = sharedPreferences.getInt("bottom_threshold_right", 0);

            topThresholdHeight = sharedPreferences.getInt("top_threshold_height", 20);
            topThresholdLeft = sharedPreferences.getInt("top_threshold_left", 0);
            topThresholdRight = sharedPreferences.getInt("top_threshold_right", 0);

            // Separate bottom delays load
            bottomDelaySingleTap = sharedPreferences.getInt("bottom_delay_single_tap", 250);
            bottomDelayDoubleTapWindow = sharedPreferences.getInt("bottom_delay_double_tap_window", 300);
            bottomDelayLongPress = sharedPreferences.getInt("bottom_delay_long_press", 500);

            // Separate top delays load
            topDelaySingleTap = sharedPreferences.getInt("top_delay_single_tap", 250);
            topDelayDoubleTapWindow = sharedPreferences.getInt("top_delay_double_tap_window", 300);
            topDelayLongPress = sharedPreferences.getInt("top_delay_long_press", 500);

            bottomActionSingleTap = sharedPreferences.getString("bottom_action_single_tap", "none_disabled");
            bottomActionDoubleTap = sharedPreferences.getString("bottom_action_double_tap", "screenshot");
            bottomActionLongPress = sharedPreferences.getString("bottom_action_long_press", "launch_app");
            bottomActionSwipeLeft = sharedPreferences.getString("bottom_action_swipe_left", "volume_down");
            bottomActionSwipeRight = sharedPreferences.getString("bottom_action_swipe_right", "volume_up");

            topActionSingleTap = sharedPreferences.getString("top_action_single_tap", "none_disabled");
            topActionDoubleTap = sharedPreferences.getString("top_action_double_tap", "notifications");
            topActionLongPress = sharedPreferences.getString("top_action_long_press", "none_disabled");
            topActionSwipeLeft = sharedPreferences.getString("top_action_swipe_left", "brightness_down");
            topActionSwipeRight = sharedPreferences.getString("top_action_swipe_right", "brightness_up");

            String fallback = "com.android.chrome";
            bottomPackageSingleTap = sharedPreferences.getString("bottom_package_single_tap", fallback);
            bottomPackageDoubleTap = sharedPreferences.getString("bottom_package_double_tap", fallback);
            bottomPackageLongPress = sharedPreferences.getString("bottom_package_long_press", fallback);

            topPackageSingleTap = sharedPreferences.getString("top_package_single_tap", fallback);
            topPackageDoubleTap = sharedPreferences.getString("top_package_double_tap", fallback);
            topPackageLongPress = sharedPreferences.getString("top_package_long_press", fallback);
        }
    }

    private void setupOverlayWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager == null) return;

        removeOverlaysIfAttached();

        bottomOverlayView = new View(this);
        bottomOverlayView.setOnTouchListener((v, event) -> onOverlayTouchEvent("bottom", event));

        topOverlayView = new View(this);
        topOverlayView.setOnTouchListener((v, event) -> onOverlayTouchEvent("top", event));

        attachOverlays();
    }

    private void attachOverlays() {
        if (windowManager == null) return;

        try {
            WindowManager.LayoutParams bottomParams = getOverlayParams("bottom");
            windowManager.addView(bottomOverlayView, bottomParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            WindowManager.LayoutParams topParams = getOverlayParams("top");
            windowManager.addView(topOverlayView, topParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeOverlaysIfAttached() {
        if (windowManager == null) return;

        if (bottomOverlayView != null && bottomOverlayView.isAttachedToWindow()) {
            try {
                windowManager.removeView(bottomOverlayView);
            } catch (Exception ignored) {}
        }
        if (topOverlayView != null && topOverlayView.isAttachedToWindow()) {
            try {
                windowManager.removeView(topOverlayView);
            } catch (Exception ignored) {}
        }
    }

    private WindowManager.LayoutParams getOverlayParams(String zone) {
        boolean enabled;
        int heightProgress;
        int leftPercent;
        int rightPercent;
        int gravity;

        if ("bottom".equals(zone)) {
            enabled = bottomEnabled;
            heightProgress = bottomThresholdTop;
            leftPercent = bottomThresholdLeft;
            rightPercent = bottomThresholdRight;
            gravity = android.view.Gravity.BOTTOM | android.view.Gravity.LEFT;
        } else {
            enabled = topEnabled;
            heightProgress = topThresholdHeight;
            leftPercent = topThresholdLeft;
            rightPercent = topThresholdRight;
            gravity = android.view.Gravity.TOP | android.view.Gravity.LEFT;
        }

        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        int width;
        int height;
        int x;

        if (!enabled) {
            width = 1;
            height = 1;
            x = 0;
            flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        } else {
            float heightPercent = heightProgress / 10.0f;
            height = (int) (screenHeight * (heightPercent / 100.0f));
            if (height < 30) height = 30;

            int leftMargin = (int) (screenWidth * (leftPercent / 100.0f));
            int rightMargin = (int) (screenWidth * (rightPercent / 100.0f));
            width = screenWidth - leftMargin - rightMargin;
            x = leftMargin;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                flags,
                android.graphics.PixelFormat.TRANSLUCENT
        );
        params.gravity = gravity;
        params.x = x;
        params.y = 0;

        return params;
    }

    private void updateOverlayViews() {
        if (windowManager == null) return;
        if (screenWidth <= 0 || screenHeight <= 0) {
            updateScreenDimensions();
        }

        if (bottomOverlayView != null && bottomOverlayView.isAttachedToWindow()) {
            try {
                WindowManager.LayoutParams params = getOverlayParams("bottom");
                windowManager.updateViewLayout(bottomOverlayView, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (topOverlayView != null && topOverlayView.isAttachedToWindow()) {
            try {
                WindowManager.LayoutParams params = getOverlayParams("top");
                windowManager.updateViewLayout(topOverlayView, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean onOverlayTouchEvent(final String zone, MotionEvent event) {
        if (screenHeight <= 0 || screenWidth <= 0) {
            updateScreenDimensions();
        }

        // Zone-level master bypass instantly ignoring Touch Calculations if zone is disabled
        if ("bottom".equals(zone) && !bottomEnabled) {
            cancelPendingGesture();
            return false;
        }
        if ("top".equals(zone) && !topEnabled) {
            cancelPendingGesture();
            return false;
        }

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchedZone = zone;
                triggerGlow(zone);
                downX = x;
                downY = y;
                downTime = System.currentTimeMillis();
                isGesturePending = true;
                isLongPressTriggered = false;

                // Post custom Delay long press task only if valid gesture routing doesn't resolve 'None (Disabled)'
                String lpAction = "bottom".equals(zone) ? bottomActionLongPress : topActionLongPress;
                int currentLpDelay = "bottom".equals(zone) ? bottomDelayLongPress : topDelayLongPress;
                if (!"none_disabled".equals(lpAction)) {
                    handler.removeCallbacks(longPressRunnable);
                    handler.postDelayed(longPressRunnable, currentLpDelay);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isGesturePending && zone.equals(touchedZone)) {
                    float moveDeltaX = Math.abs(x - downX);
                    float moveDeltaY = Math.abs(y - downY);

                    if (moveDeltaX > touchSlop * 2 || moveDeltaY > touchSlop * 2) {
                        handler.removeCallbacks(longPressRunnable);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                handler.removeCallbacks(longPressRunnable);
                if (isGesturePending && zone.equals(touchedZone)) {
                    isGesturePending = false;

                    if (isLongPressTriggered) {
                        touchedZone = "none";
                        break;
                    }

                    long now = System.currentTimeMillis();
                    float deltaX = x - downX;
                    float deltaY = y - downY;
                    long duration = now - downTime;

                    int currentSingleTapDelay = "bottom".equals(touchedZone) ? bottomDelaySingleTap : topDelaySingleTap;
                    int currentDoubleTapWindow = "bottom".equals(touchedZone) ? bottomDelayDoubleTapWindow : topDelayDoubleTapWindow;
                    int currentLongPressDelay = "bottom".equals(touchedZone) ? bottomDelayLongPress : topDelayLongPress;

                    // Swipe Intercept checks (100px delta threshold dominantly raw direction)
                    if (Math.abs(deltaX) >= 100f && Math.abs(deltaX) > Math.abs(deltaY)) {
                        if (deltaX > 0) {
                            triggerGestureAction(touchedZone, "swipe_right");
                        } else {
                            triggerGestureAction(touchedZone, "swipe_left");
                        }
                        lastUpTime = 0;
                        lastTappedZone = "none";
                    } else if (Math.abs(deltaY) >= 100f && Math.abs(deltaY) > Math.abs(deltaX) && "top".equals(touchedZone) && deltaY > 0) {
                        // Swipe-down shade pulldown bypass safety replication
                        performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
                        lastUpTime = 0;
                        lastTappedZone = "none";
                    } else if (duration < currentLongPressDelay) {
                        // Double Tap timing validation and routing
                        if (now - lastUpTime <= currentDoubleTapWindow && touchedZone.equals(lastTappedZone)) {
                            handler.removeCallbacks(singleTapRunnable);
                            singleTapPendingZone = "none";
                            triggerGestureAction(touchedZone, "double_tap");
                            lastUpTime = 0;
                            lastTappedZone = "none";
                        } else {
                            // Enqueue Single Tap with isolated intercept delay
                            lastUpTime = now;
                            lastTappedZone = touchedZone;
                            handler.removeCallbacks(singleTapRunnable);
                            singleTapPendingZone = touchedZone;
                            handler.postDelayed(singleTapRunnable, currentSingleTapDelay);
                        }
                    }
                }
                touchedZone = "none";
                break;

            case MotionEvent.ACTION_CANCEL:
                cancelPendingGesture();
                break;
        }

        return false;
    }

    private void cancelPendingGesture() {
        if (isGesturePending) {
            handler.removeCallbacks(longPressRunnable);
            handler.removeCallbacks(singleTapRunnable);
            isGesturePending = false;
            isLongPressTriggered = false;
            touchedZone = "none";
            singleTapPendingZone = "none";
        }
    }

    private void triggerGestureAction(String zone, String gesture) {
        String actionToExecute = "none_disabled";
        String targetPkg = "";

        if ("bottom".equals(zone)) {
            if ("single_tap".equals(gesture)) {
                actionToExecute = bottomActionSingleTap;
                targetPkg = bottomPackageSingleTap;
            } else if ("double_tap".equals(gesture)) {
                actionToExecute = bottomActionDoubleTap;
                targetPkg = bottomPackageDoubleTap;
            } else if ("long_press".equals(gesture)) {
                actionToExecute = bottomActionLongPress;
                targetPkg = bottomPackageLongPress;
            } else if ("swipe_left".equals(gesture)) {
                actionToExecute = bottomActionSwipeLeft;
            } else if ("swipe_right".equals(gesture)) {
                actionToExecute = bottomActionSwipeRight;
            }
        } else if ("top".equals(zone)) {
            if ("single_tap".equals(gesture)) {
                actionToExecute = topActionSingleTap;
                targetPkg = topPackageSingleTap;
            } else if ("double_tap".equals(gesture)) {
                actionToExecute = topActionDoubleTap;
                targetPkg = topPackageDoubleTap;
            } else if ("long_press".equals(gesture)) {
                actionToExecute = topActionLongPress;
                targetPkg = topPackageLongPress;
            } else if ("swipe_left".equals(gesture)) {
                actionToExecute = topActionSwipeLeft;
            } else if ("swipe_right".equals(gesture)) {
                actionToExecute = topActionSwipeRight;
            }
        }

        if ("none_disabled".equals(actionToExecute)) {
            return;
        }

        if ("launch_app".equals(actionToExecute)) {
            launchTargetApp(targetPkg);
        } else if ("screenshot".equals(actionToExecute)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);
            } else {
                showToastOnMain("Screenshot requires Android 9 Pie or higher");
            }
        } else if ("notifications".equals(actionToExecute)) {
            performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
        } else if ("lock_screen".equals(actionToExecute)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
            } else {
                showToastOnMain("Lock Screen requires Android 9 Pie or higher");
            }
        } else if ("volume_up".equals(actionToExecute)) {
            adjustSystemVolume(true);
        } else if ("volume_down".equals(actionToExecute)) {
            adjustSystemVolume(false);
        } else if ("brightness_up".equals(actionToExecute)) {
            adjustSystemBrightness(true);
        } else if ("brightness_down".equals(actionToExecute)) {
            adjustSystemBrightness(false);
        } else if ("screen_rotation_toggle".equals(actionToExecute)) {
            toggleScreenRotation();
        } else if ("quick_notes".equals(actionToExecute)) {
            handler.post(this::showQuickNotesPopup);
        }
    }

    private void launchTargetApp(final String pkg) {
        if (pkg == null || pkg.trim().isEmpty()) {
            showToastOnMain("Target application was not configured");
            return;
        }
        handler.post(() -> {
            try {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pkg);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launchIntent);
                } else {
                    Toast.makeText(NavbarAccessibilityService.this, "Launch intent not found for: " + pkg, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(NavbarAccessibilityService.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adjustSystemVolume(boolean increase) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                increase ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            );
        }
    }

    private void adjustSystemBrightness(boolean increase) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)) {
                try {
                    int currentBr = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                    int targetBr = increase ? Math.min(255, currentBr + 30) : Math.max(10, currentBr - 30);
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, targetBr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                showToastOnMain("Write settings permission missing - brightness action skipped");
            }
        }
    }

    private void showToastOnMain(final String message) {
        handler.post(() -> Toast.makeText(NavbarAccessibilityService.this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {
        cancelPendingGesture();
    }

    @Override
    public void onDestroy() {
        cancelPendingGesture();
        try {
            unregisterReceiver(configReceiver);
        } catch (Exception ignored) {}
        hideQuickNotesPopup();
        removeOverlaysIfAttached();
        super.onDestroy();
    }

    private void triggerGlow(final String zone) {
        final View v = "bottom".equals(zone) ? bottomOverlayView : topOverlayView;
        if (v == null) return;

        int primaryColor;
        try {
            android.util.TypedValue typedValue = new android.util.TypedValue();
            int attrId = getResources().getIdentifier("colorPrimary", "attr", getPackageName());
            if (attrId != 0) {
                getTheme().resolveAttribute(attrId, typedValue, true);
                primaryColor = typedValue.data;
            } else {
                primaryColor = 0xFF4F46E5;
            }
        } catch (Exception e) {
            primaryColor = 0xFF4F46E5;
        }

        final int baseColor = primaryColor & 0x00FFFFFF;
        int startColor = baseColor | 0x66000000;
        int endColor = baseColor | 0x00000000;

        final GradientDrawable gd = new GradientDrawable(
            "bottom".equals(zone) ? GradientDrawable.Orientation.BOTTOM_TOP : GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{startColor, endColor}
        );
        gd.setShape(GradientDrawable.RECTANGLE);
        v.setBackground(gd);

        final long duration = 250;
        final long startTime = System.currentTimeMillis();

        final Runnable fadeRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= duration) {
                    v.setBackground(null);
                } else {
                    float progress = 1.0f - ((float) elapsed / duration);
                    int currentAlpha = (int) (progress * 130);
                    gd.setAlpha(currentAlpha);
                    v.postInvalidate();
                    handler.postDelayed(this, 16);
                }
            }
        };
        v.post(fadeRunnable);
    }

    private void toggleScreenRotation() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)) {
                try {
                    int currentRotation = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                    int targetRotation = (currentRotation == 1) ? 0 : 1;
                    Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, targetRotation);
                    showToastOnMain(targetRotation == 1 ? "Auto-Rotate Activated" : "Rotation Locked");
                } catch (Exception e) {
                    showToastOnMain("Error toggling rotation: " + e.getMessage());
                }
            } else {
                showToastOnMain("Write settings permission missing - Rotation toggle skipped");
            }
        } else {
            try {
                int currentRotation = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                int targetRotation = (currentRotation == 1) ? 0 : 1;
                Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, targetRotation);
                showToastOnMain(targetRotation == 1 ? "Auto-Rotate Activated" : "Rotation Locked");
            } catch (Exception e) {
                showToastOnMain("Error: " + e.getMessage());
            }
        }
    }

    private View notesPopupView = null;
    private WindowManager.LayoutParams quickNotesParams = null;
    private boolean isQuickNotesMinimized = false;

    private void showQuickNotesPopup() {
        if (notesPopupView != null && notesPopupView.isAttachedToWindow()) {
            return;
        }

        final Context context = new android.view.ContextThemeWrapper(this, com.example.R.style.Theme_MyApplication);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    showToastOnMain("Enable Overlay Permission to write Quick Notes");
                } catch (Exception e) {
                    showToastOnMain("Overlay Permission required!");
                }
                return;
            }
        }

        try {
            notesPopupView = android.view.LayoutInflater.from(context).inflate(com.example.R.layout.floating_quick_notes, null);
        } catch (Exception e) {
            e.printStackTrace();
            showToastOnMain("Failed to inflate popup: " + e.getMessage());
            return;
        }

        notesPopupView.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_OUTSIDE) {
                hideQuickNotesPopup();
                return true;
            }
            return false;
        });

        final View expandedCard = notesPopupView.findViewById(com.example.R.id.notes_expanded_card);
        final View minimizedCard = notesPopupView.findViewById(com.example.R.id.notes_minimized_card);
        final EditText etNote = notesPopupView.findViewById(com.example.R.id.et_quick_note);
        final View btnMinimize = notesPopupView.findViewById(com.example.R.id.btn_minimize_note);
        final View btnClose = notesPopupView.findViewById(com.example.R.id.btn_close_note);

        // Ensure key intercepts so Back key doesn't block but hides popup
        notesPopupView.setFocusableInTouchMode(true);
        notesPopupView.requestFocus();
        notesPopupView.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.getAction() == android.view.KeyEvent.ACTION_UP) {
                hideQuickNotesPopup();
                return true;
            }
            return false;
        });

        // Sync text from SharedPreferences
        String savedNote = sharedPreferences.getString("quick_note_text", "");
        if (etNote != null) {
            etNote.setText(savedNote);
            etNote.setSelection(savedNote.length());

            // Selection & Cursor Focus Controls
            etNote.setTextIsSelectable(true);
            etNote.setFocusable(true);
            etNote.setFocusableInTouchMode(true);
            etNote.setCursorVisible(true);
            etNote.setClickable(true);

            etNote.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.getAction() == android.view.KeyEvent.ACTION_UP) {
                    hideQuickNotesPopup();
                    return true;
                }
                return false;
            });

            etNote.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    sharedPreferences.edit().putString("quick_note_text", s.toString()).apply();
                }
                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });

            // Clipboard Ribbon actions
            final View btnSelectAll = notesPopupView.findViewById(com.example.R.id.btn_note_select_all);
            if (btnSelectAll != null) {
                btnSelectAll.setOnClickListener(v -> {
                    etNote.requestFocus();
                    etNote.selectAll();
                });
            }

            final View btnCopy = notesPopupView.findViewById(com.example.R.id.btn_note_copy);
            if (btnCopy != null) {
                btnCopy.setOnClickListener(v -> {
                    int start = etNote.getSelectionStart();
                    int end = etNote.getSelectionEnd();
                    String textToCopy;
                    if (start >= 0 && end >= 0 && start != end) {
                        textToCopy = etNote.getText().toString().substring(Math.min(start, end), Math.max(start, end));
                    } else {
                        textToCopy = etNote.getText().toString();
                        etNote.selectAll();
                    }
                    if (!textToCopy.isEmpty()) {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("quick_note", textToCopy);
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                            showToastOnMain("Copied to clipboard");
                        }
                    }
                });
            }

            final View btnCut = notesPopupView.findViewById(com.example.R.id.btn_note_cut);
            if (btnCut != null) {
                btnCut.setOnClickListener(v -> {
                    int start = etNote.getSelectionStart();
                    int end = etNote.getSelectionEnd();
                    if (start >= 0 && end >= 0 && start != end) {
                        int min = Math.min(start, end);
                        int max = Math.max(start, end);
                        String textToCut = etNote.getText().toString().substring(min, max);
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("quick_note", textToCut);
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                        }
                        etNote.getText().delete(min, max);
                        showToastOnMain("Cut to clipboard");
                    } else {
                        String textToCut = etNote.getText().toString();
                        if (!textToCut.isEmpty()) {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("quick_note", textToCut);
                            if (clipboard != null) {
                                clipboard.setPrimaryClip(clip);
                            }
                            etNote.setText("");
                            showToastOnMain("Cut entire note");
                        }
                    }
                });
            }

            final View btnPaste = notesPopupView.findViewById(com.example.R.id.btn_note_paste);
            if (btnPaste != null) {
                btnPaste.setOnClickListener(v -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription() != null) {
                        android.content.ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                        CharSequence pasteText = item.getText();
                        if (pasteText != null) {
                            int start = etNote.getSelectionStart();
                            int end = etNote.getSelectionEnd();
                            if (start >= 0 && end >= 0) {
                                int min = Math.min(start, end);
                                int max = Math.max(start, end);
                                etNote.getText().replace(min, max, pasteText);
                            } else {
                                etNote.append(pasteText);
                            }
                            showToastOnMain("Pasted");
                        }
                    } else {
                        showToastOnMain("Clipboard is empty");
                    }
                });
            }

            final View btnClear = notesPopupView.findViewById(com.example.R.id.btn_note_clear);
            if (btnClear != null) {
                btnClear.setOnClickListener(v -> {
                    etNote.setText("");
                    showToastOnMain("Note cleared");
                });
            }
        }

        isQuickNotesMinimized = false;
        if (expandedCard != null) expandedCard.setVisibility(View.VISIBLE);
        if (minimizedCard != null) minimizedCard.setVisibility(View.GONE);

        // Close action
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> hideQuickNotesPopup());
        }

        final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        int notesWindowType;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notesWindowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            notesWindowType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // Window Layout Parameters - Clears FLAG_NOT_FOCUSABLE to allow selection and typing
        quickNotesParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(360),
                notesWindowType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                android.graphics.PixelFormat.TRANSLUCENT
        );
        quickNotesParams.gravity = android.view.Gravity.CENTER;
        quickNotesParams.x = 0;
        quickNotesParams.y = 0;
        int margin = (int) (0.05f * screenWidth);
        quickNotesParams.width = screenWidth - (margin * 2);

        // Draggable System Integration on the Header Layout
        final View headerLayout = notesPopupView.findViewById(com.example.R.id.quick_notes_header);
        if (headerLayout != null) {
            headerLayout.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (isQuickNotesMinimized) {
                        return false;
                    }
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = quickNotesParams.x;
                            initialY = quickNotesParams.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            v.performClick();
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            float deltaX = event.getRawX() - initialTouchX;
                            float deltaY = event.getRawY() - initialTouchY;
                            quickNotesParams.x = initialX + (int) deltaX;
                            quickNotesParams.y = initialY + (int) deltaY;
                            try {
                                if (wm != null && notesPopupView != null && notesPopupView.isAttachedToWindow()) {
                                    wm.updateViewLayout(notesPopupView, quickNotesParams);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return true;
                    }
                    return false;
                }
            });
        }

        // Minimize action
        if (btnMinimize != null) {
            btnMinimize.setOnClickListener(v -> {
                if (wm == null || notesPopupView == null || !notesPopupView.isAttachedToWindow()) return;
                
                // Persistence Guard: Sync current text
                if (etNote != null) {
                    sharedPreferences.edit().putString("quick_note_text", etNote.getText().toString()).apply();
                }

                isQuickNotesMinimized = true;
                if (expandedCard != null) expandedCard.setVisibility(View.GONE);
                if (minimizedCard != null) minimizedCard.setVisibility(View.VISIBLE);

                // Dynamically shrink LayoutParams
                quickNotesParams.width = dpToPx(56);
                quickNotesParams.height = dpToPx(56);
                quickNotesParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
                quickNotesParams.x = dpToPx(16);
                quickNotesParams.y = screenHeight / 3;
                quickNotesParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

                try {
                    wm.updateViewLayout(notesPopupView, quickNotesParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Restore action (clicking minimized bubble)
        if (minimizedCard != null) {
            minimizedCard.setOnClickListener(v -> {
                if (wm == null || notesPopupView == null || !notesPopupView.isAttachedToWindow()) return;

                // Persistence Guard: Query and sync text smoothly
                if (etNote != null) {
                    String currentSaved = sharedPreferences.getString("quick_note_text", "");
                    etNote.setText(currentSaved);
                    etNote.setSelection(currentSaved.length());
                }

                isQuickNotesMinimized = false;
                if (expandedCard != null) expandedCard.setVisibility(View.VISIBLE);
                if (minimizedCard != null) minimizedCard.setVisibility(View.GONE);

                // Dynamically expand LayoutParams back to custom dialog size
                quickNotesParams.width = screenWidth - (margin * 2);
                quickNotesParams.height = dpToPx(360);
                quickNotesParams.gravity = android.view.Gravity.CENTER;
                quickNotesParams.x = 0;
                quickNotesParams.y = 0;
                quickNotesParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

                try {
                    wm.updateViewLayout(notesPopupView, quickNotesParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        try {
            if (wm != null) {
                wm.addView(notesPopupView, quickNotesParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToastOnMain("WindowManager insertion failed: " + e.getMessage());
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void performLongPressFeedback(String zone) {
        View v = "bottom".equals(zone) ? bottomOverlayView : topOverlayView;
        if (v != null) {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
            triggerLongPressVisualPulse(zone);
        }
    }

    private void triggerLongPressVisualPulse(final String zone) {
        final View v = "bottom".equals(zone) ? bottomOverlayView : topOverlayView;
        if (v == null) return;

        int secondaryColor;
        try {
            android.util.TypedValue typedValue = new android.util.TypedValue();
            int attrId = getResources().getIdentifier("colorSecondary", "attr", getPackageName());
            if (attrId != 0) {
                getTheme().resolveAttribute(attrId, typedValue, true);
                secondaryColor = typedValue.data;
            } else {
                secondaryColor = 0xFF10B981;
            }
        } catch (Exception e) {
            secondaryColor = 0xFF10B981;
        }

        final int baseColor = secondaryColor & 0x00FFFFFF;
        int startColor = baseColor | 0xAA000000;
        int endColor = baseColor | 0x00000000;

        final GradientDrawable gd = new GradientDrawable(
            "bottom".equals(zone) ? GradientDrawable.Orientation.BOTTOM_TOP : GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{startColor, endColor}
        );
        gd.setShape(GradientDrawable.RECTANGLE);
        v.setBackground(gd);

        final long duration = 400;
        final long startTime = System.currentTimeMillis();

        final Runnable pulseRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= duration) {
                    v.setBackground(null);
                } else {
                    float progress = 1.0f - ((float) elapsed / duration);
                    int currentAlpha = (int) (progress * 200);
                    gd.setAlpha(currentAlpha);
                    v.postInvalidate();
                    handler.postDelayed(this, 16);
                }
            }
        };
        v.post(pulseRunnable);
    }

    private void hideQuickNotesPopup() {
        if (notesPopupView != null && notesPopupView.isAttachedToWindow()) {
            try {
                WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                if (wm != null) {
                    wm.removeView(notesPopupView);
                } else if (windowManager != null) {
                    windowManager.removeView(notesPopupView);
                }
            } catch (Exception ignored) {}
            notesPopupView = null;
        }
    }
}
