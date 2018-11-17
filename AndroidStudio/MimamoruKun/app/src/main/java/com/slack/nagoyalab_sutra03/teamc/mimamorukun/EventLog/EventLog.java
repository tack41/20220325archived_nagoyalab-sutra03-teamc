package com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog;

import java.util.Date;

public class EventLog
{
    //イベント種別
    public EventLogType eventLogType;
    public void setType(EventLogType eventLogType){ this.eventLogType = eventLogType; }
    public EventLogType getType(){ return this.eventLogType; }

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

