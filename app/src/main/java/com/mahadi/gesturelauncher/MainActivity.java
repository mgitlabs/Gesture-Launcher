package com.mahadi.gesturelauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "GestureLauncherPrefs";

    // Bottom and Top master status keys
    public static final String KEY_BOTTOM_ENABLED = "bottom_enabled";
    public static final String KEY_TOP_ENABLED = "top_enabled";

    // Bottom threshold geometry keys
    public static final String KEY_BOTTOM_THRESHOLD_TOP = "bottom_threshold_top";
    public static final String KEY_BOTTOM_THRESHOLD_LEFT = "bottom_threshold_left";
    public static final String KEY_BOTTOM_THRESHOLD_RIGHT = "bottom_threshold_right";

    // Top threshold geometry keys
    public static final String KEY_TOP_THRESHOLD_HEIGHT = "top_threshold_height";
    public static final String KEY_TOP_THRESHOLD_LEFT = "top_threshold_left";
    public static final String KEY_TOP_THRESHOLD_RIGHT = "top_threshold_right";

    // Bottom Delay configurations
    public static final String KEY_BOTTOM_DELAY_LONG_PRESS = "bottom_delay_long_press";
    public static final String KEY_BOTTOM_DELAY_SINGLE_TAP = "bottom_delay_single_tap";
    public static final String KEY_BOTTOM_DELAY_DOUBLE_TAP_WINDOW = "bottom_delay_double_tap_window";

    // Top Delay configurations
    public static final String KEY_TOP_DELAY_LONG_PRESS = "top_delay_long_press";
    public static final String KEY_TOP_DELAY_SINGLE_TAP = "top_delay_single_tap";
    public static final String KEY_TOP_DELAY_DOUBLE_TAP_WINDOW = "top_delay_double_tap_window";

    // Bottom Action Keys
    public static final String KEY_BOTTOM_ACTION_SINGLE_TAP = "bottom_action_single_tap";
    public static final String KEY_BOTTOM_ACTION_DOUBLE_TAP = "bottom_action_double_tap";
    public static final String KEY_BOTTOM_ACTION_LONG_PRESS = "bottom_action_long_press";
    public static final String KEY_BOTTOM_ACTION_SWIPE_LEFT = "bottom_action_swipe_left";
    public static final String KEY_BOTTOM_ACTION_SWIPE_RIGHT = "bottom_action_swipe_right";

    // Top Action Keys
    public static final String KEY_TOP_ACTION_SINGLE_TAP = "top_action_single_tap";
    public static final String KEY_TOP_ACTION_DOUBLE_TAP = "top_action_double_tap";
    public static final String KEY_TOP_ACTION_LONG_PRESS = "top_action_long_press";
    public static final String KEY_TOP_ACTION_SWIPE_LEFT = "top_action_swipe_left";
    public static final String KEY_TOP_ACTION_SWIPE_RIGHT = "top_action_swipe_right";

    // Bottom Target App Package keys
    public static final String KEY_BOTTOM_PACKAGE_SINGLE_TAP = "bottom_package_single_tap";
    public static final String KEY_BOTTOM_PACKAGE_DOUBLE_TAP = "bottom_package_double_tap";
    public static final String KEY_BOTTOM_PACKAGE_LONG_PRESS = "bottom_package_long_press";

    // Top Target App Package keys
    public static final String KEY_TOP_PACKAGE_SINGLE_TAP = "top_package_single_tap";
    public static final String KEY_TOP_PACKAGE_DOUBLE_TAP = "top_package_double_tap";
    public static final String KEY_TOP_PACKAGE_LONG_PRESS = "top_package_long_press";

    // Preview Overlays
    private View topLivePreviewBox;
    private View bottomLivePreviewBox;

    private SharedPreferences sharedPreferences;

    public static class AppInfo {
        public String label;
        public String packageName;
        public android.graphics.drawable.Drawable icon;

        public AppInfo(String label, String packageName, android.graphics.drawable.Drawable icon) {
            this.label = label;
            this.packageName = packageName;
            this.icon = icon;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Pre-initialize Preferences and Theme configuration before standard lifecycle creation
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark_theme", true);
        if (isDark) {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        } else {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

        // Initialize edge to edge capabilities
        androidx.activity.EdgeToEdge.enable(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind Live previews
        topLivePreviewBox = findViewById(R.id.top_live_preview_box);
        bottomLivePreviewBox = findViewById(R.id.bottom_live_preview_box);

        // Setup Top padding window inset management on header view
        View rootLayout = findViewById(R.id.root_layout);
        View headerLayoutContainer = findViewById(R.id.header_layout_container);
        if (rootLayout != null && headerLayoutContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                int statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                headerLayoutContainer.setPadding(
                        headerLayoutContainer.getPaddingLeft(),
                        statusBarTop,
                        headerLayoutContainer.getPaddingRight(),
                        headerLayoutContainer.getPaddingBottom()
                );
                return insets;
            });
        }

        // Bind TabLayout and ViewPager2
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        // Dynamic Theme Switch Click Listener
        androidx.appcompat.widget.AppCompatImageButton btnThemeToggle = findViewById(R.id.btn_theme_toggle);
        if (btnThemeToggle != null) {
            btnThemeToggle.setImageResource(isDark ? R.drawable.ic_sun : R.drawable.ic_moon);
            btnThemeToggle.setOnClickListener(v -> {
                boolean currentDark = sharedPreferences.getBoolean("dark_theme", true);
                boolean nextDark = !currentDark;
                sharedPreferences.edit().putBoolean("dark_theme", nextDark).apply();

                if (nextDark) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                recreate(); // Instantly apply theme shift
            });
        }

        // Header pulsing heartbeat decorations on vector logo ImageView
        View headerIcon = findViewById(R.id.header_icon);
        if (headerIcon != null) {
            android.view.animation.AlphaAnimation anim = new android.view.animation.AlphaAnimation(1.0f, 0.4f);
            anim.setDuration(1500);
            anim.setRepeatMode(android.view.animation.Animation.REVERSE);
            anim.setRepeatCount(android.view.animation.Animation.INFINITE);
            headerIcon.startAnimation(anim);
        }

        viewPager.setAdapter(new ScreenSlidePagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Bottom Bar");
                    break;
                case 1:
                    tab.setText("Top Bar");
                    break;
                case 2:
                default:
                    tab.setText("Global");
                    break;
            }
        }).attach();

        // Safe initial layout rendering preview calculation
        getWindow().getDecorView().post(this::updateAllPreviews);
    }

    public void updateAllPreviews() {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        // 1. Bottom Zone PREVIEW
        boolean bottomEnabled = sharedPreferences.getBoolean(KEY_BOTTOM_ENABLED, true);
        int bottomTop = sharedPreferences.getInt(KEY_BOTTOM_THRESHOLD_TOP, 20);
        int bottomLeft = sharedPreferences.getInt(KEY_BOTTOM_THRESHOLD_LEFT, 0);
        int bottomRight = sharedPreferences.getInt(KEY_BOTTOM_THRESHOLD_RIGHT, 0);

        if (bottomLivePreviewBox != null) {
            bottomLivePreviewBox.setVisibility(bottomEnabled ? View.VISIBLE : View.GONE);
            if (bottomEnabled) {
                updatePreviewBoxGeometry(bottomLivePreviewBox, bottomTop, bottomLeft, bottomRight);
            }
        }

        // 2. Top Zone PREVIEW
        boolean topEnabled = sharedPreferences.getBoolean(KEY_TOP_ENABLED, true);
        int topHeight = sharedPreferences.getInt(KEY_TOP_THRESHOLD_HEIGHT, 20);
        int topLeft = sharedPreferences.getInt(KEY_TOP_THRESHOLD_LEFT, 0);
        int topRight = sharedPreferences.getInt(KEY_TOP_THRESHOLD_RIGHT, 0);

        if (topLivePreviewBox != null) {
            topLivePreviewBox.setVisibility(topEnabled ? View.VISIBLE : View.GONE);
            if (topEnabled) {
                updatePreviewBoxGeometry(topLivePreviewBox, topHeight, topLeft, topRight);
            }
        }
    }

    private void updatePreviewBoxGeometry(View box, int heightProgress, int leftPercent, int rightPercent) {
        if (box == null) return;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // heightProgress represents tenths of a percent (e.g., 20 = 2.0%)
        float heightPercent = heightProgress / 10.0f;
        int calculatedHeight = (int) (screenHeight * (heightPercent / 100.0f));
        int calculatedLeftMargin = (int) (screenWidth * (leftPercent / 100.0f));
        int calculatedRightMargin = (int) (screenWidth * (rightPercent / 100.0f));

        if (calculatedHeight < 30) {
            calculatedHeight = 30; // Min bounds to be visible
        }

        ViewGroup.LayoutParams params = box.getLayoutParams();
        if (params instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) params;
            lp.height = calculatedHeight;
            lp.leftMargin = calculatedLeftMargin;
            lp.rightMargin = calculatedRightMargin;
            box.setLayoutParams(lp);
        } else if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) params;
            lp.height = calculatedHeight;
            lp.leftMargin = calculatedLeftMargin;
            lp.rightMargin = calculatedRightMargin;
            box.setLayoutParams(lp);
        }
    }

    private static class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(AppCompatActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new BottomBarFragment();
                case 1:
                    return new TopBarFragment();
                case 2:
                default:
                    return new GlobalSettingsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
