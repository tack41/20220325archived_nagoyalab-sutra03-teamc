package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class EventStoreSQLite {

    //SQLiteDatabase.finalize実行時にcloseも呼び出されるので、明示的な呼び出しは不要
    SQLiteDatabase _db;

    public EventStoreSQLite(Context context){
        EventSQLiteOpenHelper helper = new EventSQLiteOpenHelper(context);
        _db = helper.getWritableDatabase();
    }

    public int insertEvent(Event event){
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);
        return insertEvent(eventList);
    }

    public int insertEvent(List<Event> eventList){
        int retVal = 0;

        _db.beginTransaction();
        try{
            final SQLiteStatement statement = _db.compileStatement("INSERT INTO events (event_type, content, occurred_date)VALUES (?, ?, ?)");
            try{
                for(int i=0; i<eventList.size(); i++){
                    Event event = eventList.get(i);
                    statement.bindString(1, event.getType().toValue());
                    statement.bindString(2, event.getContent());
                    statement.bindLong(3, event.occurredDate.getTime());

                    //戻り値が0以上(row id)ならば成功
                    if(0 <= statement.executeInsert()) retVal++;
                }
            }finally {
                statement.close();
            }
            _db.setTransactionSuccessful();
        }finally{
            _db.endTransaction();
        }

        return retVal;
    }

    public List<Event> getAllEvent(){
        List<Event> retVal = new ArrayList<>();

        Cursor cursor = _db.rawQuery("SELECT event_type,content,occurred_date FROM events", null);
        try{
            while(cursor.moveToNext()){
                Event event = new Event();
                event.setType(EventType.valueOf(cursor.getString(cursor.getColumnIndex("event_type"))));
                event.setContent(cursor.getString(cursor.getColumnIndex("content")));
                event.setOccurredDate(new java.util.Date(cursor.getLong(cursor.getColumnIndex("occurred_date"))));

                retVal.add(event);
            }
        }finally {
            cursor.close();
        }

/*        //初回のみサンプルデータを追加する。
        if(retVal.size() == 0){
            addInitialEvent(retVal);
        }
*/

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
}