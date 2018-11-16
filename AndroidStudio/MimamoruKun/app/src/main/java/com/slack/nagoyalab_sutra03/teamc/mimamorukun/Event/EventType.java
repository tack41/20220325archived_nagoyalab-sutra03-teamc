package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Event;

public enum EventType {
    //光イベント発生
    Light("Light"),
    //光イベント解消
    LightBecomeNormal("LightBecomeNormal"),
    //光イベント対応完了
    LightHandled("LightHandled"),
    //振るイベント発生
    Swing("Swing"),
    //振るイベント解消
    SwingBecomeNormal("SwingBecomeNormal"),
    //振るイベント対応完了
    SwingHandled("SwingHandled"),
    //温度イベント発生
    TemperatureUnusual("TemperatureUnusual"),
    //温度イベント解消
    TemperatureBecomeUsual("TemperatureBecomeUsual"),
    //不明なイベント
    Unknown("Unknown");

    private final String value;

    EventType(String value){
        this.value = value;
    }

    public String toValue(){
        return value;
    }

    public EventType getDefault(){
        return Unknown;
    }


    //イベントのタイトル
    public String getTitle()
    {
        switch(this){
            case Light:
                return "起床しました";
            case LightBecomeNormal:
                return "就寝しました";
            case LightHandled:
                return "起床に対応しました";
            case Swing:
                return "呼び出しました";
            case SwingBecomeNormal:
                return "呼び出しを解除しました";
            case SwingHandled:
                return "呼び出しに対応しました";
            case TemperatureUnusual:
                return "温度異常が発生しました";
            case TemperatureBecomeUsual:
                return "温度異常が解消しました";
            default:
                return "不明なイベント";
        }
    }


    /*
      イベントの説明を記載します
     */
    public String getDescription(){
        switch(this){
            case Light:
                return "装置が光を検知しました";
            case LightBecomeNormal:
                return "装置が光を検知しなくなりました";
            case LightHandled:
                return "装置が光を検知したことに対応しました";
            case Swing:
                return "装置が振動を検知しました";
            case SwingBecomeNormal:
                return "装置が振動停止を検知しました";
            case SwingHandled:
                return "装置が振動を検知したことに対応しました";
            case TemperatureUnusual:
                return "装置が温度の異常(30℃より上,10℃未満)を検知しました";
            case TemperatureBecomeUsual:
                return "装置が温度が正常範囲内(10℃～30℃)となったことを検知しました。";
            default:
                return "不明なイベント";
        }
    }
}
