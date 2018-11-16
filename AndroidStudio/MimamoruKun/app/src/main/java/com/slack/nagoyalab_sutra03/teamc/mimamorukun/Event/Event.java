package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event;

import java.util.Date;

public class Event
{
    //イベント種別
    public EventType eventType;
    public void setType(EventType eventType){ this.eventType = eventType; }
    public EventType getType(){ return this.eventType; }

    //イベント内容
    String content;
    public void setContent(String content){
        this.content = content;
    }
    public String getContent(){
        return this.content;
    }

    //発生日時
    Date occurredDate;
    public void setOccurredDate(Date occurredDate){
        this.occurredDate = occurredDate;
    }
    public Date getOccurredDate(){
        return this.occurredDate;
    }

}

