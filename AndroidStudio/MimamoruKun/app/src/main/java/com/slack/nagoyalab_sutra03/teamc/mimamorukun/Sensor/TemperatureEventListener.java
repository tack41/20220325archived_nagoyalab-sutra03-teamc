package com.slack.nagoyalab_sutra03.teamc.mimamorukun.Sensor;

import java.util.EventListener;

public interface TemperatureEventListener extends EventListener {
    void onTemperatureChanged(TemperatureEvent e);
}
