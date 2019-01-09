package com.slack.nagoyalab_sutra03.teamc.mimamorukun.EventLog;

public enum EventLogType {
    //光イベント発生
    Light("Light"),
    //光イベント対応完了
    LightHandled("LightHandled"),
    //動きイベント発生
    Movement("Movement"),
    //動きイベント対応完了
    MovementHandled("MovementHandled"),
    //温度イベント発生
    TemperatureUnusual("TemperatureUnusual"),
    //温度イベント対応完了
    TemperatureHandled("TemperatureHandled"),
    //不明なイベント
    Unknown("Unknown");

    private final String value;

    EventLogType(String value){
        this.value = value;
    }

    public String toValue(){
        return value;
    }

    public EventLogType getDefault(){
        return Unknown;
    }


    //イベントのタイトル
    public String getTitle()
    {
        switch(this){
            case Light:
                return "起床しました";
            case LightHandled:
                return "起床に対応しました";
            case Movement:
                return "呼び出しました";
            case MovementHandled:
                return "呼び出しに対応しました";
            case TemperatureUnusual:
                return "温度異常が発生しました";
            case TemperatureHandled:
                return "温度異常に対応しました";
            default:
                return "不明なイベント";
        }
    }
}
