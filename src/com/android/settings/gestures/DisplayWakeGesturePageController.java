/*
 * Copyright (C) 2025 Yet Another AOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.gestures;

import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.MainSwitchPreference;

import com.yasp.settings.preferences.SecureSettingSwitchPreference;

public abstract class DisplayWakeGesturePageController extends AbstractPreferenceController
        implements PreferenceControllerMixin, OnCheckedChangeListener {

    final Context mContext;
    private final boolean mDefault;
    private AmbientDisplayConfiguration mAmbientConfig;
    private MainSwitchPreference mSwitch;
    private SecureSettingSwitchPreference mAmbientPref;
    private SecureSettingSwitchPreference mAODPref;
    private SecureSettingSwitchPreference mVibPref;

    private boolean mIsVibAvailable;

    public DisplayWakeGesturePageController(Context context) {
        super(context);
        mContext = context;
        mDefault = getDefault();
    }

    public abstract boolean getDefault();
    public abstract String getSettingsKey();
    public abstract String getAmbientKey();
    public abstract String getAODKey();
    public abstract String getVibKey();

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mAmbientPref = screen.findPreference(getAmbientKey());
        mAODPref = screen.findPreference(getAODKey());
        mSwitch = screen.findPreference(getPreferenceKey());
        mSwitch.setOnPreferenceClickListener(preference -> {
            final boolean enabled = Settings.Secure.getInt(mContext.getContentResolver(),
                    getSettingsKey(), mDefault ? 1 : 0) == 1;
            Settings.Secure.putInt(mContext.getContentResolver(),
                    getSettingsKey(), enabled ? 0 : 1);
            updateEnablement(!enabled);
            return true;
        });
        updateAmbientEnablement();
        mSwitch.addOnSwitchChangeListener(this);
        updateState(mSwitch);

        mVibPref = screen.findPreference(getVibKey());
        final Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mIsVibAvailable = vibrator != null && vibrator.hasVibrator();
        if (!mIsVibAvailable) mVibPref.setVisible(false);
    }

    public void setChecked(boolean isChecked) {
        if (mSwitch != null) {
            mSwitch.setChecked(isChecked);
        }
        updateEnablement(isChecked);
        updateAmbientEnablement(isChecked);
    }

    @Override
    public void updateState(Preference preference) {
        final boolean enabled = Settings.Secure.getInt(mContext.getContentResolver(),
                getSettingsKey(), mDefault ? 1 : 0) == 1;
        setChecked(enabled);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Settings.Secure.putInt(mContext.getContentResolver(),
                getSettingsKey(), isChecked ? 1 : 0);
        updateEnablement(isChecked);
        updateAmbientEnablement(isChecked);
    }

    private void updateEnablement(boolean enabled) {
        if (mAmbientPref != null) mAmbientPref.setEnabled(enabled);
        if (mAODPref != null) mAODPref.setEnabled(enabled);
        if (mVibPref != null && mIsVibAvailable) mVibPref.setEnabled(enabled);
    }

    private void updateAmbientEnablement() {
        if (mSwitch == null) return;
        updateAmbientEnablement(mSwitch.isChecked());
    }

    private void updateAmbientEnablement(boolean isChecked) {
        if (mAmbientPref == null) return;
        AmbientDisplayConfiguration config = getAmbientConfig();
        if (!config.pulseOnNotificationAvailable()) {
            mAmbientPref.setVisible(false);
            return;
        }
        final boolean isEnabled = config.pulseOnNotificationEnabled(UserHandle.USER_CURRENT);
        mAmbientPref.setEnabled(isEnabled && isChecked);
        mAmbientPref.setSummary(isEnabled
                ? R.string.doze_gesture_ambient_summary
                : R.string.doze_disabled_summary);
    }

    AmbientDisplayConfiguration getAmbientConfig() {
        if (mAmbientConfig == null) {
            mAmbientConfig = new AmbientDisplayConfiguration(mContext);
        }

        return mAmbientConfig;
    }
}
