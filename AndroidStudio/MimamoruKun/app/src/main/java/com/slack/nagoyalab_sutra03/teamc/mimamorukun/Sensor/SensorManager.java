package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import java.util.ArrayList;
import java.util.List;

public class SensorManager{
    private static List<LightEventListener> lightEventList;
    private static List<SwingEventListener> swingEventList;
    private static List<TemperatureEventListener> temperatureEventList;

    static{
        lightEventList = new ArrayList<>();
        swingEventList = new ArrayList<>();
        temperatureEventList = new ArrayList<>();
    }

    public static void addLightEventListener(LightEventListener listener){
        lightEventList.add(listener);
    }

    public static void removeLightEventListener(LightEventListener listener){
        lightEventList.remove(listener);
    }

    public static void addSwingEventListener(SwingEventListener listener){
        swingEventList.add(listener);
    }

    public static void removeSwingEventListener(SwingEventListener listener){
        swingEventList.remove(listener);
    }

    public static void addTemperatureEventListener(TemperatureEventListener listener){
        temperatureEventList.add(listener);
    }

    public static void removeTemperatureEventListener(TemperatureEventListener listener){
        temperatureEventList.remove(listener);
    }

    // 以下、各イベントを手動発生させる。
    // 最終的にセンサー値から内部発生できるようになったらprivateにする

    // 光イベント
    public static void fireLighted(boolean isNormal){
        LightEvent event = new LightEvent(isNormal,
                isNormal ? "光が閾値以下となったことを検知" : "光が閾値以上となったことを検知");
        for(LightEventListener listner : lightEventList){
            listner.onLighted(event);
        }
    }

    // 振動イベント
    public static void fireSwinged(boolean isNormal){
        SwingEvent event = new SwingEvent(isNormal,
                isNormal ? "振動が閾値以下となったことを検知" : "振動が閾値以上となったことを検知");
        for(SwingEventListener listner : swingEventList){
            listner.onSwinged(event);
        }
    }

    // 温度イベント
    public static void fireTemperatured(boolean isNormal, double temperature){
        TemperatureEvent event = new TemperatureEvent(isNormal,
                temperature,
                isNormal ? "温度が閾値範囲となったことを検知" : "温度が閾値範囲外となったことを検知");
        for(TemperatureEventListener listner : temperatureEventList){
            listner.onTemperatureChanged(event);
        }
    }
}
