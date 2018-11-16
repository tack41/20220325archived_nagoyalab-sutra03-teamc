package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event;

import java.util.GregorianCalendar;
import java.util.List;
import android.content.Intent;
import android.content.Context;

/*
  Eventデータを取り扱うクラス
 */
public class EventUtility {
    /*
      EventクラスのオブジェクトをIntentにputします。
      取り出すにはgetEventFromIntentを使用します。
     */
    public static void putEventToIntent(Intent intent, Event event){
        intent.putExtra("EVENT_TYPE", event.getType());
        intent.putExtra("EVENT_CONTENT", event.getContent());
        intent.putExtra("EVENT_OCCURRED_DATE", event.getOccurredDate().getTime());
    }

    public static Event getEventFromIntent(Intent intent){
        Event retVal = null;

        if(intent.getExtras() != null && intent.getExtras().size() > 0){
            retVal = new Event();

            retVal.setType((EventType)intent.getSerializableExtra("EVENT_TYPE"));
            retVal.setContent(intent.getStringExtra("EVENT_CONTENT"));
            retVal.setOccurredDate(new java.util.Date(intent.getLongExtra("EVENT_OCCURRED_DATE", 0)));
        }

        return retVal;
    }
}
