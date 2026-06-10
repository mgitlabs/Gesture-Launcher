package com.mahadi.gesturelauncher;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.R;

import java.util.List;

public class GlobalSettingsFragment extends Fragment {

    private View statusDot;
    private TextView tvServiceStatus;
    private TextView tvWriteSettingsStatus;
    private Button btnEnableService;
    private Button btnGrantWriteSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_global_settings, container, false);

        statusDot = view.findViewById(R.id.status_dot);
        tvServiceStatus = view.findViewById(R.id.tv_service_status);
        tvWriteSettingsStatus = view.findViewById(R.id.tv_write_settings_status);
        btnEnableService = view.findViewById(R.id.btn_enable_service);
        btnGrantWriteSettings = view.findViewById(R.id.btn_grant_write_settings);

        // Header pulsing heartbeat decoration
        View pulseDot = view.findViewById(R.id.pulse_dot);
        if (pulseDot != null) {
            android.view.animation.AlphaAnimation anim = new android.view.animation.AlphaAnimation(1.0f, 0.2f);
            anim.setDuration(1200);
            anim.setRepeatMode(android.view.animation.Animation.REVERSE);
            anim.setRepeatCount(android.view.animation.Animation.INFINITE);
            pulseDot.startAnimation(anim);
        }

        btnEnableService.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        btnGrantWriteSettings.setOnClickListener(v -> {
            checkAndRequestWriteSettingsPermission();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateServiceStatusUI();
        updateWriteSettingsStatusUI();
    }

    private void updateServiceStatusUI() {
        boolean isEnabled = isAccessibilityServiceEnabled();
        tvServiceStatus.setText(isEnabled ? "ACTIVE" : "INACTIVE");

        GradientDrawable statusDrawable = new GradientDrawable();
        statusDrawable.setShape(GradientDrawable.OVAL);
        int activeColor = isEnabled ? Color.parseColor("#10B981") : Color.parseColor("#EF4444");
        statusDrawable.setColor(activeColor);
        statusDot.setBackground(statusDrawable);
    }

    private void updateWriteSettingsStatusUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean canWrite = Settings.System.canWrite(requireContext());
            tvWriteSettingsStatus.setText(canWrite ? "GRANTED" : "DENIED");
            tvWriteSettingsStatus.setTextColor(canWrite ? Color.parseColor("#10B981") : Color.parseColor("#EF4444"));
        } else {
            tvWriteSettingsStatus.setText("GRANTED");
            tvWriteSettingsStatus.setTextColor(Color.parseColor("#10B981"));
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) return false;

        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_GENERIC);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(requireContext().getPackageName()) &&
                enabledServiceInfo.name.equals(NavbarAccessibilityService.class.getName())) {
                return true;
            }
        }
        return false;
    }

    private void checkAndRequestWriteSettingsPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(requireContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(android.net.Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
                Toast.makeText(requireContext(), "Please grant System Write Settings permission to adjust brightness.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), "System Write Settings permission is already GRANTED.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Your Android version grants this permission automatically.", Toast.LENGTH_SHORT).show();
        }
    }
}
