package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import java.util.Date;

/**
 * This event fired when sensor measured values.
 */
public class MeasuredEvent {

    Date _measuredDate;
    public Date getMeasuredDate(){
        return _measuredDate;
    }

    private double _temperature;
    public double getTemperature(){
        return _temperature;
    }

    public MeasuredEvent(Date measuredDate, double temperature){
        _measuredDate = measuredDate;
        _temperature = temperature;
    }
}
