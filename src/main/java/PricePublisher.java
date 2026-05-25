import java.math.BigDecimal;
import java.util.concurrent.*;

public class PricePublisher {

    private final ExecutorService pool;

    public PricePublisher() {
        this.pool = Executors.newFixedThreadPool(4);
    }

    private static class Subscription {
        final PriceListener listener;
        final BlockingQueue<PriceData> queue = new LinkedBlockingQueue<>(1000);
        volatile boolean running = true;

        Subscription(PriceListener listener) {
            this.listener = listener;
        }

        private void drainLoop() {
            while (running) {
                try {
                    PriceData data = queue.take();
                    listener.onPrice(data.symbol, data.price);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        void stop() {
            running = false;
        }

    }

    private final CopyOnWriteArrayList<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    public void subscribe(PriceListener listener) {
        Subscription sub = new Subscription(listener);
        subscriptions.add(sub);
        pool.execute(sub::drainLoop);
    }

    public void unsubscribe(PriceListener listener) {
        subscriptions.removeIf(subscription -> {
            if (subscription.listener.equals(listener)) {
                subscription.stop();
                return true;
            }
            return false;
        });
    }

    public void publish(String symbol, BigDecimal price) {
        PriceData data = new PriceData(symbol, price);
        for (Subscription sub : subscriptions) {
            sub.queue.offer(data);
        }
    }


}

