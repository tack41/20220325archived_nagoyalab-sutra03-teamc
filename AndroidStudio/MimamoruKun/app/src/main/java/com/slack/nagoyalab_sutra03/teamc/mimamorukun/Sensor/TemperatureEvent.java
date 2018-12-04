package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

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

    private double _temperature;
    public double getTemperature(){
        return _temperature;
    }

    private String _message;
    public String getMessage(){
        return _message;
    }

    public TemperatureEvent(boolean normal, Date occurredDate, double temperature, String message){
        _normal = normal;
        _occurredDate = occurredDate;
        _temperature = temperature;
        _message = message;
    }
}
