package com.mahadi.gesturelauncher;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.util.TypedValue;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.R;

import java.util.List;
import java.util.Locale;

public class TopBarFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    
    // UI elements
    private MaterialSwitch switchTopEnabled;
    private Slider sbTopThresholdHeight;
    private Slider sbTopThresholdLeft;
    private Slider sbTopThresholdRight;
    private TextView tvTopThresholdHeightLabel;
    private TextView tvTopThresholdLeftLabel;
    private TextView tvTopThresholdRightLabel;

    private AutoCompleteTextView spinTopSingleTap;
    private AutoCompleteTextView spinTopDoubleTap;
    private AutoCompleteTextView spinTopLongPress;
    private AutoCompleteTextView spinTopSwipeLeft;
    private AutoCompleteTextView spinTopSwipeRight;

    private LinearLayout layoutTopSingleTapApp;
    private LinearLayout layoutTopDoubleTapApp;
    private LinearLayout layoutTopLongPressApp;
    private TextView tvTopSingleTapApp;
    private TextView tvTopDoubleTapApp;
    private TextView tvTopLongPressApp;

    private TextView tvTopDelaySingleTapLabel;
    private TextView tvTopDelayDoubleTapWindowLabel;
    private TextView tvTopDelayLongPressLabel;
    private Slider sbTopDelaySingleTap;
    private Slider sbTopDelayDoubleTapWindow;
    private Slider sbTopDelayLongPress;

    private final String[] actionsDisplay = {
        "None (Disabled)",
        "Launch App",
        "Take Screenshot",
        "Expand Notifications",
        "Lock Screen",
        "Increase Volume",
        "Decrease Volume",
        "Increase Brightness",
        "Decrease Brightness",
        "Toggle Auto-Rotation",
        "Quick Note Pop-up"
    };

    private final String[] actionValues = {
        "none_disabled",
        "launch_app",
        "screenshot",
        "notifications",
        "lock_screen",
        "volume_up",
        "volume_down",
        "brightness_up",
        "brightness_down",
        "screen_rotation_toggle",
        "quick_notes"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_bar, container, false);
        
        sharedPreferences = requireContext().getSharedPreferences("GestureLauncherPrefs", Context.MODE_PRIVATE);

        // Bind views
        switchTopEnabled = view.findViewById(R.id.switch_top_enabled);
        sbTopThresholdHeight = view.findViewById(R.id.sb_top_threshold_height);
        sbTopThresholdLeft = view.findViewById(R.id.sb_top_threshold_left);
        sbTopThresholdRight = view.findViewById(R.id.sb_top_threshold_right);
        tvTopThresholdHeightLabel = view.findViewById(R.id.tv_top_threshold_height_label);
        tvTopThresholdLeftLabel = view.findViewById(R.id.tv_top_threshold_left_label);
        tvTopThresholdRightLabel = view.findViewById(R.id.tv_top_threshold_right_label);

        spinTopSingleTap = view.findViewById(R.id.spin_top_single_tap);
        spinTopDoubleTap = view.findViewById(R.id.spin_top_double_tap);
        spinTopLongPress = view.findViewById(R.id.spin_top_long_press);
        spinTopSwipeLeft = view.findViewById(R.id.spin_top_swipe_left);
        spinTopSwipeRight = view.findViewById(R.id.spin_top_swipe_right);

        layoutTopSingleTapApp = view.findViewById(R.id.layout_top_single_tap_app);
        layoutTopDoubleTapApp = view.findViewById(R.id.layout_top_double_tap_app);
        layoutTopLongPressApp = view.findViewById(R.id.layout_top_long_press_app);
        tvTopSingleTapApp = view.findViewById(R.id.tv_top_single_tap_app);
        tvTopDoubleTapApp = view.findViewById(R.id.tv_top_double_tap_app);
        tvTopLongPressApp = view.findViewById(R.id.tv_top_long_press_app);

        Button btnSelectTopSingleTap = view.findViewById(R.id.btn_select_top_single_tap);
        Button btnSelectTopDoubleTap = view.findViewById(R.id.btn_select_top_double_tap);
        Button btnSelectTopLongPress = view.findViewById(R.id.btn_select_top_long_press);

        tvTopDelaySingleTapLabel = view.findViewById(R.id.tv_top_delay_single_tap_label);
        tvTopDelayDoubleTapWindowLabel = view.findViewById(R.id.tv_top_delay_double_tap_window_label);
        tvTopDelayLongPressLabel = view.findViewById(R.id.tv_top_delay_long_press_label);
        
        sbTopDelaySingleTap = view.findViewById(R.id.sb_top_delay_single_tap);
        sbTopDelayDoubleTapWindow = view.findViewById(R.id.sb_top_delay_double_tap_window);
        sbTopDelayLongPress = view.findViewById(R.id.sb_top_delay_long_press);

        // Setup Spinners ArrayAdapter setup with dynamics
        setupSpinners();

        // Load Toggle States & Values
        loadSettings();

        // Setup Listeners
        setupListeners();

        // Dialog buttons connection
        btnSelectTopSingleTap.setOnClickListener(v -> openAppPickerDialog(MainActivity.KEY_TOP_PACKAGE_SINGLE_TAP));
        btnSelectTopDoubleTap.setOnClickListener(v -> openAppPickerDialog(MainActivity.KEY_TOP_PACKAGE_DOUBLE_TAP));
        btnSelectTopLongPress.setOnClickListener(v -> openAppPickerDialog(MainActivity.KEY_TOP_PACKAGE_LONG_PRESS));

        return view;
    }

    private void setupSpinners() {
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
            requireContext(),
            R.layout.dropdown_item,
            actionsDisplay
        );

        spinTopSingleTap.setAdapter(spinnerAdapter);
        spinTopDoubleTap.setAdapter(spinnerAdapter);
        spinTopLongPress.setAdapter(spinnerAdapter);
        spinTopSwipeLeft.setAdapter(spinnerAdapter);
        spinTopSwipeRight.setAdapter(spinnerAdapter);
    }

    private void loadSettings() {
        switchTopEnabled.setChecked(sharedPreferences.getBoolean(MainActivity.KEY_TOP_ENABLED, true));

        // Load spinner values
        setSpinnerSelection(spinTopSingleTap, sharedPreferences.getString(MainActivity.KEY_TOP_ACTION_SINGLE_TAP, "none_disabled"), layoutTopSingleTapApp);
        setSpinnerSelection(spinTopDoubleTap, sharedPreferences.getString(MainActivity.KEY_TOP_ACTION_DOUBLE_TAP, "notifications"), layoutTopDoubleTapApp);
        setSpinnerSelection(spinTopLongPress, sharedPreferences.getString(MainActivity.KEY_TOP_ACTION_LONG_PRESS, "none_disabled"), layoutTopLongPressApp);
        setSpinnerSelection(spinTopSwipeLeft, sharedPreferences.getString(MainActivity.KEY_TOP_ACTION_SWIPE_LEFT, "brightness_down"), null);
        setSpinnerSelection(spinTopSwipeRight, sharedPreferences.getString(MainActivity.KEY_TOP_ACTION_SWIPE_RIGHT, "brightness_up"), null);

        // Geometry Sliders Value
        sbTopThresholdHeight.setValue((float) sharedPreferences.getInt(MainActivity.KEY_TOP_THRESHOLD_HEIGHT, 20));
        sbTopThresholdLeft.setValue((float) sharedPreferences.getInt(MainActivity.KEY_TOP_THRESHOLD_LEFT, 0));
        sbTopThresholdRight.setValue((float) sharedPreferences.getInt(MainActivity.KEY_TOP_THRESHOLD_RIGHT, 0));

        // Timing Settings Load
        int delaySingleTapValue = sharedPreferences.getInt(MainActivity.KEY_TOP_DELAY_SINGLE_TAP, 250);
        int delayDoubleTapWindowValue = sharedPreferences.getInt(MainActivity.KEY_TOP_DELAY_DOUBLE_TAP_WINDOW, 300);
        int delayLongPressValue = sharedPreferences.getInt(MainActivity.KEY_TOP_DELAY_LONG_PRESS, 500);

        sbTopDelaySingleTap.setValue((float) clampProgress(delaySingleTapValue - 50, 350));
        sbTopDelayDoubleTapWindow.setValue((float) clampProgress(delayDoubleTapWindowValue - 150, 350));
        sbTopDelayLongPress.setValue((float) clampProgress(delayLongPressValue - 200, 1300));

        tvTopDelaySingleTapLabel.setText(delaySingleTapValue + "ms");
        tvTopDelayDoubleTapWindowLabel.setText(delayDoubleTapWindowValue + "ms");
        tvTopDelayLongPressLabel.setText(delayLongPressValue + "ms");

        updateAllAppLabels();
        updateLabelTexts();
    }

    private void setSpinnerSelection(AutoCompleteTextView view, String value, LinearLayout appLayout) {
        int index = getIndexInList(actionValues, value);
        view.setText(actionsDisplay[index], false);
        if (appLayout != null) {
            appLayout.setVisibility("launch_app".equals(value) ? View.VISIBLE : View.GONE);
        }
    }

    private int clampProgress(int progress, int max) {
        if (progress < 0) return 0;
        if (progress > max) return max;
        return progress;
    }

    private int getIndexInList(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(val)) return i;
        }
        return 0;
    }

    private void setupListeners() {
        switchTopEnabled.setOnCheckedChangeListener((btn, isChecked) -> {
            sharedPreferences.edit().putBoolean(MainActivity.KEY_TOP_ENABLED, isChecked).apply();
            notifyConfigChanged();
        });

        // Top Geometry Listeners
        Slider.OnSliderTouchListener topGeometryTouchListener = new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sharedPreferences.edit()
                    .putInt(MainActivity.KEY_TOP_THRESHOLD_HEIGHT, (int) sbTopThresholdHeight.getValue())
                    .putInt(MainActivity.KEY_TOP_THRESHOLD_LEFT, (int) sbTopThresholdLeft.getValue())
                    .putInt(MainActivity.KEY_TOP_THRESHOLD_RIGHT, (int) sbTopThresholdRight.getValue())
                    .apply();
                notifyConfigChanged();
            }
        };

        sbTopThresholdHeight.addOnSliderTouchListener(topGeometryTouchListener);
        sbTopThresholdLeft.addOnSliderTouchListener(topGeometryTouchListener);
        sbTopThresholdRight.addOnSliderTouchListener(topGeometryTouchListener);

        sbTopThresholdHeight.addOnChangeListener((slider, value, fromUser) -> {
            if (value < 1.0f) {
                slider.setValue(1.0f);
            }
            updateLabelTexts();
            triggerPreviewUpdateInActivity();
        });
        sbTopThresholdLeft.addOnChangeListener((slider, value, fromUser) -> {
            updateLabelTexts();
            triggerPreviewUpdateInActivity();
        });
        sbTopThresholdRight.addOnChangeListener((slider, value, fromUser) -> {
            updateLabelTexts();
            triggerPreviewUpdateInActivity();
        });

        // Bind Spinners Item Selected Listeners
        bindSpinnerListener(spinTopSingleTap, MainActivity.KEY_TOP_ACTION_SINGLE_TAP, layoutTopSingleTapApp);
        bindSpinnerListener(spinTopDoubleTap, MainActivity.KEY_TOP_ACTION_DOUBLE_TAP, layoutTopDoubleTapApp);
        bindSpinnerListener(spinTopLongPress, MainActivity.KEY_TOP_ACTION_LONG_PRESS, layoutTopLongPressApp);
        bindSpinnerListener(spinTopSwipeLeft, MainActivity.KEY_TOP_ACTION_SWIPE_LEFT, null);
        bindSpinnerListener(spinTopSwipeRight, MainActivity.KEY_TOP_ACTION_SWIPE_RIGHT, null);

        // Timing listeners
        sbTopDelaySingleTap.addOnChangeListener((slider, value, fromUser) -> {
            tvTopDelaySingleTapLabel.setText(((int) value + 50) + "ms");
        });
        sbTopDelaySingleTap.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sharedPreferences.edit().putInt(MainActivity.KEY_TOP_DELAY_SINGLE_TAP, (int) sbTopDelaySingleTap.getValue() + 50).apply();
                notifyConfigChanged();
            }
        });

        sbTopDelayDoubleTapWindow.addOnChangeListener((slider, value, fromUser) -> {
            tvTopDelayDoubleTapWindowLabel.setText(((int) value + 150) + "ms");
        });
        sbTopDelayDoubleTapWindow.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sharedPreferences.edit().putInt(MainActivity.KEY_TOP_DELAY_DOUBLE_TAP_WINDOW, (int) sbTopDelayDoubleTapWindow.getValue() + 150).apply();
                notifyConfigChanged();
            }
        });

        sbTopDelayLongPress.addOnChangeListener((slider, value, fromUser) -> {
            tvTopDelayLongPressLabel.setText(((int) value + 200) + "ms");
        });
        sbTopDelayLongPress.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sharedPreferences.edit().putInt(MainActivity.KEY_TOP_DELAY_LONG_PRESS, (int) sbTopDelayLongPress.getValue() + 200).apply();
                notifyConfigChanged();
            }
        });
    }

    private void bindSpinnerListener(AutoCompleteTextView view, String prefKey, LinearLayout appLayout) {
        view.setOnItemClickListener((parent, v, pos, id) -> {
            String val = actionValues[pos];
            sharedPreferences.edit().putString(prefKey, val).apply();
            if (appLayout != null) {
                appLayout.setVisibility("launch_app".equals(val) ? View.VISIBLE : View.GONE);
            }
            notifyConfigChanged();
            if ("brightness_up".equals(val) || "brightness_down".equals(val)) {
                checkAndRequestWriteSettingsPermission();
            }
        });
    }

    private void checkAndRequestWriteSettingsPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(requireContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(android.net.Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
                Toast.makeText(requireContext(), "Please grant System Write Settings permission to adjust brightness.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateLabelTexts() {
        int topHeight = (int) sbTopThresholdHeight.getValue();
        int topLeft = (int) sbTopThresholdLeft.getValue();
        int topRight = (int) sbTopThresholdRight.getValue();

        tvTopThresholdHeightLabel.setText(String.format(Locale.US, "%.1f%%", topHeight / 10.0));
        tvTopThresholdLeftLabel.setText(topLeft + "%");
        tvTopThresholdRightLabel.setText(topRight + "%");
    }

    private void triggerPreviewUpdateInActivity() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateAllPreviews();
        }
    }

    private void notifyConfigChanged() {
        triggerPreviewUpdateInActivity();
        Intent intent = new Intent("com.mahadi.gesturelauncher.CONFIG_CHANGED");
        intent.setPackage(requireContext().getPackageName());
        requireContext().sendBroadcast(intent);
    }

    private void updateAllAppLabels() {
        PackageManager pm = requireContext().getPackageManager();
        String fallback = "com.android.chrome";

        setAppLabel(pm, tvTopSingleTapApp, sharedPreferences.getString(MainActivity.KEY_TOP_PACKAGE_SINGLE_TAP, fallback));
        setAppLabel(pm, tvTopDoubleTapApp, sharedPreferences.getString(MainActivity.KEY_TOP_PACKAGE_DOUBLE_TAP, fallback));
        setAppLabel(pm, tvTopLongPressApp, sharedPreferences.getString(MainActivity.KEY_TOP_PACKAGE_LONG_PRESS, fallback));
    }

    private void setAppLabel(PackageManager pm, TextView tv, String packageName) {
        if (tv == null) return;
        try {
            tv.setText(pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)) + "\n(" + packageName + ")");
        } catch (Exception e) {
            tv.setText("App: " + packageName);
        }
    }

    private void openAppPickerDialog(final String preferenceKey) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout rootLayout = new LinearLayout(requireContext());
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#121824")); 
        rootLayout.setPadding(32, 40, 32, 40);

        TextView titleTv = new TextView(requireContext());
        titleTv.setText("Choose Application Target");
        titleTv.setTextColor(Color.WHITE);
        titleTv.setTextSize(18);
        titleTv.setPadding(0, 0, 0, 24);
        titleTv.setTypeface(null, android.graphics.Typeface.BOLD);
        rootLayout.addView(titleTv);

        final ProgressBar progressBar = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        rootLayout.addView(progressBar);

        final ListView listView = new ListView(requireContext());
        listView.setDivider(new ColorDrawable(Color.parseColor("#243146")));
        listView.setDividerHeight(1);
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        rootLayout.addView(listView);

        dialog.setContentView(rootLayout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().heightPixels * 0.72));
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#121824")));
        dialog.show();

        new Thread(() -> {
            final java.util.ArrayList<MainActivity.AppInfo> apps = new java.util.ArrayList<>();
            PackageManager pm = requireContext().getPackageManager();
            Intent queryIntent = new Intent(Intent.ACTION_MAIN, null);
            queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolves = pm.queryIntentActivities(queryIntent, 0);

            resolves.sort((r1, r2) -> r1.loadLabel(pm).toString().compareToIgnoreCase(r2.loadLabel(pm).toString()));

            for (ResolveInfo ri : resolves) {
                String name = ri.loadLabel(pm).toString();
                String pkg = ri.activityInfo.packageName;
                android.graphics.drawable.Drawable icon = ri.loadIcon(pm);
                apps.add(new MainActivity.AppInfo(name, pkg, icon));
            }

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                listView.setAdapter(new android.widget.BaseAdapter() {
                    @Override
                    public int getCount() {
                        return apps.size();
                    }
                    @Override
                    public Object getItem(int position) {
                        return apps.get(position);
                    }
                    @Override
                    public long getItemId(int position) {
                        return position;
                    }
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            LinearLayout row = new LinearLayout(requireContext());
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            row.setPadding(20, 24, 20, 24);
                            row.setGravity(Gravity.CENTER_VERTICAL);

                            android.widget.ImageView iv = new android.widget.ImageView(requireContext());
                            LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(96, 96);
                            ivParams.rightMargin = 28;
                            iv.setLayoutParams(ivParams);
                            row.addView(iv);

                            LinearLayout txts = new LinearLayout(requireContext());
                            txts.setOrientation(LinearLayout.VERTICAL);

                            TextView nameTv = new TextView(requireContext());
                            nameTv.setTextColor(Color.WHITE);
                            nameTv.setTextSize(14);
                            nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
                            txts.addView(nameTv);

                            TextView pkgTv = new TextView(requireContext());
                            pkgTv.setTextColor(Color.parseColor("#94A3B8"));
                            pkgTv.setTextSize(11);
                            txts.addView(pkgTv);

                            row.addView(txts);
                            convertView = row;
                        }

                        MainActivity.AppInfo info = apps.get(position);
                        android.widget.ImageView iv = (android.widget.ImageView) ((ViewGroup) convertView).getChildAt(0);
                        LinearLayout txts = (LinearLayout) ((ViewGroup) convertView).getChildAt(1);
                        TextView nameTv = (TextView) txts.getChildAt(0);
                        TextView pkgTv = (TextView) txts.getChildAt(1);

                        iv.setImageDrawable(info.icon);
                        nameTv.setText(info.label);
                        pkgTv.setText(info.packageName);

                        return convertView;
                    }
                });

                listView.setOnItemClickListener((parent2, view2, position, id) -> {
                    MainActivity.AppInfo select = apps.get(position);
                    sharedPreferences.edit()
                        .putString(preferenceKey, select.packageName)
                        .apply();
                    updateAllAppLabels();
                    notifyConfigChanged();
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Assigned: " + select.label, Toast.LENGTH_SHORT).show();
                });
            });
        }).start();
    }
}
