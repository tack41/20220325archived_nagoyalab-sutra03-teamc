package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import java.util.Date;

public class Event
{
    //イベント種別
    public EventType eventType;
    public void setType(EventType eventType){ this.eventType = eventType; }
    public EventType getType(){ return this.eventType; }

    //イベントのタイトル
    public String getTitle()
    {
        switch(this.eventType){
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
            case Temperature:
                return "温度異常が発生しました";
            case TemperatureBecomeNormal:
                return "温度異常が解消しました";
            default:
                return "不明なイベント";
        }
    }

    /*
      イベントの説明を記載します
     */
    public String getDescription(){
        switch(this.eventType){
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
            case Temperature:
                return "装置が温度の異常(30℃より上,10℃未満)を検知しました";
            case TemperatureBecomeNormal:
                return "装置が温度が正常範囲内(10℃～30℃)となったことを検知しました。";
            default:
                return "不明なイベント";
        }
    }

    //イベント内容
    String content;
    public void setContent(String content){
        this.content = content;
    }
    public String getContent(){
        return this.content;
    }

    //発生日時
    Date occurredDate;
    public void setOccurredDate(Date occurredDate){
        this.occurredDate = occurredDate;
    }
    public Date getOccurredDate(){
        return this.occurredDate;
    }

}

