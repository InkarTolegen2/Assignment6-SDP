package fleet.listener;

import fleet.core.Observer;

import java.util.concurrent.atomic.AtomicInteger;

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