package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class SettingActivity extends Activity {

    private Setting _setting;

    private EditText _edittext_user_name;
    private EditText _edittext_temperature_upper_limit;
    private EditText _edittext_temperature_lower_limit;
    private EditText _edittext_interval;
    private Button _button_cancel;
    private Button _button_update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Intent intent = getIntent();
        _setting = Setting.getFromIntent(intent);

        //Get UI Instances to handle from code.
        getUIInstances();

        //Set initial values.
        setInitialValues();

    }

    /**
     * Get UI Instance and register event handler.
     */
    private void getUIInstances(){

        _edittext_user_name = findViewById(R.id.setting_user_name);
        _edittext_temperature_upper_limit = findViewById(R.id.setting_temperature_upper_limit);
        _edittext_temperature_lower_limit = findViewById(R.id.setting_temperature_lower_limit);
        _edittext_interval = findViewById(R.id.setting_interval);

        _button_cancel = findViewById(R.id.setting_cancel);
        _button_cancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Return to main activity
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
        );

        _button_update = findViewById(R.id.setting_update);
        _button_update.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String userName = _edittext_user_name.getText().toString();
                        String strTemperatureUpperLimit = _edittext_temperature_upper_limit.getText().toString();
                        String strTemperatureLowerLimit = _edittext_temperature_lower_limit.getText().toString();
                        String strInterval = _edittext_interval.getText().toString();

                        float temperatureUpperLimit,temperatureLowerLimit;
                        int interval;
                        try{
                            temperatureUpperLimit = Float.parseFloat(strTemperatureUpperLimit);
                        }catch(NumberFormatException e){
                            new AlertDialog.Builder(SettingActivity.this)
                                    .setTitle("入力エラー")
                                    .setMessage("適正温度の上限には数字を指定してください")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }
                        try{
                            temperatureLowerLimit = Float.parseFloat(strTemperatureLowerLimit);
                        }catch(NumberFormatException e){
                            new AlertDialog.Builder(SettingActivity.this)
                                    .setTitle("入力エラー")
                                    .setMessage("適正温度の下限には数字を指定してください")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }
                        try{
                            interval = Integer.parseInt(strInterval);
                        }catch(NumberFormatException e){
                            new AlertDialog.Builder(SettingActivity.this)
                                    .setTitle("入力エラー")
                                    .setMessage("計測間隔には整数を指定してください")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }

                        if(temperatureLowerLimit >= temperatureUpperLimit){
                            new AlertDialog.Builder(SettingActivity.this)
                                    .setTitle("入力エラー")
                                    .setMessage("適正温度の下限には上限よりも小さい値を指定してください")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }
                        if(interval < 10){
                            new AlertDialog.Builder(SettingActivity.this)
                                    .setTitle("入力エラー")
                                    .setMessage("計測間隔には10秒以上を指定してください")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }

                        Setting setting = new Setting();
                        setting.setUserName(userName);
                        setting.setTemperatureUpperLimit(temperatureUpperLimit);
                        setting.setTemperatureLowerLimit(temperatureLowerLimit);
                        setting.setMeasurementInterval(interval);

                        //Save inputted value
                        setting.save();

                        //Return to main activity
                        Intent intent = getIntent();
                        setting.putToIntent(intent);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
        );
    }

    private void setInitialValues(){
        _edittext_user_name.setText(_setting.getUserName());
        _edittext_temperature_upper_limit.setText(String.valueOf(_setting.getTemperatureUpperLimit()));
        _edittext_temperature_lower_limit.setText(String.valueOf(_setting.getTemperatureLowerLimit()));
        _edittext_interval.setText(String.valueOf(_setting.getMeasurementInterval()));
    }
}
