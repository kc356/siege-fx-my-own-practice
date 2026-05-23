import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {

    private volatile BigDecimal balance;

    private ReentrantLock lock;

    private final Instant createdAt;

    private final long accountId;

    private volatile boolean active;

    private final Condition fundsAvailbale;

    public BankAccount(BigDecimal balance, ReentrantLock lock, Instant createdAt, long accountId) {
        this.balance = balance;
        this.lock = lock;
        this.createdAt = createdAt;
        this.accountId = accountId;
        this.fundsAvailbale = lock.newCondition();
    }


    public BigDecimal deposit(BigDecimal amount) {
        validateAmount(amount);
        lock.lock();
        try {
            checkActive();
            balance = balance.add(amount);
            fundsAvailbale.signalAll();
            return balance;
        } finally {
            lock.unlock();
        }
    }

    private void checkActive() {
        if (!active) {
            throw new AccountClosedException("Account " + accountId + " is closed.");
        }
    }

    public BigDecimal withdraw(BigDecimal amount) {
        validateAmount(amount);
        lock.lock();
        try {
            checkActive();
            if (balance.compareTo(amount) < 0) {
                throw new InsufficientFundsException("Not enough funds.");
            }
            balance = balance.subtract(amount);
            return balance;
        } finally {
            lock.unlock();
        }
    }

    public BigDecimal awaitSufficientFunds(BigDecimal amount) throws InterruptedException {
        validateAmount(amount);
        lock.lock();
        try {
            checkActive();

            while (balance.compareTo(amount) < 0) {
                fundsAvailbale.await();
            }
            balance = balance.subtract(amount);
            return balance;

        } finally {
            lock.unlock();
        }

    }

    public void closeAccount(long accountId) {
        lock.lock();
        try {
            this.active = false;
            fundsAvailbale.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public BigDecimal getBalance() {
        lock.lock();
        try {
            checkActive();
            return balance;
        } finally {
            lock.unlock();
        }
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

    public static class AccountClosedException extends RuntimeException {
        public AccountClosedException(String message) {
            super(message);
        }
    }

}
