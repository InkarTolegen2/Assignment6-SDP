package fleet.core;

public interface Subject<E> {
    <T extends E> void subscribe(Class<T> eventType,  Observer<? super T> observer);

    <T extends E> void unsubscribe(Class<T> eventType,  Observer<? super T> observer);
    
}
