package core;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class AyoBoard {
    private final int[] pits = new int[12];
    private final ReentrantLock[] locks = new ReentrantLock[12]; 

    public AyoBoard() {
        for (int i = 0; i < 12; i++) {
            pits[i] = 4; // Standard Ayo start
            locks[i] = new ReentrantLock(true); // Fair locks prevent Starvation
        }
    }

    /* The Atomicinteger provides a way to perform threadsafe operation on a single int value same as
     a regular integer but with additional methods for atomic operations*/

    public boolean makeMoveSafe(int startPit, String playerName, AtomicInteger score) {    
        while (true) {
            // 1. OPTIMISTIC READ: Check seeds without locking yet
            int numSeeds = pits[startPit];
            if (numSeeds == 0) return false; // Pit is empty, invalid move

            // 2. Determine all pits affected by this turn
            int[] pitsToLock = new int[numSeeds + 1];
            pitsToLock[0] = startPit;
            for (int i = 1; i <= numSeeds; i++) {
                pitsToLock[i] = (startPit + i) % 12;
            }

            // 3. DEADLOCK PREVENTION: Remove duplicates and sort numerically
            int[] uniquePits = Arrays.stream(pitsToLock).distinct().sorted().toArray();

            // 4. Acquire locks in strict numerical order
            for (int pit : uniquePits) {
                locks[pit].lock();
            }

            try {
                // 5. DOUBLE-CHECK: Did another thread change the pit while we locked?
                if (pits[startPit] != numSeeds) {
                    // State changed! Release locks and retry from the top of the while-loop
                    continue; 
                }

                // ==========================================
                // CRITICAL SECTION: We own the board logic
                // ==========================================
                pits[startPit] = 0;
                int currentPit = startPit;
                
                // Sow the seeds counter-clockwise
                for (int i = 0; i < numSeeds; i++) {
                    currentPit = (currentPit + 1) % 12;
                    pits[currentPit]++;
                }

                // Ayo Capture Logic: If the last seed makes the pit exactly 4, capture!
                if (pits[currentPit] == 4) {
                    score.addAndGet(4);
                    pits[currentPit] = 0;
                    System.out.println(">>> " + playerName + " CAPTURED 4 seeds at Pit " + currentPit + "!");
                }

                printBoardState(playerName, startPit, numSeeds);
                return true; 
                
            } finally {
                // 6. Release all locks cleanly
                for (int i = uniquePits.length - 1; i >= 0; i--) {
                    locks[uniquePits[i]].unlock();
                }
            }
        }
    }

    private void printBoardState(String player, int playedPit, int seeds) {
        System.out.println(player + " played Pit " + playedPit + " (sowed " + seeds + " seeds)");
        System.out.print("[P2] ");
        for (int i = 11; i >= 6; i--) System.out.printf("%2d ", pits[i]);
        System.out.println();
        System.out.print("[P1] ");
        for (int i = 0; i <= 5; i++) System.out.printf("%2d ", pits[i]);
        System.out.println("\n------------------------------------");
    }
}