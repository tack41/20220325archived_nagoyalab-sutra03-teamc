package com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog;

import android.content.Intent;

/*
  Eventデータを取り扱うクラス
 */
public class EventLogUtility {

    /*
    本クラスの処理は全てstaticのため、インスタンス化させない
     */
    private EventLogUtility(){
        throw new AssertionError();
    }

    /*
      EventクラスのオブジェクトをIntentにputします。
      取り出すにはgetEventFromIntentを使用します。
     */
    public static void putEventToIntent(Intent intent, EventLog eventLog){
        intent.putExtra("EVENT_TYPE", eventLog.getType());
        intent.putExtra("EVENT_CONTENT", eventLog.getContent());
        intent.putExtra("EVENT_OCCURRED_DATE", eventLog.getOccurredDate().getTime());
    }

    public static EventLog getEventFromIntent(Intent intent){
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
