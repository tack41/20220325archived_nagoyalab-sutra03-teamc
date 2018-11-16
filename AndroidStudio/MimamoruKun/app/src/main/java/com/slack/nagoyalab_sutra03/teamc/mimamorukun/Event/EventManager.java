package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event;

import java.util.GregorianCalendar;
import java.util.List;
import android.content.Intent;
import android.content.Context;

/*
  Eventデータを取り扱うクラス
 */
public class EventManager {

    private EventStoreSQLite _store;

    public EventManager(Context context){
        _store = new EventStoreSQLite(context);
    }

    public List<Event> getAllEvent() {
        List<Event> retVal;
        retVal = _store.getAllEvent();

        //初回のみサンプルデータを追加する。
        if(retVal.size() == 0){
            addInitialEvent(retVal);
        }

        return retVal;
    }

    private static void addInitialEvent(List<Event> eventList){
        //1件目
        Event event = new Event();
        event = new Event();
        event.setOccurredDate(new GregorianCalendar(2018, 11 - 1, 10, 23, 40, 32).getTime());
        event.setType(EventType.TemperatureUnusual);
        event.setContent("室温が32.5℃(30℃以上)に上がりました。");
        eventList.add(event);

        //2件目
        event = new Event();
        event.setOccurredDate(new GregorianCalendar(2018, 11 - 1, 10, 23, 45, 43).getTime());
        event.setType(EventType.TemperatureBecomeUsual);
        event.setContent("室温が29.4℃(30℃以下)に下がりました。");
        eventList.add(event);

        //3件目
        event = new Event();
        event.setOccurredDate(new GregorianCalendar(2018, 11 - 1, 11, 7, 0, 3).getTime());
        event.setType(EventType.Light);
        event.setContent("起床イベントが発生しました。");
        eventList.add(event);

        //4件目
        event = new Event();
        event.setOccurredDate(new GregorianCalendar(2018, 11 - 1, 11, 7, 1, 53).getTime());
        event.setType(EventType.LightHandled);
        event.setContent("対応コメント: 確認しました。");
        eventList.add(event);

        //5件目
        event = new Event();
        event.setOccurredDate(new GregorianCalendar(2018, 11 - 1, 11, 13, 30, 25).getTime());
        event.setType(EventType.Swing);
        event.setContent("振るイベントが発生しました。");
        eventList.add(event);

        //6件目
        event = new Event();
        event.setOccurredDate(new GregorianCalendar(2018, 11 - 1, 11, 13, 40, 10).getTime());
        event.setType(EventType.SwingHandled);
        event.setContent("対応コメント: トイレに付き添いました。");
        eventList.add(event);
    }

    public void insertEvent(Event event){
        _store.insertEvent(event);
    }

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
