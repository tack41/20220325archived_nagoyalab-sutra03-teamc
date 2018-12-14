package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.TextView;

import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.MeasuredEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.MeasuredEventListener;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.SensorService;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.TemperatureEvent;
import com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor.TemperatureEventListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TemperatureEventActivity extends Activity implements TemperatureEventListener, MeasuredEventListener {

    private Handler _handler = new Handler();

    private TextView _textViewTitle;
    private TextView _textViewOccurredDate;
    private TextView _textViewTemperature;

    private TemperatureEvent _temperatureEvent;

    //音源
    private SoundPool _soundPool;
    private int _soundAlert;
    private int _soundOK;
    private boolean _loadSoundOKFinished = false;

    SensorService _sensorService;
    private ServiceConnection _connectionSensorService = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            _sensorService = ((SensorService.LocalBinder)service).getService();

            //Bind sensor events.
            _sensorService.addTemperatureEventListener(TemperatureEventActivity.this);
            _sensorService.addMeasuredEventListener(TemperatureEventActivity.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            _sensorService.removeTemperatureEventListener(TemperatureEventActivity.this);
            _sensorService.removeMeasuredEventListner(TemperatureEventActivity.this);

            _sensorService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_event);

        //Load sound source.
        loadAndPlaySound();

        //Get UI Instances to handle from code.
        getUIInstances();

        // Bind SensorService
        Intent i = new Intent(getBaseContext(), SensorService.class);
        bindService(i, _connectionSensorService, Context.BIND_AUTO_CREATE);

        //Get values from caller activity
        Intent intent = getIntent();
        _temperatureEvent = TemperatureEvent.getFromIntent(intent);

        //Display events.
        displayEvent(_temperatureEvent);

        //Display temperature.
        displayTemperature(_temperatureEvent.getTemperature());
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        unloadSound();
        unbindService(_connectionSensorService);
    }

    /**
     * Load sound source to class instance variable.
     */
    private void loadAndPlaySound() {

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        _soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(1)
                .build();

        _soundAlert = _soundPool.load(this, R.raw.kettle_boiling1, 1);
        _soundOK = _soundPool.load(this,R.raw.decision3,1);

        // Play warning sound continuously after load sound source.
        _soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if(_soundAlert == sampleId && 0 == status){
                    soundPool.play(_soundAlert, 1.0f, 1.0f, 0, -1, 1);
                }else if(_soundOK == sampleId && 0 == status){
                    _loadSoundOKFinished = true;
                }
            }
        });
    }

    /**
     * Unload sound.
     */
    private void unloadSound() {

        if(_loadSoundOKFinished){
            _soundPool.unload(_soundAlert);
        }
        _soundPool.release();
    }

    /**
     * Get UI Instance and regist event handler.
     */
    private void getUIInstances() {
        _textViewTitle = findViewById(R.id.textview_title);
        _textViewOccurredDate = findViewById(R.id.textview_occured_date);
        _textViewTemperature = findViewById(R.id.textview_temperature);
    }

    private void displayEvent(TemperatureEvent event){
        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日 H時mm分",new Locale("ja", "JP", "JP"));
        _textViewTitle.setText(_temperatureEvent.getMessage());
        _textViewOccurredDate.setText(sdf.format(_temperatureEvent.getOccurredDate()));
    }

    private void displayTemperature(double temperature){
        _textViewTemperature.setText(String.format("%.1f℃",temperature));

    }
    @Override
    public void onTemperatureChanged(TemperatureEvent e){

        //温度異常が解消された場合はメイン画面に戻る
        if(e.isNormal()){
            _sensorService.removeTemperatureEventListener(this);

            //この画面を閉じてメイン画面に戻る
            Intent intent = new Intent();
            this.setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onMeasured(MeasuredEvent e){
        //Update temperature on UI thread.
        _handler.post(new Runnable() {
            @Override
            public void run() {
                displayTemperature(_sensorService.getTemperature());
            }
        });
    }
}
