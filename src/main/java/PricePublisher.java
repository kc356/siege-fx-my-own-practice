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
        volatile Future<?> future;          // ← hold 住個 handle

        Subscription(PriceListener listener) {
            this.listener = listener;
        }

        private void loop() {
            // 唔再靠 running flag,改睇 interrupt 狀態
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    PriceData data = queue.take();
                    listener.onPrice(data.symbol, data.price);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();   // 復原 flag,然後...
                    return;                                // 退出 loop,thread 收工
                }
            }
        }

        void stop() {
            if (future != null) {
                future.cancel(true);   // true = interrupt 緊跑嗰條 thread → take() 即刻醒
            }
        }
    }

    private final CopyOnWriteArrayList<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    public void subscribe(PriceListener listener) {
        Subscription sub = new Subscription(listener);
        subscriptions.add(sub);
        sub.future = pool.submit(sub::loop);   // ← submit 攞返 Future
    }

    public void unsubscribe(PriceListener listener) {
        subscriptions.removeIf(subscription -> {
            if (subscription.listener.equals(listener)) {
                subscription.stop();   // cancel(true) 戳醒並中止條 thread
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

    public void shutdown() {
        pool.shutdownNow();   // 中止所有仲跑緊嘅 drainLoop 並停 pool
    }
}