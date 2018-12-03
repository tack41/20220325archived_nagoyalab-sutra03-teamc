package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLog;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogStoreService;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog.EventLogType;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.MyApplication;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.R;

import java.util.ArrayList;
import java.util.List;

public class SensorService extends Service {
    private final static String TAB = "SensorService";
    private final IBinder _binder = new SensorService.LocalBinder();

    public class LocalBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }

    private List<LightEventListener> lightEventList = new ArrayList<>();
    private List<SwingEventListener> swingEventList = new ArrayList<>();
    private List<TemperatureEventListener> temperatureEventList = new ArrayList<>();

    EventLogStoreService _eventLogStoreService;
    // Serviceとのインターフェースクラス
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Serviceとの接続確立時に呼び出される。
            // service引数には、Onbind()で返却したBinderが渡される
            _eventLogStoreService = ((EventLogStoreService.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            // Serviceとの切断時に呼び出される。
            _eventLogStoreService = null;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // Bind EventLogStoreService
        Intent i = new Intent(getBaseContext(), EventLogStoreService.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);

        return _binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAB,"onUnbind");

        lightEventList.clear();
        swingEventList.clear();
        temperatureEventList.clear();

        // Unbind EventLogStoreService
        unbindService(mConnection);

        return true;
    }

    public void addLightEventListener(LightEventListener listener){
        lightEventList.add(listener);
    }

    public void removeLightEventListener(LightEventListener listener){
        lightEventList.remove(listener);
    }

    public void addSwingEventListener(SwingEventListener listener){
        swingEventList.add(listener);
    }

    public void removeSwingEventListener(SwingEventListener listener){
        swingEventList.remove(listener);
    }

    public void addTemperatureEventListener(TemperatureEventListener listener){
        temperatureEventList.add(listener);
    }

    public void removeTemperatureEventListener(TemperatureEventListener listener){
        temperatureEventList.remove(listener);
    }

    // 以下、各イベントを手動発生させる。
    // 最終的にセンサー値から内部発生できるようになったらprivateにする

    // 光イベント
    public void fireLighted(boolean isNormal){
        //Create Event object.
        LightEvent event = new LightEvent(isNormal,
                isNormal ? "光が閾値以下となったことを検知" : "光が閾値以上となったことを検知");

        //Save EventLog
        _eventLogStoreService.insertEvent(event);

        //Post notification
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder
                = new NotificationCompat.Builder(MyApplication.getInstance(), getString(R.string.app_name))
                .setContentTitle(event.getMessage())
                .setContentText(event.getMessage())
                .setSmallIcon(R.drawable.notification_icon_background);
        NotificationManagerCompat.from(MyApplication.getInstance()).notify(1, builder.build());

        //Do all registered callback
        for(LightEventListener listner : lightEventList){
            listner.onLighted(event);
        }
    }

    // 振動イベント
    public void fireSwinged(boolean isNormal){
        //Create Event object
        SwingEvent event = new SwingEvent(isNormal,
                isNormal ? "振動が閾値以下となったことを検知" : "振動が閾値以上となったことを検知");

        //Save EventLog
        _eventLogStoreService.insertEvent(event);

        //Post notification
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder
                = new NotificationCompat.Builder(MyApplication.getInstance(), getString(R.string.app_name))
                .setContentTitle(event.getMessage())
                .setContentText(event.getMessage())
                .setSmallIcon(R.drawable.notification_icon_background);
        NotificationManagerCompat.from(MyApplication.getInstance()).notify(1, builder.build());

        //Do all registered callback
        for(SwingEventListener listner : swingEventList){
            listner.onSwinged(event);
        }
    }

    // 温度イベント
    public void fireTemperatured(boolean isNormal, double temperature) {
        //Create Event object
        TemperatureEvent event = new TemperatureEvent(isNormal,
                temperature,
                isNormal ? "温度が閾値範囲となったことを検知" : "温度が閾値範囲外となったことを検知");

        //Save EventLog
        _eventLogStoreService.insertEvent(event);

        //Post notification
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder
                = new NotificationCompat.Builder(MyApplication.getInstance(), getString(R.string.app_name))
                .setContentTitle(event.getMessage())
                .setContentText(event.getMessage())
                .setSmallIcon(R.drawable.notification_icon_background);
        NotificationManagerCompat.from(MyApplication.getInstance()).notify(1, builder.build());

        //Do all registered callback
        for (TemperatureEventListener listner : temperatureEventList) {
            listner.onTemperatureChanged(event);
        }
    }
}
