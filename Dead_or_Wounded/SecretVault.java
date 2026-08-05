import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Arrays;

public class SecretVault {
    private int[] secretArray;
    
    // Concurrency Controls
    private final ReentrantLock stateLock = new ReentrantLock();
    private final Semaphore rateLimiter = new Semaphore(2);
    private final CountDownLatch startupLatch = new CountDownLatch(3);

    public SecretVault() {
        this.secretArray = generateUniqueDigitArray();
    }

    // --- SYSTEM BOOT SEQUENCE ---
    
    public void markSystemCheckComplete() {
        startupLatch.countDown();
        System.out.println("[SYSTEM] Boot check complete. Remaining: " + startupLatch.getCount());
    }

    public void awaitSystemReady() throws InterruptedException {
        startupLatch.await(); 
    }

    // --- DOMAIN LOGIC ---

    public GuessResult attemptGuess(int[] guessArray) {
        try {
            rateLimiter.acquire(); // Throttle incoming guesses
            
            GuessResult result = null;
            
            stateLock.lock(); // Lock state for safe evaluation
            try {
                result = evaluateGuess(guessArray, this.secretArray);
                
                if (result.dead == 4) {
                    System.out.println("\n[SYSTEM] VAULT CRACKED! Generating new secret...");
                    this.secretArray = generateUniqueDigitArray(); 
                }
            } finally {
                stateLock.unlock(); 
            }
            
            return result;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new GuessResult(-1, -1); // Error state
        } finally {
            rateLimiter.release(); 
        }
    }

    private GuessResult evaluateGuess(int[] guess, int[] secret) {
        int dead = 0;
        int wounded = 0;

        for (int i = 0; i < 4; i++) {
            if (guess[i] == secret[i]) {
                dead++;
            } else {
                for (int j = 0; j < 4; j++) {
                    if (guess[i] == secret[j]) {
                        wounded++;
                        break; 
                    }
                }
            }
        }
        return new GuessResult(dead, wounded);
    }

    private int[] generateUniqueDigitArray() {
        int[] newSecret = new int[4];
        boolean[] usedDigits = new boolean[10]; 
        int currentIndex = 0;
        
        while (currentIndex < 4) {
            int randomDigit = ThreadLocalRandom.current().nextInt(0, 10);
            
            if (!usedDigits[randomDigit]) {
                newSecret[currentIndex] = randomDigit;
                usedDigits[randomDigit] = true;
                currentIndex++; 
            }
        }
        return newSecret;
    }
}