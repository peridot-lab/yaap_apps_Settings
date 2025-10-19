/*
 * Copyright (C) 2023-2025 Yet Another AOSP Project
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
import android.os.SystemProperties;
import android.provider.Settings;
import android.widget.CompoundButton;

public class TapPreferenceController extends DisplayWakeGesturePageController {

    private static final String KEY = "gesture_tap";
    private static final String AMBIENT_KEY = "doze_tap_gesture_ambient";
    private static final String AOD_KEY = "doze_tap_gesture_allow_ambient";
    private static final String VIB_KEY = "doze_tap_gesture_vibrate";

    public TapPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return getAmbientConfig().tapSensorAvailable();
    }

    @Override
    public boolean getDefault() {
        return false;
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    @Override
    public String getSettingsKey() {
        return Settings.Secure.DOZE_TAP_SCREEN_GESTURE;
    }

    @Override
    public String getAmbientKey() {
        return AMBIENT_KEY;
    }

    @Override
    public String getAODKey() {
        return AOD_KEY;
    }

    @Override
    public String getVibKey() {
        return VIB_KEY;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        super.onCheckedChanged(buttonView, isChecked);
        SystemProperties.set("persist.sys.tap_gesture", isChecked ? "1" : "0");
    }
}
