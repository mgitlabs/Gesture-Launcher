package com.mahadi.gesturelauncher;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Edge-to-Edge configuration
        androidx.activity.EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_settings);

        // Header and back logic
        View rootLayout = findViewById(R.id.settings_root_layout);
        View settingsHeader = findViewById(R.id.settings_header);
        if (rootLayout != null && settingsHeader != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                int statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                settingsHeader.setPadding(
                        settingsHeader.getPaddingLeft(),
                        statusBarTop,
                        settingsHeader.getPaddingRight(),
                        settingsHeader.getPaddingBottom()
                );
                return insets;
            });
        }

        View btnBack = findViewById(R.id.btn_settings_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Dynamically load the GlobalSettingsFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_fragment_container, new GlobalSettingsFragment())
                    .commit();
        }
    }
}
