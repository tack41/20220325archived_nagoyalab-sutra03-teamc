package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

public class LightEvent {

    //正常かどうか(True: 光が検知されなくなった, False: 光が検知された)
    private boolean is_normal;
    public boolean isNormal(){
        return this.is_normal;
    }

    private String message;
    public String getMessage(){
        return this.message;
    }

    public LightEvent(boolean isNormal, String message){
        this.is_normal = isNormal;
        this.message = message;
    }
}
