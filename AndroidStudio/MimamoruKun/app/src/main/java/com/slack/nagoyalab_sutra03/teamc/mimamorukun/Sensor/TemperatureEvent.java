package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

public class TemperatureEvent {

    //正常かどうか(True: 温度異常が検知されなくなった, False: 温度異常が検知された)
    private boolean is_normal;
    public boolean isNormal(){
        return this.is_normal;
    }

    private double temperature;
    public double getTemperature(){
        return this.temperature;
    }
    private String message;
    public String getMessage(){
        return this.message;
    }

    public TemperatureEvent(boolean isNormal, double temperature, String message){
        this.is_normal = isNormal;
        this.temperature = temperature;
        this.message = message;
    }
}
