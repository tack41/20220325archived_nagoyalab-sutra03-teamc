package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import java.util.EventListener;

public interface LightEventListener extends EventListener {
    void onLighted(LightEvent e);
}
