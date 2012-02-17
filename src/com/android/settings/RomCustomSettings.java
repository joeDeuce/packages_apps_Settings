package com.android.settings;
import com.android.settings.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Spannable;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import android.widget.NumberPicker;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import com.android.internal.app.ShutdownThread;
import com.android.settings.utils.CMDProcessor;
import com.android.settings.utils.Helpers;

public class RomCustomSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String PREF_VOLUME_MUSIC = "volume_music_controls";
    private static final String QUAD_TARGETS = "pref_lockscreen_quad_targets";
    private static final String PREF_CLOCK_DISPLAY_STYLE = "clock_am_pm";
    private static final String PREF_CLOCK_STYLE = "clock_style";
    CheckBoxPreference mVolumeMusic;
    CheckBoxPreference mQuadTargets;
    private ListPreference mAmPmStyle;
    private ListPreference mClockStyle;

    private static final String PREF_ROTATION_ANIMATION = "rotation_animation_delay";
    ListPreference mAnimationRotationDelay;

    private static final String BATTERY_STYLE = "battery_style";
    private static final String PREF_BATT_BAR = "battery_bar_list";
    private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
    private static final String BATTERY_BAR_COLOR = "battery_bar_color";
    private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
    private static final String PREF_BATT_ANIMATE = "battery_bar_animate";
    private ListPreference mBatteryStyle;
    ListPreference mBattBar;
    ListPreference mBatteryBarStyle;
    ListPreference mBatteryBarThickness;
    CheckBoxPreference mBatteryBarChargingAnimation;
    private ColorPickerPreference mBattBarColor;

    ListPreference mLcdDensity;

    private static final String PREF_VOLUME_WAKE = "volume_wake";
    CheckBoxPreference mVolumeWake;
    
    private static final String NOTIFICATION_BUTTON_BACKLIGHT = "notification_button_backlight";
    private CheckBoxPreference mUseBLN;

    private static final String PREF_180 = "rotate_180";
    CheckBoxPreference mAllow180Rotation;

    private static final String PREF_CARRIER_TEXT = "carrier_text";
    private Preference mCarrier;
    String mCarrierText = null;

    private static final String PREF_BRIGHTNESS_TOGGLE = "status_bar_brightness_toggle";
    CheckBoxPreference mStatusBarBrightnessToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rom_custom_settings);
        PreferenceScreen prefSet = getPreferenceScreen();

        mVolumeMusic = (CheckBoxPreference) prefSet.findPreference(PREF_VOLUME_MUSIC);
        mVolumeMusic.setChecked(Settings.System.getInt(getActivity()
            .getContentResolver(), Settings.System.VOLUME_MUSIC_CONTROLS,
            0) == 1);

        mClockStyle = (ListPreference) prefSet.findPreference(PREF_CLOCK_STYLE);
        mAmPmStyle = (ListPreference) prefSet.findPreference(PREF_CLOCK_DISPLAY_STYLE);

        int styleValue = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_AM_PM, 2);
        mAmPmStyle.setValueIndex(styleValue);
        mAmPmStyle.setOnPreferenceChangeListener(this);

        int clockVal = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_CLOCK, 1);
        mClockStyle.setValueIndex(clockVal);
        mClockStyle.setOnPreferenceChangeListener(this);

	mBatteryStyle = (ListPreference) prefSet.findPreference(BATTERY_STYLE);
        int battVal = Settings.System.getInt(getContentResolver(),
                Settings.System.BATTERY_PERCENTAGES, 1);
        mBatteryStyle.setValueIndex(battVal);
        mBatteryStyle.setOnPreferenceChangeListener(this);

        mVolumeWake = (CheckBoxPreference) findPreference(PREF_VOLUME_WAKE);
        mVolumeWake.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.VOLUME_WAKE_SCREEN,	
                0) == 1);

        mBattBar = (ListPreference) findPreference(PREF_BATT_BAR);
        mBattBar.setOnPreferenceChangeListener(this);
        mBattBar.setValue((Settings.System.getInt(getActivity()
		.getContentResolver(), Settings.System.STATUSBAR_BATTERY_BAR,
		0)) + "");

	mBatteryBarStyle = (ListPreference) findPreference(PREF_BATT_BAR_STYLE);
        mBatteryBarStyle.setOnPreferenceChangeListener(this);
        mBatteryBarStyle.setValue((Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_BATTERY_BAR_STYLE,
                0)) + "");

	mBatteryBarChargingAnimation = (CheckBoxPreference) findPreference(PREF_BATT_ANIMATE);
        mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE,
                0) == 1);

        mBatteryBarThickness = (ListPreference) findPreference(PREF_BATT_BAR_WIDTH);
        mBatteryBarThickness.setOnPreferenceChangeListener(this);
        mBatteryBarThickness.setValue((Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS,
                1)) + "");

        mBattBarColor = (ColorPickerPreference) prefSet.findPreference(BATTERY_BAR_COLOR);
        mBattBarColor.setOnPreferenceChangeListener(this);

        mCarrier = (Preference) prefSet.findPreference(PREF_CARRIER_TEXT);
        updateCarrierText();

	mAllow180Rotation = (CheckBoxPreference) findPreference(PREF_180);
        mAllow180Rotation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
            Settings.System.ACCELEROMETER_ROTATION_ANGLES, (1 | 2 | 8)) == (1 | 2 | 4 | 8));

	mUseBLN = (CheckBoxPreference) prefSet.findPreference(NOTIFICATION_BUTTON_BACKLIGHT);
	mUseBLN.setChecked(Settings.System.getInt(getContentResolver(),
            Settings.System.NOTIFICATION_USE_BUTTON_BACKLIGHT, 0) == 1);

	mAnimationRotationDelay = (ListPreference) findPreference(PREF_ROTATION_ANIMATION);
            mAnimationRotationDelay.setOnPreferenceChangeListener(this);
            mAnimationRotationDelay.setValue(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME,
                    50) + "");

	String currentProperty = SystemProperties.get("ro.sf.lcd_density");
        if (currentProperty == null)
            currentProperty = "0";
        mLcdDensity = (ListPreference) findPreference("lcd_density");
        mLcdDensity.setSummary(currentProperty);
        mLcdDensity.setOnPreferenceChangeListener(this);
        mLcdDensity.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME,
                Integer.parseInt(currentProperty)) + "");

	mStatusBarBrightnessToggle = (CheckBoxPreference) findPreference(PREF_BRIGHTNESS_TOGGLE);
        mStatusBarBrightnessToggle.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE,
                1) == 1);
    }

    private void updateBatteryBarToggle(boolean bool){
        if (bool)
            mBattBarColor.setEnabled(true);
        else
            mBattBarColor.setEnabled(false);
    }

    private void updateCarrierText() {
        mCarrierText = Settings.System.getString(getContentResolver(), Settings.System.CUSTOM_CARRIER_TEXT);
        if (mCarrierText == null) {
            mCarrier.setSummary("Upon changing you will need to data wipe to get back stock. Requires reboot.");
        } else {
            mCarrier.setSummary(mCarrierText);
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
	if (preference == mVolumeMusic) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.VOLUME_MUSIC_CONTROLS,
		((CheckBoxPreference) preference).isChecked() ? 1 : 0);
	    return true;
	} else if (preference == mVolumeWake) {
            Settings.System.putInt(getActivity().getContentResolver(),
            Settings.System.VOLUME_WAKE_SCREEN,
                ((CheckBoxPreference) preference).isChecked() ? 1 : 0);	
            return true;
        } else if (preference == mAllow180Rotation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION_ANGLES, checked ? (1 | 2 | 4 | 8)
                    : (1 | 2 | 8));
            return true;
	} else if (preference == mUseBLN) {
            value = mUseBLN.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.NOTIFICATION_USE_BUTTON_BACKLIGHT, value ? 1 : 0);
            return true;
	} else if (preference == mStatusBarBrightnessToggle) {
            Log.e("LOL", "m");
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true; 
	} else if (preference == mCarrier) {
            AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
            ad.setTitle("Custom Carrier Text");
            ad.setMessage("Enter new carrier text here");
            final EditText text = new EditText(getActivity());
            text.setText(mCarrierText != null ? mCarrierText : "");
            ad.setView(text);
            ad.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) text.getText()).toString();
                    Settings.System.putString(getActivity().getContentResolver(), Settings.System.CUSTOM_CARRIER_TEXT, value);
                    updateCarrierText();
                }
            });
            ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            ad.show();
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAmPmStyle) {
            int statusBarAmPm = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                Settings.System.STATUS_BAR_AM_PM, statusBarAmPm);
            return true;
	} else if (preference == mBattBar) {
            int val = Integer.parseInt((String) newValue);
            return Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR, val);
        } else if (preference == mBatteryBarStyle) {
            int val = Integer.parseInt((String) newValue);
            return Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_STYLE, val);
        } else if (preference == mBatteryBarThickness) {
            int val = Integer.parseInt((String) newValue);
            return Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, val);
	} else if (preference == mBatteryBarChargingAnimation) {
            Settings.System.putInt(getActivity().getContentResolver(),
	    Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE,
                ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
                return true;
        } else if (preference == mClockStyle) {
            int val = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                Settings.System.STATUS_BAR_CLOCK, val);
            return true;
	} else if (preference == mBatteryStyle) {
            int val = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                Settings.System.BATTERY_PERCENTAGES, val);
            return true;
        } else if (preference == mBattBarColor) {
            String hexColor = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hexColor);
            int color = ColorPickerPreference.convertToColorInt(hexColor);
            Settings.System.putInt(getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_COLOR, color);
            return true;
	} else if (preference == mLcdDensity) {
            Helpers.getMount("rw");
            new CMDProcessor().su.runWaitFor("busybox sed -i 's|ro.sf.lcd_density=.*|"
                    + "ro.sf.lcd_density" + "=" + newValue + "|' " + "/system/build.prop");
            Helpers.getMount("ro");
            Toast.makeText(getActivity().getApplicationContext(), "Reboot to see changes",
                    Toast.LENGTH_LONG).show();
            preference.setSummary((String) newValue);
            return true;
	} else if (preference == mAnimationRotationDelay) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME,
                    Integer.parseInt((String) newValue));
            return true;
        }

        return false;
    }

}

