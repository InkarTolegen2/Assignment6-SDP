package fleet.listener;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventCounterTest {
    @Test
    void startsAtZero() {
        assertEquals(0, new EventCounter<String>().count());
    }

    @Test
    void countsEveryEvent() {
        EventCounter<String> counter = new EventCounter<>();

        counter.onEvent("a");
        counter.onEvent("b");

        assertEquals(2, counter.count());
    }

    @Test
    void countsExactlyWhenFourThreadsCountAtOnce() throws Exception {
        EventCounter<String> counter = new EventCounter<>();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        for (int t = 0; t < 4; t++) {
            pool.submit(() -> {
                start.await();
                for (int i = 0; i < 1_000; i++) {
                    counter.onEvent("x");
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();

        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(4_000, counter.count());
    }
}