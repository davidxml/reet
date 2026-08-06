import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SecretVault vault = new SecretVault();
        
        System.out.println("Initiating system boot sequence...");
        
        // 1. Spawn background threads to simulate the startup sequence required by the Latch
        for (int i = 1; i <= 3; i++) {
            new Thread(() -> {
                try {
                    // Simulate boot time
                    Thread.sleep((long) (Math.random() * 1000) + 500);
                    vault.markSystemCheckComplete();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        // 2. Block the main thread until the background checks finish
        try {
            vault.awaitSystemReady();
        } catch (InterruptedException e) {
            System.out.println("Boot sequence interrupted.");
            return;
        }

        System.out.println("\n=== SYSTEM READY ===");
        System.out.println("Enter your 4-digit guess (or type 'exit' to quit):");
        
        // 3. Interactive Game Loop
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Shutting down system...");
                break;
            }
            
            // Basic validation
            if (input.length() != 4 || !input.matches("\\d{4}")) {
                System.out.println("Invalid input. Please enter exactly 4 digits.");
                continue;
            }
            
            // Convert the String into the int[] required by our pure domain model
            int[] guessArray = new int[4];
            for (int i = 0; i < 4; i++) {
                // Character.getNumericValue safely converts '5' to the integer 5
                guessArray[i] = Character.getNumericValue(input.charAt(i));
            }
            
            // Hit the concurrent vault
            GuessResult result = vault.attemptGuess(guessArray);
            
            // Print the result object
            System.out.println("Result: " + result.toString());
            
            if (result.dead == 4) {
                System.out.println("Congratulations! You won. A new game has started.");
            }
        }
        
        scanner.close();
    }
}