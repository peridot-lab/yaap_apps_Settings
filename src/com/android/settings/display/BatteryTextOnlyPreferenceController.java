/*
 * Copyright (C) 2026 Yet Another AOSP Project
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
package com.android.settings.display;

import static android.provider.Settings.System.BATTERY_TEXT_ONLY;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;

/**
 * A controller to manage the switch for showing battery percentage as text only in the status bar.
 */

// LINT.IfChange
public class BatteryTextOnlyPreferenceController extends BasePreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private Preference mPreference;

    public BatteryTextOnlyPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        if (!Utils.isBatteryPresent(mContext)) {
            onPreferenceChange(mPreference, false /* newValue */);
        }
    }

    @Override
    public int getAvailabilityStatus() {
        if (!Utils.isBatteryPresent(mContext)) {
            return CONDITIONALLY_UNAVAILABLE;
        }
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        boolean enabled = Settings.System.getInt(mContext.getContentResolver(),
                BATTERY_TEXT_ONLY, 0) == 1;
        ((TwoStatePreference) preference).setChecked(enabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean value = (Boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(),
                BATTERY_TEXT_ONLY, value ? 1 : 0);
        return true;
    }
}
// LINT.ThenChange(BatteryTextOnlySwitchPreference.kt)
