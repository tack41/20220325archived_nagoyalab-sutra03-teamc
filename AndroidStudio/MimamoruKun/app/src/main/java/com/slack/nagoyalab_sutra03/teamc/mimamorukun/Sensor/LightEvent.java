package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import java.util.Date;

public class LightEvent {

    //正常かどうか(True: 光が検知されなくなった, False: 光が検知された)
    private boolean _normal;
    public boolean isNormal(){
        return _normal;
    }

    //発生日時
    Date _occurredDate;
    public Date getOccurredDate(){
        return _occurredDate;
    }

    private String _message;
    public String getMessage(){
        return _message;
    }

    public LightEvent(boolean normal, Date occurredDate, String message){
        _normal = normal;
        _occurredDate = occurredDate;
        _message = message;
    }
}
