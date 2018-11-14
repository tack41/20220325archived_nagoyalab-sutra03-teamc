package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import java.util.Date;

public class EventHistory
{
    //発生日時
    Date occuredDate;
    public void setOccuredDate(Date occuredDate){
        this.occuredDate = occuredDate;
    }
    public Date getOccuredDate(){
        return this.occuredDate;
    }

    //イベント名
    String eventName;
    public void setEventName(String eventName){
        this.eventName = eventName;
    }
    public String getEventName(){
        return this.eventName;
    }

    //イベント内容
    String eventContents;
    public void setEventContents(String eventContents){
        this.eventContents = eventContents;
    }
    public String getEventContent(){
        return this.eventContents;
    }
}
