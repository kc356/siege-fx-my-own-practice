import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class PricePublisher {

    private static class Subscription {
        final PriceListener listener;
        final BlockingQueue<PriceData> queue = new LinkedBlockingQueue<>(1000);
        final Thread worker;
        volatile boolean running = true;

        Subscription(PriceListener listener) {
            this.listener = listener;
            this.worker = new Thread(this::drainLoop);
            this.worker.start();
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
            worker.interrupt();
        }

    }

    private final CopyOnWriteArrayList<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    public void subscribe(PriceListener listener) {
        subscriptions.add(new Subscription(listener));
    }

    public void unsubcribe(PriceListener listener) {
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

