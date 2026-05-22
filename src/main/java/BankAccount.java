import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {

    private volatile BigDecimal balance;

    private ReentrantLock lock;

    private final Instant createdAt;

    private final long accountId;

    public BankAccount(BigDecimal balance, ReentrantLock lock, Instant createdAt, long accountId) {
        this.balance = balance;
        this.lock = lock;
        this.createdAt = createdAt;
        this.accountId = accountId;
    }


    public BigDecimal deposit(BigDecimal amount) {
        lock.lock();
        try {
            balance = balance.add(amount);

            return balance;
        } finally {
            lock.unlock();
        }
    }

    public BigDecimal withdraw(BigDecimal amount) {
        validateAmount(amount);
        lock.lock();
        try {
            if (balance.compareTo(amount) < 0) {
                throw new InsufficientFundsException("Not enough funds.");
            }
            balance = balance.subtract(amount);
            return balance;
        } finally {
            lock.unlock();
        }
    }

    public BigDecimal awaitSufficientFunds(BigDecimal amount) {

    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
    }

    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

}
