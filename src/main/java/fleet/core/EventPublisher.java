package fleet.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventPublisher<E> implements Subject<E> {
    private final Map<Class<?>, CopyOnWriteArrayList<Observer<?>>> registry = new ConcurrentHashMap<>();
    private final Consumer<Throwable> listenerErrors;

    public EventPublisher(Consumer<Throwable> listenerErrors) {
        this.listenerErrors = listenerErrors;
    }

    @Override
    public <T extends E> void subscribe(Class<T> eventType, Observer<? super T> observer) {
        registry.computeIfAbsent(eventType, type -> new CopyOnWriteArrayList<>()).addIfAbsent(observer);
    }

    @Override
    public <T extends E> void unsubscribe(Class<T> eventType, Observer<? super T> observer) {
        List<Observer<?>> observers = registry.get(eventType);
        if (observers != null) {
            observers.remove(observer);
        }
    }

    @SuppressWarnings("unchecked")
    public void publish(E event) {
        List<Observer<?>> observers = registry.get(event.getClass());
        if (observers == null) {
            return;
        }
        for (Observer<?> observer : observers) {
            try {
                ((Observer<E>) observer).onEvent(event);
            } catch (RuntimeException failure) {
                listenerErrors.accept(failure);
            }
        }
    }
}