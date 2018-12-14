package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import android.content.Intent;

import java.util.Date;

public class TemperatureEvent {

    //正常かどうか(True: 温度異常が検知されなくなった, False: 温度異常が検知された)
    private boolean _normal;
    public boolean isNormal(){
        return _normal;
    }

    //発生日時
    Date _occurredDate;
    public Date getOccurredDate(){
        return _occurredDate;
    }

    private float _temperature;
    public float getTemperature(){
        return _temperature;
    }

    private String _message;
    public String getMessage(){
        return _message;
    }

    public TemperatureEvent(boolean normal, Date occurredDate, float temperature, String message){
        _normal = normal;
        _occurredDate = occurredDate;
        _temperature = temperature;
        _message = message;
    }

    private static final String _INTENT_KEY_EVENT_TYPE = "EVENT_TYPE";
    private static final String _INTENT_VALUE_EVENT_TYPE_TEMPERATURE = "EVENT_TYPE_TEMPERATURE";

    private static final String _INTENT_KEY_TEMPERATUREEVENT_NORMAL = "TEMPERATUREEVENT_NORMAL";
    private static final String _INTENT_KEY_TEMPERATUREEVENT_OCCURRED_DATE = "TEMPERATUREEVENT_OCCURRED_DATE";
    private static final String _INTENT_KEY_TEMPERATUREEVENT_TEMPERATURE = "TEMPERATUREEVENT_TEMPERATURE";
    private static final String _INTENT_KEY_TEMPERATUREEVENT_MESSAGE = "TEMPERATUREEVENT_MESSAGE";

    /**
     * put TemperatureEvent instance to Intent.
     * to get, use getLightEventFromIntent
     */
    public void putToIntent(Intent intent){
        intent.putExtra(_INTENT_KEY_EVENT_TYPE, _INTENT_VALUE_EVENT_TYPE_TEMPERATURE);
        intent.putExtra(_INTENT_KEY_TEMPERATUREEVENT_NORMAL, _normal);
        intent.putExtra(_INTENT_KEY_TEMPERATUREEVENT_OCCURRED_DATE, _occurredDate.getTime());
        intent.putExtra(_INTENT_KEY_TEMPERATUREEVENT_TEMPERATURE, _temperature);
        intent.putExtra(_INTENT_KEY_TEMPERATUREEVENT_MESSAGE, _message);
    }

    public static TemperatureEvent getFromIntent(Intent intent){
        TemperatureEvent retVal = null;

        if(intent.getExtras() != null && intent.getExtras().size() > 0 &&
                _INTENT_VALUE_EVENT_TYPE_TEMPERATURE.equals(intent.getStringExtra(_INTENT_KEY_EVENT_TYPE))){

            boolean normal = intent.getBooleanExtra(_INTENT_KEY_TEMPERATUREEVENT_NORMAL, false);
            Date occurredDate = new Date(intent.getLongExtra(_INTENT_KEY_TEMPERATUREEVENT_OCCURRED_DATE, 0));
            float temperature = intent.getFloatExtra(_INTENT_KEY_TEMPERATUREEVENT_TEMPERATURE, 0.0f);
            String message = intent.getStringExtra(_INTENT_KEY_TEMPERATUREEVENT_MESSAGE);
            retVal = new TemperatureEvent(normal, occurredDate, temperature, message);
        }

        return retVal;
    }
}
