package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import android.content.Intent;

import java.util.Date;

/**
 * Collection of utility method for Events.
 */
public class EventUtility {
    /**
     * Singleton
     */
    private EventUtility(){
        throw new AssertionError();
    }

    private static final String _INTENT_KEY_EVENT_TYPE = "EVENT_TYPE";
    private static final String _INTENT_VALUE_EVENT_TYPE_LIGHT = "EVENT_TYPE_LIGHT";
    private static final String _INTENT_VALUE_EVENT_TYPE_SWING = "EVENT_TYPE_SWING";
    private static final String _INTENT_VALUE_EVENT_TYPE_TEMPERATURE = "EVENT_TYPE_TEMPERATURE";

    private static final String _INTENT_KEY_LIGHTEVENT_NORMAL = "LIGHTEVENT_NORMAL";
    private static final String _INTENT_KEY_LIGHTEVENT_OCCURRED_DATE = "LIGHTEVENT_OCCURRED_DATE";
    private static final String _INTENT_KEY_LIGHTEVENT_MESSAGE = "LIGHTEVENT_MESSAGE";

    private static final String _INTENT_KEY_SWINGEVENT_NORMAL = "SWINGEVENT_NORMAL";
    private static final String _INTENT_KEY_SWINGEVENT_OCCURRED_DATE = "SWINGEVENT_OCCURRED_DATE";
    private static final String _INTENT_KEY_SWINGEVENT_MESSAGE = "SWINGEVENT_MESSAGE";

    private static final String _INTENT_KEY_TEMPERATUREEVENT_NORMAL = "TEMPERATUREEVENT_NORMAL";
    private static final String _INTENT_KEY_TEMPERATUREEVENT_OCCURRED_DATE = "TEMPERATUREEVENT_OCCURRED_DATE";
    private static final String _INTENT_KEY_TEMPERATUREEVENT_TEMPERATURE = "TEMPERATUREEVENT_TEMPERATURE";
    private static final String _INTENT_KEY_TEMPERATUREEVENT_MESSAGE = "TEMPERATUREEVENT_MESSAGE";

    /**
     * put LightEvent instance to Intent.
     * to get, use getLightEventFromIntent
     */
    public static void putEventToIntent(Intent intent, LightEvent event){
        intent.putExtra(_INTENT_KEY_EVENT_TYPE, _INTENT_VALUE_EVENT_TYPE_LIGHT);
        intent.putExtra(_INTENT_KEY_LIGHTEVENT_NORMAL, event.isNormal());
        intent.putExtra(_INTENT_KEY_LIGHTEVENT_OCCURRED_DATE, event.getOccurredDate().getTime());
        intent.putExtra(_INTENT_KEY_LIGHTEVENT_MESSAGE, event.getMessage());
    }

    /**
     * put SwingEvent instance to Intent.
     * to get, use getLightEventFromIntent
     */
    public static void putEventToIntent(Intent intent, SwingEvent event){
        intent.putExtra(_INTENT_KEY_EVENT_TYPE, _INTENT_VALUE_EVENT_TYPE_SWING);
        intent.putExtra(_INTENT_KEY_SWINGEVENT_NORMAL, event.isNormal());
        intent.putExtra(_INTENT_KEY_SWINGEVENT_OCCURRED_DATE, event.getOccurredDate().getTime());
        intent.putExtra(_INTENT_KEY_SWINGEVENT_MESSAGE, event.getMessage());
    }

    /**
     * put TemperatureEvent instance to Intent.
     * to get, use getLightEventFromIntent
     */
    public static void putEventToIntent(Intent intent, TemperatureEvent event){
        intent.putExtra(_INTENT_KEY_EVENT_TYPE, _INTENT_VALUE_EVENT_TYPE_TEMPERATURE);
        intent.putExtra(_INTENT_KEY_TEMPERATUREEVENT_NORMAL, event.isNormal());
        intent.putExtra(_INTENT_KEY_TEMPERATUREEVENT_OCCURRED_DATE, event.getOccurredDate().getTime());
        intent.putExtra(_INTENT_KEY_TEMPERATUREEVENT_TEMPERATURE, event.getTemperature());
        intent.putExtra(_INTENT_KEY_LIGHTEVENT_MESSAGE, event.getMessage());
    }

    public static LightEvent getLightEventFromIntent(Intent intent){
        LightEvent retVal = null;

        if(intent.getExtras() != null && intent.getExtras().size() > 0 &&
                _INTENT_VALUE_EVENT_TYPE_LIGHT.equals(intent.getStringExtra(_INTENT_KEY_EVENT_TYPE))){

            boolean normal = intent.getBooleanExtra(_INTENT_KEY_LIGHTEVENT_NORMAL, false);
            Date occurredDate = new Date(intent.getLongExtra(_INTENT_KEY_LIGHTEVENT_OCCURRED_DATE, 0));
            String message = intent.getStringExtra(_INTENT_KEY_LIGHTEVENT_MESSAGE);
            retVal = new LightEvent(normal, occurredDate, message);
        }

        return retVal;
    }

    public static SwingEvent getSwingEventFromIntent(Intent intent){
        SwingEvent retVal = null;

        if(intent.getExtras() != null && intent.getExtras().size() > 0&&
                _INTENT_VALUE_EVENT_TYPE_SWING.equals(intent.getStringExtra(_INTENT_KEY_EVENT_TYPE))){

            boolean normal = intent.getBooleanExtra(_INTENT_KEY_SWINGEVENT_NORMAL, false);
            Date occurredDate = new Date(intent.getLongExtra(_INTENT_KEY_SWINGEVENT_OCCURRED_DATE, 0));
            String message = intent.getStringExtra(_INTENT_KEY_SWINGEVENT_MESSAGE);
            retVal = new SwingEvent(normal, occurredDate, message);
        }

        return retVal;
    }

    public static TemperatureEvent getTemperatureEventFromIntent(Intent intent){
        TemperatureEvent retVal = null;

        if(intent.getExtras() != null && intent.getExtras().size() > 0 &&
                _INTENT_VALUE_EVENT_TYPE_TEMPERATURE.equals(intent.getStringExtra(_INTENT_KEY_EVENT_TYPE))){

            boolean normal = intent.getBooleanExtra(_INTENT_KEY_TEMPERATUREEVENT_NORMAL, false);
            Date occurredDate = new Date(intent.getLongExtra(_INTENT_KEY_TEMPERATUREEVENT_OCCURRED_DATE, 0));
            double temperature = intent.getDoubleExtra(_INTENT_KEY_TEMPERATUREEVENT_TEMPERATURE, 0.0);
            String message = intent.getStringExtra(_INTENT_KEY_TEMPERATUREEVENT_MESSAGE);
            retVal = new TemperatureEvent(normal, occurredDate, temperature, message);
        }

        return retVal;
    }
}
