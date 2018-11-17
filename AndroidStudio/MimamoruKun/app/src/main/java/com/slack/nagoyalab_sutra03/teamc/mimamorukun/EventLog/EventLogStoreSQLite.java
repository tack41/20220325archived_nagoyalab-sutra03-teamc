package com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class EventLogStoreSQLite {

    //SQLiteDatabase.finalize実行時にcloseも呼び出されるので、明示的な呼び出しは不要
    SQLiteDatabase _db;

    public EventLogStoreSQLite(Context context){
        EventLogSQLiteOpenHelper helper = new EventLogSQLiteOpenHelper(context);
        _db = helper.getWritableDatabase();
    }

    public int insertEvent(EventLog eventLog){
        List<EventLog> eventLogList = new ArrayList<>();
        eventLogList.add(eventLog);
        return insertEvent(eventLogList);
    }

    public int insertEvent(List<EventLog> eventLogList){
        int retVal = 0;

        _db.beginTransaction();
        try{
            final SQLiteStatement statement = _db.compileStatement("INSERT INTO events (event_type, content, occurred_date)VALUES (?, ?, ?)");
            try{
                for(int i = 0; i< eventLogList.size(); i++){
                    EventLog eventLog = eventLogList.get(i);
                    statement.bindString(1, eventLog.getType().toValue());
                    statement.bindString(2, eventLog.getContent());
                    statement.bindLong(3, eventLog.occurredDate.getTime());

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

    public List<EventLog> getAllEvent(){
        List<EventLog> retVal = new ArrayList<>();

        Cursor cursor = _db.rawQuery("SELECT event_type,content,occurred_date FROM events", null);
        try{
            while(cursor.moveToNext()){
                EventLog eventLog = new EventLog();
                eventLog.setType(EventLogType.valueOf(cursor.getString(cursor.getColumnIndex("event_type"))));
                eventLog.setContent(cursor.getString(cursor.getColumnIndex("content")));
                eventLog.setOccurredDate(new java.util.Date(cursor.getLong(cursor.getColumnIndex("occurred_date"))));

                retVal.add(eventLog);
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

    private static void addInitialEvent(List<EventLog> eventLogList){
        //1件目
        EventLog eventLog = new EventLog();
        eventLog = new EventLog();
        eventLog.setOccurredDate(new GregorianCalendar(2018, 11 - 1, 10, 23, 40, 32).getTime());
        eventLog.setType(EventLogType.TemperatureUnusual);
        eventLog.setContent("室温が32.5℃(30℃以上)に上がりました。");
        eventLogList.add(eventLog);

        //2件目
        eventLog = new EventLog();
        eventLog.setOccurredDate(new GregorianCalendar(2018, 11 - 1, 10, 23, 45, 43).getTime());
        eventLog.setType(EventLogType.TemperatureBecomeUsual);
        eventLog.setContent("室温が29.4℃(30℃以下)に下がりました。");
        eventLogList.add(eventLog);

        //3件目
        eventLog = new EventLog();
        eventLog.setOccurredDate(new GregorianCalendar(2018, 11 - 1, 11, 7, 0, 3).getTime());
        eventLog.setType(EventLogType.Light);
        eventLog.setContent("起床イベントが発生しました。");
        eventLogList.add(eventLog);

        //4件目
        eventLog = new EventLog();
        eventLog.setOccurredDate(new GregorianCalendar(2018, 11 - 1, 11, 7, 1, 53).getTime());
        eventLog.setType(EventLogType.LightHandled);
        eventLog.setContent("対応コメント: 確認しました。");
        eventLogList.add(eventLog);

        //5件目
        eventLog = new EventLog();
        eventLog.setOccurredDate(new GregorianCalendar(2018, 11 - 1, 11, 13, 30, 25).getTime());
        eventLog.setType(EventLogType.Swing);
        eventLog.setContent("振るイベントが発生しました。");
        eventLogList.add(eventLog);

        //6件目
        eventLog = new EventLog();
        eventLog.setOccurredDate(new GregorianCalendar(2018, 11 - 1, 11, 13, 40, 10).getTime());
        eventLog.setType(EventLogType.SwingHandled);
        eventLog.setContent("対応コメント: トイレに付き添いました。");
        eventLogList.add(eventLog);
    }
}