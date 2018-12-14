package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import android.content.Intent;

import java.util.Date;

public class LightEvent {

    //正常かどうか(True: 光が検知されなくなった, False: 光が検知された)
    private boolean _normal;

    public boolean isNormal() {
        return _normal;
    }

    //発生日時
    Date _occurredDate;

    public Date getOccurredDate() {
        return _occurredDate;
    }

    private String _message;

    public String getMessage() {
        return _message;
    }

    public LightEvent(boolean normal, Date occurredDate, String message) {
        _normal = normal;
        _occurredDate = occurredDate;
        _message = message;
    }

    private static final String _INTENT_KEY_EVENT_TYPE = "EVENT_TYPE";
    private static final String _INTENT_VALUE_EVENT_TYPE_LIGHT = "EVENT_TYPE_LIGHT";

    private static final String _INTENT_KEY_LIGHTEVENT_NORMAL = "LIGHTEVENT_NORMAL";
    private static final String _INTENT_KEY_LIGHTEVENT_OCCURRED_DATE = "LIGHTEVENT_OCCURRED_DATE";
    private static final String _INTENT_KEY_LIGHTEVENT_MESSAGE = "LIGHTEVENT_MESSAGE";

    /**
     * put LightEvent instance to Intent.
     * to get, use getLightEventFromIntent
     */
    public void putToIntent(Intent intent) {
        intent.putExtra(_INTENT_KEY_EVENT_TYPE, _INTENT_VALUE_EVENT_TYPE_LIGHT);
        intent.putExtra(_INTENT_KEY_LIGHTEVENT_NORMAL, _normal);
        intent.putExtra(_INTENT_KEY_LIGHTEVENT_OCCURRED_DATE, _occurredDate.getTime());
        intent.putExtra(_INTENT_KEY_LIGHTEVENT_MESSAGE, _message);
    }

    public static LightEvent getFromIntent(Intent intent){
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
}

