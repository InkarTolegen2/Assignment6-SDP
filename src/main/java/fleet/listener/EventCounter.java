package fleet.listener;

import fleet.core.Observer;

import java.util.concurrent.atomic.AtomicInteger;

/** Counts the events of one type; safe to share between producer threads. */
public class EventCounter<E> implements Observer<E> {
    private final AtomicInteger count = new AtomicInteger();

    @Override
    public void onEvent(E event) {
        count.incrementAndGet();
    }

    public int count() {
        return count.get();
    }
}