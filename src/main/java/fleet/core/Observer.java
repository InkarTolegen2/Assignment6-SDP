package fleet.core;

@FunctionalInterface public interface Observer<E> {
    void onEvent(E event);
}