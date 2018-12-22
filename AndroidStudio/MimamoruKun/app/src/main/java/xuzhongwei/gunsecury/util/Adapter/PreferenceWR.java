package xuzhongwei.gunsecury.util.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferenceWR {

    public static final String PREFERENCEWR_NEEDS_REFRESH = "refresh";
    private SharedPreferences sharedPreferences;
    private String prefix;

    public PreferenceWR(String BluetoothAddress,Context con) {
        super();
        this.prefix = BluetoothAddress;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(con);
        this.setBooleanPreference("Exists",true);
        Log.d("PreferenceWR","Instantiated a new preference reader/writer with prefix : \"" + this.prefix + "_\"");
    }

    public static boolean isKnown(String BluetoothAddress,Context con) {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(con);
        return s.getBoolean(BluetoothAddress + "_" + "Exists",false);
    }

    /* String settings */
    public String getStringPreference(String prefName) {
        String defaultValue = "NS";
        return this.sharedPreferences.getString(this.prefix + "_" + prefName, defaultValue);
    }

    public boolean setStringPreference(String prefName, String prefValue) {
        SharedPreferences.Editor ed = this.sharedPreferences.edit();
        ed.putString(this.prefix + "_" + prefName, prefValue);
        return ed.commit();
    }

    /* Boolean settings */

    public boolean getBooleanPreference(String prefName) {
        boolean defaultValue = false;
        return this.sharedPreferences.getBoolean(this.prefix + "_" + prefName, defaultValue);
    }

    public boolean setBooleanPreference(String prefName, boolean prefValue) {
        SharedPreferences.Editor ed = this.sharedPreferences.edit();
        ed.putBoolean(this.prefix + "_" + prefName, prefValue);
        return ed.commit();
    }

    /* Integer settings */

    public int getIntegerPreference(String prefName) {
        int defaultValue = -1;
        return this.sharedPreferences.getInt(this.prefix + "_" + prefName, -1);
    }

    public boolean setIntegerPreference(String prefName,int prefValue) {
        SharedPreferences.Editor ed = this.sharedPreferences.edit();
        ed.putInt(this.prefix + "_" + prefName, prefValue);
        return ed.commit();
    }

}
