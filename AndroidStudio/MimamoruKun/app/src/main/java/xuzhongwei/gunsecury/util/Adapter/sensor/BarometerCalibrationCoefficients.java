package xuzhongwei.gunsecury.util.Adapter.sensor;

import java.util.List;

public enum BarometerCalibrationCoefficients {
    INSTANCE;
    volatile public List<Integer> barometerCalibrationCoefficients;
    volatile public double heightCalibration;
}
