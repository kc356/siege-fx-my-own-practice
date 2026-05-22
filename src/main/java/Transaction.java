import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Transaction
{
    private enum Type {DEPOSIT, WITHDRAW, TRANSFER};

    private enum Status {PENDING, SUCCESS, FAILED};

    private final AtomicLong transactionIdGenerator = new AtomicLong(1);

    private final long transactionId;

    private final long sourceAccountId;

    private final long targetAccountId;

    private final BigDecimal amount;

    private volatile Status status;

    private final Instant createdAt;


    public Transaction(long sourceAccountId, long targetAccountId, BigDecimal amount, Instant createdAt) {
        this.transactionId = transactionIdGenerator.getAndIncrement();
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.createdAt = createdAt;
        this.status = Status.PENDING;
    }

}
