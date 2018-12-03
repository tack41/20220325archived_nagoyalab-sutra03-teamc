package com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog;

import android.app.Service;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.IBinder;
import android.os.Binder;
import android.util.Log;
import android.content.Intent;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.MyApplication;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.LightEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SwingEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.TemperatureEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to manage load and store event logs.
 */
public class EventLogStoreService extends Service {
    private final static String TAB = "EventLogStoreService";

    EventLogSQLiteOpenHelper _helper;
    SQLiteDatabase _db;

    private final IBinder _binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public EventLogStoreService getService() {
            return EventLogStoreService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        _helper = new EventLogSQLiteOpenHelper(MyApplication.getInstance());
        _db = _helper.getWritableDatabase();

        return _binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        _db.close();
        _db = null;
        _helper = null;
        Log.i(TAB,"onUnbind");

        return true;
    }

    public synchronized int insertEvent(LightEvent event){
        List<EventLog> eventLogList = new ArrayList<>();

        //Create EventLog object from event.
        EventLog eventLog = new EventLog();
        eventLog.setType(EventLogType.Light);
        eventLog.setContent("端末が光を検知しました");
        eventLog.setOccurredDate(new java.util.Date());

        eventLogList.add(eventLog);
        return insertEvent(eventLogList);
    }

    public synchronized int insertEvent(SwingEvent event){
        List<EventLog> eventLogList = new ArrayList<>();

        //Create EventLog object from event.
        EventLog eventLog = new EventLog();
        eventLog.setType(EventLogType.Swing);
        eventLog.setContent("端末が振動を検知しました");
        eventLog.setOccurredDate(new java.util.Date());

        eventLogList.add(eventLog);
        return insertEvent(eventLogList);
    }

    public synchronized int insertEvent(TemperatureEvent event){
        List<EventLog> eventLogList = new ArrayList<>();

        //Create EventLog object from event.
        EventLog eventLog = new EventLog();
        eventLog.setType(EventLogType.TemperatureUnusual);
        eventLog.setContent("温度異常を検知しました(" + event.getTemperature() + "℃)");
        eventLog.setOccurredDate(new java.util.Date());

        eventLogList.add(eventLog);
        return insertEvent(eventLogList);
    }

    public synchronized int insertEvent(List<EventLog> eventLogList){
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

    public synchronized List<EventLog> getAllEvent(){
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

        return retVal;
    }
}