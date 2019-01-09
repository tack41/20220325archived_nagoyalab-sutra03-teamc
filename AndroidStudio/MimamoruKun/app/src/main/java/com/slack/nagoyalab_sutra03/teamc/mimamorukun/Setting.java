package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;

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

    private float _opticalThreshold;
    public float getOpticalThreshold(){return _opticalThreshold;}
    public void setOpticalThreshold(float opticalThreshold){_opticalThreshold = opticalThreshold;}

    private float _movementThreshold;
    public float getMovementThreshold(){return _movementThreshold;}
    public void setMovementThreshold(float gyroThreshold){_movementThreshold = gyroThreshold;}

    private static final String _STORAGE_NAME = "SettingStorageNameMimamoruKun";
    private static final String _KEY_USER_NAME = "SettingKeyUserName";
    private static final String _KEY_TEMPERATURE_UPPER_LIMIT = "SettingKeyTemperatureUpperLimit";
    private static final String _KEY_TEMPERATURE_LOWER_LIMIT = "SettingKeyTemperatureLowerLimit";
    private static final String _KEY_OPTICAL_THRESHOLD = "SettingKeyOpticalThreashold";
    private static final String _KEY_MOVEMENT_THRESHOLD = "SettingKeyMovementThreshold";

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
        editor.putFloat(_KEY_OPTICAL_THRESHOLD, _opticalThreshold);
        editor.putFloat(_KEY_MOVEMENT_THRESHOLD, _movementThreshold);
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
        retVal.setOpticalThreshold(prefs.getFloat(_KEY_OPTICAL_THRESHOLD, 30.0f));
        retVal.setMovementThreshold(prefs.getFloat(_KEY_MOVEMENT_THRESHOLD, 30.0f));
        return retVal;
    }

    public void putToIntent(Intent intent){
        intent.putExtra(_STORAGE_NAME, _STORAGE_NAME);
        intent.putExtra(_KEY_USER_NAME, _userName);
        intent.putExtra(_KEY_TEMPERATURE_UPPER_LIMIT, _temperatureUpperLimit);
        intent.putExtra(_KEY_TEMPERATURE_LOWER_LIMIT, _temperatureLowerLimit);
        intent.putExtra(_KEY_OPTICAL_THRESHOLD, _opticalThreshold);
        intent.putExtra(_KEY_MOVEMENT_THRESHOLD, _movementThreshold);
    }

    public static Setting getFromIntent(Intent intent){
        Setting retVal = null;

        if(intent.getExtras() != null && intent.getExtras().size() > 0 && intent.getStringExtra(_STORAGE_NAME) != null){
            retVal = new Setting();
            retVal.setUserName(intent.getStringExtra(_KEY_USER_NAME));
            retVal.setTemperatureUpperLimit(intent.getFloatExtra(_KEY_TEMPERATURE_UPPER_LIMIT, 30.0f));
            retVal.setTemperatureLowerLimit(intent.getFloatExtra(_KEY_TEMPERATURE_LOWER_LIMIT, 10.0f));
            retVal.setOpticalThreshold(intent.getFloatExtra(_KEY_OPTICAL_THRESHOLD, 30.0f));
            retVal.setMovementThreshold(intent.getFloatExtra(_KEY_MOVEMENT_THRESHOLD, 30.0f));
        }

        return retVal;
    }
}

