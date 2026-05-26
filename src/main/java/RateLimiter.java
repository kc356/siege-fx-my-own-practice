import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {


    private static class Window {
        final long start;
        final int count;

        private Window(long start, int count) {
            this.start = start;
            this.count = count;
        }
    }


    private final int windowMs = 1000;
    private final int windowLimit = 5;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();


    public boolean allow(String clientId) {
        long now = System.currentTimeMillis();

        Window updated = windows.compute(clientId, (k, w) -> {
            if (w == null || now - w.start >= windowMs) {
                return new Window(now, 1);
            }
            return new Window(w.start, w.count + 1);
        });

        return updated.count <= windowLimit;
    }


}
