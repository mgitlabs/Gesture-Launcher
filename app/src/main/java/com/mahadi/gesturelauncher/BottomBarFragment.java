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

public class BottomBarFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    
    // UI elements
    private MaterialSwitch switchBottomEnabled;
    private Slider sbBottomThresholdTop;
    private Slider sbBottomThresholdLeft;
    private Slider sbBottomThresholdRight;
    private TextView tvBottomThresholdTopLabel;
    private TextView tvBottomThresholdLeftLabel;
    private TextView tvBottomThresholdRightLabel;

    private AutoCompleteTextView spinBottomSingleTap;
    private AutoCompleteTextView spinBottomDoubleTap;
    private AutoCompleteTextView spinBottomLongPress;
    private AutoCompleteTextView spinBottomSwipeLeft;
    private AutoCompleteTextView spinBottomSwipeRight;

    private LinearLayout layoutBottomSingleTapApp;
    private LinearLayout layoutBottomDoubleTapApp;
    private LinearLayout layoutBottomLongPressApp;
    private TextView tvBottomSingleTapApp;
    private TextView tvBottomDoubleTapApp;
    private TextView tvBottomLongPressApp;

    private TextView tvBottomDelaySingleTapLabel;
    private TextView tvBottomDelayDoubleTapWindowLabel;
    private TextView tvBottomDelayLongPressLabel;
    private Slider sbBottomDelaySingleTap;
    private Slider sbBottomDelayDoubleTapWindow;
    private Slider sbBottomDelayLongPress;

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
        View view = inflater.inflate(R.layout.fragment_bottom_bar, container, false);
        
        sharedPreferences = requireContext().getSharedPreferences("GestureLauncherPrefs", Context.MODE_PRIVATE);

        // Bind views
        switchBottomEnabled = view.findViewById(R.id.switch_bottom_enabled);
        sbBottomThresholdTop = view.findViewById(R.id.sb_bottom_threshold_top);
        sbBottomThresholdLeft = view.findViewById(R.id.sb_bottom_threshold_left);
        sbBottomThresholdRight = view.findViewById(R.id.sb_bottom_threshold_right);
        tvBottomThresholdTopLabel = view.findViewById(R.id.tv_bottom_threshold_top_label);
        tvBottomThresholdLeftLabel = view.findViewById(R.id.tv_bottom_threshold_left_label);
        tvBottomThresholdRightLabel = view.findViewById(R.id.tv_bottom_threshold_right_label);

        spinBottomSingleTap = view.findViewById(R.id.spin_bottom_single_tap);
        spinBottomDoubleTap = view.findViewById(R.id.spin_bottom_double_tap);
        spinBottomLongPress = view.findViewById(R.id.spin_bottom_long_press);
        spinBottomSwipeLeft = view.findViewById(R.id.spin_bottom_swipe_left);
        spinBottomSwipeRight = view.findViewById(R.id.spin_bottom_swipe_right);

        layoutBottomSingleTapApp = view.findViewById(R.id.layout_bottom_single_tap_app);
        layoutBottomDoubleTapApp = view.findViewById(R.id.layout_bottom_double_tap_app);
        layoutBottomLongPressApp = view.findViewById(R.id.layout_bottom_long_press_app);
        tvBottomSingleTapApp = view.findViewById(R.id.tv_bottom_single_tap_app);
        tvBottomDoubleTapApp = view.findViewById(R.id.tv_bottom_double_tap_app);
        tvBottomLongPressApp = view.findViewById(R.id.tv_bottom_long_press_app);

        Button btnSelectBottomSingleTap = view.findViewById(R.id.btn_select_bottom_single_tap);
        Button btnSelectBottomDoubleTap = view.findViewById(R.id.btn_select_bottom_double_tap);
        Button btnSelectBottomLongPress = view.findViewById(R.id.btn_select_bottom_long_press);

        tvBottomDelaySingleTapLabel = view.findViewById(R.id.tv_bottom_delay_single_tap_label);
        tvBottomDelayDoubleTapWindowLabel = view.findViewById(R.id.tv_bottom_delay_double_tap_window_label);
        tvBottomDelayLongPressLabel = view.findViewById(R.id.tv_bottom_delay_long_press_label);
        
        sbBottomDelaySingleTap = view.findViewById(R.id.sb_bottom_delay_single_tap);
        sbBottomDelayDoubleTapWindow = view.findViewById(R.id.sb_bottom_delay_double_tap_window);
        sbBottomDelayLongPress = view.findViewById(R.id.sb_bottom_delay_long_press);

        // Setup Spinners ArrayAdapter setup with dynamics
        setupSpinners();

        // Load Toggle States & Values
        loadSettings();

        // Setup Listeners
        setupListeners();

        // Dialog buttons connection
        btnSelectBottomSingleTap.setOnClickListener(v -> openAppPickerDialog(MainActivity.KEY_BOTTOM_PACKAGE_SINGLE_TAP));
        btnSelectBottomDoubleTap.setOnClickListener(v -> openAppPickerDialog(MainActivity.KEY_BOTTOM_PACKAGE_DOUBLE_TAP));
        btnSelectBottomLongPress.setOnClickListener(v -> openAppPickerDialog(MainActivity.KEY_BOTTOM_PACKAGE_LONG_PRESS));

        return view;
    }

    private void setupSpinners() {
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
            requireContext(),
            R.layout.dropdown_item,
            actionsDisplay
        );

        spinBottomSingleTap.setAdapter(spinnerAdapter);
        spinBottomDoubleTap.setAdapter(spinnerAdapter);
        spinBottomLongPress.setAdapter(spinnerAdapter);
        spinBottomSwipeLeft.setAdapter(spinnerAdapter);
        spinBottomSwipeRight.setAdapter(spinnerAdapter);
    }

    private void loadSettings() {
        switchBottomEnabled.setChecked(sharedPreferences.getBoolean(MainActivity.KEY_BOTTOM_ENABLED, true));

        // Load spinner values
        setSpinnerSelection(spinBottomSingleTap, sharedPreferences.getString(MainActivity.KEY_BOTTOM_ACTION_SINGLE_TAP, "none_disabled"), layoutBottomSingleTapApp);
        setSpinnerSelection(spinBottomDoubleTap, sharedPreferences.getString(MainActivity.KEY_BOTTOM_ACTION_DOUBLE_TAP, "screenshot"), layoutBottomDoubleTapApp);
        setSpinnerSelection(spinBottomLongPress, sharedPreferences.getString(MainActivity.KEY_BOTTOM_ACTION_LONG_PRESS, "launch_app"), layoutBottomLongPressApp);
        setSpinnerSelection(spinBottomSwipeLeft, sharedPreferences.getString(MainActivity.KEY_BOTTOM_ACTION_SWIPE_LEFT, "volume_down"), null);
        setSpinnerSelection(spinBottomSwipeRight, sharedPreferences.getString(MainActivity.KEY_BOTTOM_ACTION_SWIPE_RIGHT, "volume_up"), null);

        // Geometry Sliders Progress
        sbBottomThresholdTop.setValue((float) sharedPreferences.getInt(MainActivity.KEY_BOTTOM_THRESHOLD_TOP, 20));
        sbBottomThresholdLeft.setValue((float) sharedPreferences.getInt(MainActivity.KEY_BOTTOM_THRESHOLD_LEFT, 0));
        sbBottomThresholdRight.setValue((float) sharedPreferences.getInt(MainActivity.KEY_BOTTOM_THRESHOLD_RIGHT, 0));

        // Timing Settings Load
        int delaySingleTapValue = sharedPreferences.getInt(MainActivity.KEY_BOTTOM_DELAY_SINGLE_TAP, 250);
        int delayDoubleTapWindowValue = sharedPreferences.getInt(MainActivity.KEY_BOTTOM_DELAY_DOUBLE_TAP_WINDOW, 300);
        int delayLongPressValue = sharedPreferences.getInt(MainActivity.KEY_BOTTOM_DELAY_LONG_PRESS, 500);

        sbBottomDelaySingleTap.setValue((float) clampProgress(delaySingleTapValue - 50, 350));
        sbBottomDelayDoubleTapWindow.setValue((float) clampProgress(delayDoubleTapWindowValue - 150, 350));
        sbBottomDelayLongPress.setValue((float) clampProgress(delayLongPressValue - 200, 1300));

        tvBottomDelaySingleTapLabel.setText(delaySingleTapValue + "ms");
        tvBottomDelayDoubleTapWindowLabel.setText(delayDoubleTapWindowValue + "ms");
        tvBottomDelayLongPressLabel.setText(delayLongPressValue + "ms");

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
        switchBottomEnabled.setOnCheckedChangeListener((btn, isChecked) -> {
            sharedPreferences.edit().putBoolean(MainActivity.KEY_BOTTOM_ENABLED, isChecked).apply();
            notifyConfigChanged();
        });

        // Bottom Geometry Listeners
        Slider.OnSliderTouchListener bottomGeometryTouchListener = new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sharedPreferences.edit()
                    .putInt(MainActivity.KEY_BOTTOM_THRESHOLD_TOP, (int) sbBottomThresholdTop.getValue())
                    .putInt(MainActivity.KEY_BOTTOM_THRESHOLD_LEFT, (int) sbBottomThresholdLeft.getValue())
                    .putInt(MainActivity.KEY_BOTTOM_THRESHOLD_RIGHT, (int) sbBottomThresholdRight.getValue())
                    .apply();
                notifyConfigChanged();
            }
        };

        sbBottomThresholdTop.addOnSliderTouchListener(bottomGeometryTouchListener);
        sbBottomThresholdLeft.addOnSliderTouchListener(bottomGeometryTouchListener);
        sbBottomThresholdRight.addOnSliderTouchListener(bottomGeometryTouchListener);

        sbBottomThresholdTop.addOnChangeListener((slider, value, fromUser) -> {
            if (value < 1.0f) {
                slider.setValue(1.0f);
            }
            updateLabelTexts();
            triggerPreviewUpdateInActivity();
        });
        sbBottomThresholdLeft.addOnChangeListener((slider, value, fromUser) -> {
            updateLabelTexts();
            triggerPreviewUpdateInActivity();
        });
        sbBottomThresholdRight.addOnChangeListener((slider, value, fromUser) -> {
            updateLabelTexts();
            triggerPreviewUpdateInActivity();
        });

        // Bind Spinners Item Selected Listeners
        bindSpinnerListener(spinBottomSingleTap, MainActivity.KEY_BOTTOM_ACTION_SINGLE_TAP, layoutBottomSingleTapApp);
        bindSpinnerListener(spinBottomDoubleTap, MainActivity.KEY_BOTTOM_ACTION_DOUBLE_TAP, layoutBottomDoubleTapApp);
        bindSpinnerListener(spinBottomLongPress, MainActivity.KEY_BOTTOM_ACTION_LONG_PRESS, layoutBottomLongPressApp);
        bindSpinnerListener(spinBottomSwipeLeft, MainActivity.KEY_BOTTOM_ACTION_SWIPE_LEFT, null);
        bindSpinnerListener(spinBottomSwipeRight, MainActivity.KEY_BOTTOM_ACTION_SWIPE_RIGHT, null);

        // Timing listeners
        sbBottomDelaySingleTap.addOnChangeListener((slider, value, fromUser) -> {
            tvBottomDelaySingleTapLabel.setText(((int) value + 50) + "ms");
        });
        sbBottomDelaySingleTap.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sharedPreferences.edit().putInt(MainActivity.KEY_BOTTOM_DELAY_SINGLE_TAP, (int) sbBottomDelaySingleTap.getValue() + 50).apply();
                notifyConfigChanged();
            }
        });

        sbBottomDelayDoubleTapWindow.addOnChangeListener((slider, value, fromUser) -> {
            tvBottomDelayDoubleTapWindowLabel.setText(((int) value + 150) + "ms");
        });
        sbBottomDelayDoubleTapWindow.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sharedPreferences.edit().putInt(MainActivity.KEY_BOTTOM_DELAY_DOUBLE_TAP_WINDOW, (int) sbBottomDelayDoubleTapWindow.getValue() + 150).apply();
                notifyConfigChanged();
            }
        });

        sbBottomDelayLongPress.addOnChangeListener((slider, value, fromUser) -> {
            tvBottomDelayLongPressLabel.setText(((int) value + 200) + "ms");
        });
        sbBottomDelayLongPress.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sharedPreferences.edit().putInt(MainActivity.KEY_BOTTOM_DELAY_LONG_PRESS, (int) sbBottomDelayLongPress.getValue() + 200).apply();
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
        int bottomTop = (int) sbBottomThresholdTop.getValue();
        int bottomLeft = (int) sbBottomThresholdLeft.getValue();
        int bottomRight = (int) sbBottomThresholdRight.getValue();

        tvBottomThresholdTopLabel.setText(String.format(Locale.US, "%.1f%%", bottomTop / 10.0));
        tvBottomThresholdLeftLabel.setText(bottomLeft + "%");
        tvBottomThresholdRightLabel.setText(bottomRight + "%");
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

        setAppLabel(pm, tvBottomSingleTapApp, sharedPreferences.getString(MainActivity.KEY_BOTTOM_PACKAGE_SINGLE_TAP, fallback));
        setAppLabel(pm, tvBottomDoubleTapApp, sharedPreferences.getString(MainActivity.KEY_BOTTOM_PACKAGE_DOUBLE_TAP, fallback));
        setAppLabel(pm, tvBottomLongPressApp, sharedPreferences.getString(MainActivity.KEY_BOTTOM_PACKAGE_LONG_PRESS, fallback));
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
