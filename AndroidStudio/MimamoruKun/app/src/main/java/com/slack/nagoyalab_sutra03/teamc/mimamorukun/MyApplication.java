package com.slack.nagoyalab_sutra03.teamc.mimamorukun;

import android.app.Application;

/**
 * 複数ActivityにまたがるServiceでContextを利用するためにApplicationをSingletonで保持
 * メモリリークに注意
 * https://qiita.com/roba4coding/items/0585b8240873ec5e9c20
 */
public class MyApplication extends Application {
    private static MyApplication instance = null;
    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
