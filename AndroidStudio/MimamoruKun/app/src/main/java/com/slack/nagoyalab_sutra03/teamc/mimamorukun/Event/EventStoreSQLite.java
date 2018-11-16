package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class EventStoreSQLite {

    private EventSQLiteOpenHelper _helper;
    private SQLiteDatabase _db;
    private Context _context;

    public EventStoreSQLite(Context context){
        _context = context;
        _helper = new EventSQLiteOpenHelper(context);
        _db = this._helper.getWritableDatabase();
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

        return retVal;
    }
}
