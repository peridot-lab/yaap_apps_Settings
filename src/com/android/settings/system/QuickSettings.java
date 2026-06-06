/*
 * Copyright (C) 2024 Yet Another AOSP Project
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
package com.android.settings.system;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.yasp.settings.preferences.CustomSeekBarPreference;
import com.yasp.settings.preferences.SecureSettingMasterSwitchPreference;
import com.yasp.settings.preferences.SystemSettingEditTextPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class QuickSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "QuickSettings";
    private static final String SYSTEMUI_PKG = "com.android.systemui";
    private static final String CONFIG = "max_shade_window_blur_radius";
    private static final String QS_FOOTER_TEXT_STRING = "qs_footer_text_string";
    private static final String BRIGHTNESS_SLIDER = "qs_show_brightness";
    private static final String SHADE_BLUR_RADIUS = "shade_blur_radius";
    private static final String SHADE_SCRIM_ALPHA = "shade_scrim_alpha";

    private SystemSettingEditTextPreference mFooterString;
    private SecureSettingMasterSwitchPreference mBrightnessSlider;
    private CustomSeekBarPreference mShadeBlurRadiusPref;
    private CustomSeekBarPreference mShadeScrimAlphaPref;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.yaap_settings_quicksettings;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mBrightnessSlider = (SecureSettingMasterSwitchPreference)
                findPreference(BRIGHTNESS_SLIDER);
        mBrightnessSlider.setOnPreferenceChangeListener(this);
        boolean enabled = Settings.Secure.getInt(resolver,
                BRIGHTNESS_SLIDER, 1) == 1;
        mBrightnessSlider.setChecked(enabled);

        // Get max_shade_window_blur_radius from SystemUI
        Context sysUiContext;
        try {
            sysUiContext = getContext().createPackageContext(SYSTEMUI_PKG,
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        } catch (NameNotFoundException e) {
            // Nothing to do, If SystemUI was not found you have bigger issues :)
            sysUiContext = getContext();
        }
        Resources sysUiRes = sysUiContext.getResources();
        final int resId = sysUiRes.getIdentifier(CONFIG, "dimen", SYSTEMUI_PKG);
        final int defBlurRadiusPx = sysUiRes.getDimensionPixelSize(resId);
        final float density = getContext().getResources().getDisplayMetrics().density;
        final int defBlurRadius = Math.round(defBlurRadiusPx / density);

        mShadeBlurRadiusPref = findPreference(SHADE_BLUR_RADIUS);
        mShadeBlurRadiusPref.setDefaultValue(defBlurRadius);
        mShadeBlurRadiusPref.setOnPreferenceChangeListener(this);
        int shadeBlurRadius = Settings.System.getIntForUser(resolver,
                SHADE_BLUR_RADIUS, defBlurRadius, UserHandle.USER_CURRENT);
        mShadeBlurRadiusPref.setValue(shadeBlurRadius);
        boolean blurEnabled = Settings.Global.getInt(resolver,
                Settings.Global.DISABLE_WINDOW_BLURS, 0) == 0;
        mShadeBlurRadiusPref.setEnabled(blurEnabled);
        if (!blurEnabled) {
            mShadeBlurRadiusPref.setSummary("System blur is disabled");
        } else {
            mShadeBlurRadiusPref.setSummary(R.string.shade_blur_radius_summary);
        }

        // Determine default scrim alpha based on blur support
        // When blur is available, the effective scrim opacity is ~0.60 (from shade scrim color),
        // otherwise it's 1.0 (fully opaque).
        final int defScrimAlpha = blurEnabled ? 60 : 100;

        mShadeScrimAlphaPref = findPreference(SHADE_SCRIM_ALPHA);
        mShadeScrimAlphaPref.setDefaultValue(defScrimAlpha);
        mShadeScrimAlphaPref.setOnPreferenceChangeListener(this);
        int shadeScrimAlpha = Settings.System.getIntForUser(resolver,
                SHADE_SCRIM_ALPHA, defScrimAlpha, UserHandle.USER_CURRENT);
        mShadeScrimAlphaPref.setValue(shadeScrimAlpha);

        mFooterString = (SystemSettingEditTextPreference) findPreference(QS_FOOTER_TEXT_STRING);
        mFooterString.setOnPreferenceChangeListener(this);
        String footerString = Settings.System.getString(resolver,
                QS_FOOTER_TEXT_STRING);
        if (footerString != null && !footerString.isEmpty())
            mFooterString.setText(footerString);
        else {
            mFooterString.setText("YAAP");
            Settings.System.putString(resolver,
                    Settings.System.QS_FOOTER_TEXT_STRING, "YAAP");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBrightnessSlider == null) return;
        final boolean enabled = Settings.Secure.getInt(
                getActivity().getContentResolver(),
                BRIGHTNESS_SLIDER, 1) == 1;
        mBrightnessSlider.setChecked(enabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFooterString) {
            String value = (String) newValue;
            if (value != null && !value.isEmpty())
                Settings.System.putString(resolver,
                        Settings.System.QS_FOOTER_TEXT_STRING, value);
            else {
                mFooterString.setText("YAAP");
                Settings.System.putString(resolver,
                        Settings.System.QS_FOOTER_TEXT_STRING, "YAAP");
            }
            return true;
        } else if (preference == mBrightnessSlider) {
            Boolean value = (Boolean) newValue;
            Settings.Secure.putInt(resolver,
                    BRIGHTNESS_SLIDER, value ? 1 : 0);
            return true;
        } else if (preference == mShadeBlurRadiusPref) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver, SHADE_BLUR_RADIUS,
                    value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mShadeScrimAlphaPref) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(resolver, SHADE_SCRIM_ALPHA,
                    value, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.YASP;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.yaap_settings_quicksettings);
}
