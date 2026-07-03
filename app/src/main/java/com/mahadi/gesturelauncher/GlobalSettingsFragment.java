package com.mahadi.gesturelauncher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

public class GlobalSettingsFragment extends Fragment {

    private static final String PREFS_NAME = "GestureLauncherPrefs";
    private SharedPreferences sharedPreferences;

    private MaterialSwitch switchThemeAuto;
    private MaterialSwitch switchThemeDark;

    private TextView tvAccessibilityStatus;
    private MaterialButton btnAccessibility;
    private TextView tvOverlayStatus;
    private MaterialButton btnOverlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_global_settings, container, false);

        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Bind Switches
        switchThemeAuto = view.findViewById(R.id.switch_theme_auto);
        switchThemeDark = view.findViewById(R.id.switch_theme_dark);

        // Bind Permissions status and buttons
        tvAccessibilityStatus = view.findViewById(R.id.status_accessibility);
        btnAccessibility = view.findViewById(R.id.btn_enable_accessibility);

        tvOverlayStatus = view.findViewById(R.id.status_overlay);
        btnOverlay = view.findViewById(R.id.btn_enable_overlay);

        loadThemePreferences();
        setupThemeListeners();
        setupPermissionListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePermissionStatus();
    }

    private void loadThemePreferences() {
        boolean autoMode = sharedPreferences.getBoolean("theme_auto", false);
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", true);

        if (switchThemeAuto != null) {
            switchThemeAuto.setChecked(autoMode);
        }
        if (switchThemeDark != null) {
            switchThemeDark.setChecked(darkTheme);
            switchThemeDark.setEnabled(!autoMode);
        }

        updateAppNightMode(autoMode, darkTheme);
    }

    private void setupThemeListeners() {
        if (switchThemeAuto != null) {
            switchThemeAuto.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean("theme_auto", isChecked).apply();
                if (switchThemeDark != null) {
                    switchThemeDark.setEnabled(!isChecked);
                }
                boolean currentDark = sharedPreferences.getBoolean("dark_theme", true);
                updateAppNightMode(isChecked, currentDark);
            });
        }

        if (switchThemeDark != null) {
            switchThemeDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean("dark_theme", isChecked).apply();
                updateAppNightMode(false, isChecked);
            });
        }
    }

    private void setupPermissionListeners() {
        if (btnAccessibility != null) {
            btnAccessibility.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            });
        }

        if (btnOverlay != null) {
            btnOverlay.setOnClickListener(v -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, 
                        Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), "Overlay permission allowed already", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updatePermissionStatus() {
        if (tvAccessibilityStatus != null && btnAccessibility != null) {
            boolean active = isAccessibilityServiceEnabled();
            if (active) {
                tvAccessibilityStatus.setText("Enabled");
                tvAccessibilityStatus.setTextColor(0xFF10B981); // premium green
                btnAccessibility.setEnabled(false);
                btnAccessibility.setText("Active");
            } else {
                tvAccessibilityStatus.setText("Disabled");
                tvAccessibilityStatus.setTextColor(0xFFEF4444); // deep red
                btnAccessibility.setEnabled(true);
                btnAccessibility.setText("Enable Accessibility Service");
            }
        }

        if (tvOverlayStatus != null && btnOverlay != null) {
            boolean active = isOverlayPermissionEnabled();
            if (active) {
                tvOverlayStatus.setText("Enabled");
                tvOverlayStatus.setTextColor(0xFF10B981); // premium green
                btnOverlay.setEnabled(false);
                btnOverlay.setText("Permission Allowed");
            } else {
                tvOverlayStatus.setText("Disabled");
                tvOverlayStatus.setTextColor(0xFFEF4444); // deep red
                btnOverlay.setEnabled(true);
                btnOverlay.setText("Allow Overlay Permission");
            }
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        String expectedComponentName = requireContext().getPackageName() + "/" + NavbarAccessibilityService.class.getName();
        String settingValue = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        if (settingValue != null) {
            String[] services = settingValue.split(":");
            for (String service : services) {
                if (service.equalsIgnoreCase(expectedComponentName) || service.contains(NavbarAccessibilityService.class.getSimpleName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOverlayPermissionEnabled() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(requireContext());
        }
        return true;
    }

    private void updateAppNightMode(boolean autoMode, boolean darkTheme) {
        if (autoMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            if (darkTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }
}
