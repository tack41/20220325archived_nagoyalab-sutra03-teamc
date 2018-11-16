package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

public enum EventType {
    Light,                   //光イベント発生
    LightBecomeNormal,       //光イベント解消
    LightHandled,            //光イベント対応完了
    Swing,                   //振るイベント発生
    SwingBecomeNormal,       //振るイベント解消
    SwingHandled,            //振るイベント対応完了
    Temperature,             //温度イベント発生
    TemperatureBecomeNormal, //温度イベント解消
    Unknown                  //不明なイベント
}
