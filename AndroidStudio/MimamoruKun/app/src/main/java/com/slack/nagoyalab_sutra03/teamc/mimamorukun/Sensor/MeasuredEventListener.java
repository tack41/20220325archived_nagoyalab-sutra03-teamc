package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import java.util.EventListener;

public interface MeasuredEventListener extends EventListener {
    void onMeasured(MeasuredEvent e);
}
