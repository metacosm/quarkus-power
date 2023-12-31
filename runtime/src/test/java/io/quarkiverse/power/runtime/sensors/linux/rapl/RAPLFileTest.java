package io.quarkiverse.power.runtime.sensors.linux.rapl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import org.junit.jupiter.api.Test;

class RAPLFileTest {

    @Test
    void periodicReadingShouldWork() throws IOException, InterruptedException {
        for (int i = 0; i < 5; i++) {
            writeThenRead();
        }
    }

    private static void writeThenRead() throws IOException, InterruptedException {
        final var file = Path.of("target/test.txt");
        final var value = Math.abs(new Random().nextLong());
        Files.writeString(file, value + "\n");
        Thread.sleep(50);

        final var raplFile = ByteBufferRAPLFile.createFrom(file);
        final var measure = raplFile.extractPowerMeasure();
        assertEquals(value, measure);
    }
}
