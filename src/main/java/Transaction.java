import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Transaction {
    private enum Type {DEPOSIT, WITHDRAW, TRANSFER}

    ;

    private enum Status {PENDING, SUCCESS, FAILED}

    ;

    private final AtomicLong transactionIdGenerator = new AtomicLong(1);

    private final long transactionId;

    private final long sourceAccountId;

    private final long targetAccountId;

    private final BigDecimal amount;

    private volatile Status status;

    private final Instant createdAt;

    private final Type type;


    public Transaction(long sourceAccountId, long targetAccountId, BigDecimal amount, Instant createdAt, Type type) {
        this.type = type;
        this.transactionId = transactionIdGenerator.getAndIncrement();
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.createdAt = createdAt;
        this.status = Status.PENDING;
    }


    public static Transaction deposit(long accountId, BigDecimal amount) {
        return new Transaction(accountId, -1, amount, Instant.now(), Type.DEPOSIT);
    }

    public static Transaction withdraw(long accountId, BigDecimal amount) {
        return new Transaction(accountId, -1, amount, Instant.now(), Type.WITHDRAW);
    }

    public static Transaction transfer (long sourceId, long targetId, BigDecimal amount) {
        return new Transaction(sourceId, targetId, amount, Instant.now(), Type.TRANSFER);
    }

}
