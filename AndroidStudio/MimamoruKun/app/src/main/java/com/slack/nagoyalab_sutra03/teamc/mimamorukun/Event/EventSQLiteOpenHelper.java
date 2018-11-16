package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class EventSQLiteOpenHelper extends SQLiteOpenHelper {

    // データーベースのバージョン
    public static final int DATABASE_VERSION = 2;
    // データーベース名
    public static final String DATABASE_NAME = "EventDB.db";

    private final String SQL_CREATE_ENTRIES =
            "CREATE TABLE events (" +
                    "_id INTEGER PRIMARY KEY," +
                    "event_type TEXT," +
                    "content TEXT," +
                    "occurred_date INTEGER)";

    public EventSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // テーブル作成
        db.execSQL(
                SQL_CREATE_ENTRIES
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
         //テーブルを削除する
        db.execSQL("DROP TABLE IF EXISTS events");

        // 新しくテーブルを作成する
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db,
                            int oldVersion, int newVersion) {
    }
}
