package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

public class SwingEvent {

    //正常かどうか(True: 振動が検知されなくなった, False: 振動が検知された)
    private boolean is_normal;
    public boolean isNormal(){
        return this.is_normal;
    }

    private String message;
    public String getMessage(){
        return this.message;
    }

    public SwingEvent(boolean isNormal, String message){
        this.is_normal = isNormal;
        this.message = message;
    }
}
