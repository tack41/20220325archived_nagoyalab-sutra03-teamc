package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.TemperatureEvent;

import java.util.Date;

/**
 * Manage settings of this application
 */
public class Setting {
    private String _userName;
    public String getUserName(){return _userName;}
    public void setUserName(String userName){_userName = userName;}

    private float _temperatureUpperLimit;
    public float getTemperatureUpperLimit(){return _temperatureUpperLimit;}
    public void setTemperatureUpperLimit(float temperatureUpperLimit){_temperatureUpperLimit = temperatureUpperLimit;}

    private float _temperatureLowerLimit;
    public float getTemperatureLowerLimit(){return _temperatureLowerLimit;}
    public void setTemperatureLowerLimit(float temperatureLowerLimit){_temperatureLowerLimit = temperatureLowerLimit;}

    // Measurement interval(s)
    private int _measurementInterval;
    public int getMeasurementInterval(){return _measurementInterval;};
    public void setMeasurementInterval(int measurementInterval){_measurementInterval = measurementInterval;}

    private static final String _STORAGE_NAME = "SettingStorageNameMimamoruKun";
    private static final String _KEY_USER_NAME = "SettingKeyUserName";
    private static final String _KEY_TEMPERATURE_UPPER_LIMIT = "SettingKeyTemperatureUpperLimit";
    private static final String _KEY_TEMPERATURE_LOWER_LIMIT = "SettingKeyTemperatureLowerLimit";
    private static final String _KEY_MEASUREMENT_INTERVAL = "SettingKeyMeasurementInterval";

    /**
     * Save all settings to permanent storage.
     */
    public void save()
    {
        SharedPreferences prefs = MyApplication.getInstance().getSharedPreferences(_STORAGE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(_KEY_USER_NAME, _userName);
        editor.putFloat(_KEY_TEMPERATURE_UPPER_LIMIT, _temperatureUpperLimit);
        editor.putFloat(_KEY_TEMPERATURE_LOWER_LIMIT, _temperatureLowerLimit);
        editor.putInt(_KEY_MEASUREMENT_INTERVAL, _measurementInterval);
        editor.apply();
    }

    /**
     * Load all settings from permanent storage.
     * @return
     */
    public static Setting load(){
        SharedPreferences prefs = MyApplication.getInstance().getSharedPreferences(_STORAGE_NAME, Context.MODE_PRIVATE);
        if(prefs == null) return null;

        Setting retVal = new Setting();
        retVal.setUserName(prefs.getString(_KEY_USER_NAME,""));
        retVal.setTemperatureUpperLimit(prefs.getFloat(_KEY_TEMPERATURE_UPPER_LIMIT, 30.0f));
        retVal.setTemperatureLowerLimit(prefs.getFloat(_KEY_TEMPERATURE_LOWER_LIMIT, 10.0f));
        retVal.setMeasurementInterval(prefs.getInt(_KEY_MEASUREMENT_INTERVAL, 30));

        return retVal;
    }

    public void putToIntent(Intent intent){
        intent.putExtra(_STORAGE_NAME, _STORAGE_NAME);
        intent.putExtra(_KEY_USER_NAME, _userName);
        intent.putExtra(_KEY_TEMPERATURE_UPPER_LIMIT, _temperatureUpperLimit);
        intent.putExtra(_KEY_TEMPERATURE_LOWER_LIMIT, _temperatureLowerLimit);
        intent.putExtra(_KEY_MEASUREMENT_INTERVAL, _measurementInterval);
    }

    public static Setting getFromIntent(Intent intent){
        Setting retVal = null;

        if(intent.getExtras() != null && intent.getExtras().size() > 0 && intent.getStringExtra(_STORAGE_NAME) != null){
            retVal = new Setting();
            retVal.setUserName(intent.getStringExtra(_KEY_USER_NAME));
            retVal.setTemperatureUpperLimit(intent.getFloatExtra(_KEY_TEMPERATURE_UPPER_LIMIT, 30.0f));
            retVal.setTemperatureLowerLimit(intent.getFloatExtra(_KEY_TEMPERATURE_LOWER_LIMIT, 10.0f));
            retVal.setMeasurementInterval(intent.getIntExtra(_KEY_MEASUREMENT_INTERVAL, 30));
        }

        return retVal;
    }
}

