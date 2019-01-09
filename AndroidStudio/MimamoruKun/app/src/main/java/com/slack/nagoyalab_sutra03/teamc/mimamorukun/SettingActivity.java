package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioGroup;

public class SettingActivity extends Activity {

    private Setting _setting;
    private boolean _temperature_enable;
    private boolean _optical_enable;
    private boolean _movement_enable;

    private EditText _edittext_user_name;
    private EditText _edittext_temperature_upper_limit;
    private EditText _edittext_temperature_lower_limit;
    private EditText _edittext_optical_threshold;
    private EditText _edittext_movement_threshold;
    private RadioGroup _radiogroup_temperature_enable;
    private RadioGroup _radiogroup_optical_enable;
    private RadioGroup _radiogroup_movement_enable;

    private Button _button_cancel;
    private Button _button_update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Intent intent = getIntent();
        _setting = Setting.getFromIntent(intent);
        _temperature_enable = intent.getBooleanExtra(MainActivity.INTENT_KEY_TEMPERATURE_ENABLE, true);
        _optical_enable = intent.getBooleanExtra(MainActivity.INTENT_KEY_OPTICAL_ENABLE, true);
        _movement_enable = intent.getBooleanExtra(MainActivity.INTENT_KEY_MOVEMENT_ENABLE, true);

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
        _edittext_optical_threshold = findViewById(R.id.setting_optical_threshold);
        _edittext_movement_threshold = findViewById(R.id.setting_optical_threshold);
        _radiogroup_temperature_enable = findViewById(R.id.radiogroup_temperature_enable);
        _radiogroup_optical_enable = findViewById(R.id.radiogroup_optical_enable);
        _radiogroup_movement_enable = findViewById(R.id.radiogroup_movement_enable);

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
                        String strOpticalThreshold = _edittext_optical_threshold.getText().toString();
                        String strMovementThreshold = _edittext_movement_threshold.getText().toString();

                        float temperatureUpperLimit,temperatureLowerLimit,opticalThreshold,movementThreshold;
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
                            opticalThreshold = Float.parseFloat(strOpticalThreshold);
                        }catch(NumberFormatException e){
                            new AlertDialog.Builder(SettingActivity.this)
                                    .setTitle("入力エラー")
                                    .setMessage("光閾値には数字を指定してください")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }
                        try{
                            movementThreshold = Float.parseFloat(strMovementThreshold);
                        }catch(NumberFormatException e){
                            new AlertDialog.Builder(SettingActivity.this)
                                    .setTitle("入力エラー")
                                    .setMessage("動き閾値には数字を指定してください")
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

                        Setting setting = new Setting();
                        setting.setUserName(userName);
                        setting.setTemperatureUpperLimit(temperatureUpperLimit);
                        setting.setTemperatureLowerLimit(temperatureLowerLimit);
                        setting.setOpticalThreshold(opticalThreshold);
                        setting.setMovementThreshold(movementThreshold);

                        //Save inputted value
                        setting.save();

                        boolean temperatureEnable = (_radiogroup_temperature_enable.getCheckedRadioButtonId() == R.id.radiobutton_temperature_enable);
                        boolean opticalEnable = (_radiogroup_optical_enable.getCheckedRadioButtonId() == R.id.radiobutton_optical_enable);
                        boolean movementEnable = (_radiogroup_movement_enable.getCheckedRadioButtonId() == R.id.radiobutton_movement_enable);

                        //Return to main activity
                        Intent intent = getIntent();
                        setting.putToIntent(intent);
                        intent.putExtra(MainActivity.INTENT_KEY_TEMPERATURE_ENABLE, temperatureEnable);
                        intent.putExtra(MainActivity.INTENT_KEY_OPTICAL_ENABLE, opticalEnable);
                        intent.putExtra(MainActivity.INTENT_KEY_MOVEMENT_ENABLE, movementEnable);
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
        _edittext_optical_threshold.setText(String.valueOf(_setting.getOpticalThreshold()));
        _edittext_movement_threshold.setText(String.valueOf(_setting.getMovementThreshold()));

        if(_temperature_enable){
            _radiogroup_temperature_enable.check(R.id.radiobutton_temperature_enable);
        }else{
            _radiogroup_temperature_enable.check(R.id.radiobutton_temperature_disable);
        }

        if(_optical_enable){
            _radiogroup_optical_enable.check(R.id.radiobutton_optical_enable);
        }else{
            _radiogroup_optical_enable.check(R.id.radiobutton_optical_disable);
        }

        if(_movement_enable){
            _radiogroup_movement_enable.check(R.id.radiobutton_movement_enable);
        }else{
            _radiogroup_movement_enable.check(R.id.radiobutton_movement_disable);
        }
    }
}
