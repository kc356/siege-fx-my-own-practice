import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PricePublisherTest {


    @Test
    void allSubscriberReceiveEachPrice() throws InterruptedException {
        PricePublisher publisher = new PricePublisher();

        int listenerCount = 3;
        CountDownLatch latch = new CountDownLatch(listenerCount);
        AtomicInteger totalReceived = new AtomicInteger(0);

        for (int i = 0; i < listenerCount; i++) {
            publisher.subscribe(((symbol, price) -> {
                totalReceived.incrementAndGet();
                latch.countDown();
            }));
        }

        publisher.publish("EURUSD", new BigDecimal("1.0850"));

        boolean allReceived = latch.await(2, TimeUnit.SECONDS);

        assertTrue(allReceived, "Not all listeners received the price in time");
        assertEquals(3, totalReceived.get());

    }

    @Test
    void unsubscribedListenerStopsReceiving() throws InterruptedException {
        PricePublisher publisher = new PricePublisher();

        AtomicInteger count = new AtomicInteger(0);
        PriceListener listener = (symbol, price) -> count.incrementAndGet();

        publisher.subscribe(listener);

        CountDownLatch firstPrice = new CountDownLatch(1);

        publisher.publish("EURUSD", new BigDecimal("1.0"));

        Thread.sleep(200);

        int afterFirst = count.get();
        assertEquals(1, afterFirst);

        publisher.unsubscribe(listener);
        publisher.publish("EURUSD", new BigDecimal("2.0"));

        Thread.sleep(200);

        assertEquals(afterFirst, count.get(), "Listener received a price after unsubscribing");
    }
}
