package io.quarkiverse.power.runtime;

import java.io.IOException;
import java.util.Optional;

public interface PowerSensor<T extends PowerSensor.Measure> {

    void start(long duration, long frequency, Writer out) throws IOException, Exception;

    T stop();

    void outputConsommation(Writer out);

    interface Writer {
        void println(String message);
    }

    interface Measure {
        String CPU = "cpu";
        String GPU = "gpu";
        String TOTAL = "total";

        double cpu();

        Optional<Double> gpu();

        Optional<Double> byKey(String key);

        double total();

        int numberOfSamples();
    }
}
