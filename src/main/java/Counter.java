public class Counter {
    private volatile long value = 0;

    public synchronized void increment() {
        value += 1;
    }

    public synchronized long read()
    {
        return value;
    }
}
