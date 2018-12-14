package com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog;

import android.content.Intent;

import java.util.Date;

public class EventLog
{
    //イベント種別
    private EventLogType _eventLogType;
    public void setType(EventLogType eventLogType){ _eventLogType = eventLogType; }
    public EventLogType getType(){ return _eventLogType; }

    //イベント内容
    private String _content;
    public void setContent(String content){
        _content = content;
    }
    public String getContent(){
        return _content;
    }

    //発生日時
    private Date _occurredDate;
    public void setOccurredDate(Date occurredDate){
        _occurredDate = occurredDate;
    }
    public Date getOccurredDate(){
        return _occurredDate;
    }

    /**
     * EventクラスのオブジェクトをIntentにputします。
     * 取り出すにはgetEventFromIntentを使用します。
     */
    public void putToIntent(Intent intent){
        intent.putExtra("EVENT_TYPE", _eventLogType);
        intent.putExtra("EVENT_CONTENT", _content);
        intent.putExtra("EVENT_OCCURRED_DATE", _occurredDate.getTime());
    }

    public static EventLog getFromIntent(Intent intent){
        EventLog retVal = null;

        if(intent.getExtras() != null && intent.getExtras().size() > 0){
            retVal = new EventLog();

            retVal.setType((EventLogType)intent.getSerializableExtra("EVENT_TYPE"));
            retVal.setContent(intent.getStringExtra("EVENT_CONTENT"));
            retVal.setOccurredDate(new java.util.Date(intent.getLongExtra("EVENT_OCCURRED_DATE", 0)));
        }

        return retVal;
    }

}

