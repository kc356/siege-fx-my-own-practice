import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class BoundedBufferTest {

    @Test
    void put_test() throws InterruptedException {
        BoundedBuffer<Integer> queue = new BoundedBuffer<>(1);
        Integer item = 1;
        queue.put(item);
        Integer res = queue.take();
        assertEquals(item, res);
    }

    @Test
    void concurrency_test() throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(2);

        Thread producer = new Thread(() -> {
            try {
                buffer.put(1);
                buffer.put(2);
                buffer.put(3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                assertEquals(1, buffer.take());
                assertEquals(2, buffer.take());
                assertEquals(3, buffer.take());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
    }

    @RepeatedTest(100)
    void manyProducersManyConsumers() throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(10);

        int producerCount = 4;
        int consumerCount = 4;
        int itemsPerProducer = 1000;
        int totalItems = producerCount * itemsPerProducer;

        ExecutorService pool = Executors.newFixedThreadPool(producerCount + consumerCount);
        CountDownLatch startGate = new CountDownLatch(1);          // 一齊出發
        CountDownLatch doneGate = new CountDownLatch(producerCount + consumerCount);

        AtomicInteger produced = new AtomicInteger(0);
        AtomicInteger consumed = new AtomicInteger(0);

        // Producers
        for (int p = 0; p < producerCount; p++) {
            pool.submit(() -> {
                try {
                    startGate.await();                            // 等開閘
                    for (int i = 0; i < itemsPerProducer; i++) {
                        buffer.put(i);
                        produced.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        // Consumers
        for (int c = 0; c < consumerCount; c++) {
            pool.submit(() -> {
                try {
                    startGate.await();
                    while (consumed.get() < totalItems) {
                        buffer.take();
                        consumed.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();                                    // 開閘,全部一齊衝
        boolean finished = doneGate.await(5, TimeUnit.SECONDS);   // 最多等 5 秒
        pool.shutdownNow();

        assertTrue(finished, "Test timed out — likely a deadlock or lost item");
        assertEquals(totalItems, produced.get());
        assertEquals(totalItems, consumed.get());
    }

}
