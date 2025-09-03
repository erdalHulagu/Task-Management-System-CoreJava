package com.erdal.methods;

public class Methods implements MethodService {

    private int counter = 0;
    private final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    
    
//------------------- generateUserId --------------------
    @Override
    public String generateUserId() {
        long timePart = System.currentTimeMillis();
        counter = (counter + 1) % 1000;

        long number = (timePart + counter) % 1000000; // son 6 basamak
        int firstLetterIndex = (int) (number % 26);
        char firstLetter = ALPHABET.charAt(firstLetterIndex);

        int secondLetterIndex = (int) ((number / 10) % 26);
        char secondLetter = ALPHABET.charAt(secondLetterIndex);

        return "" + firstLetter + secondLetter + String.format("%06d", number);
    }

    // Test metodu
    public void testGenerateUserId() {
        for (int i = 0; i < 5; i++) {
            System.out.println("Oluşturulan ID: " + generateUserId());
        }
    }

    public static void main(String[] args) {
        Methods m = new Methods();
        m.testGenerateUserId();
    }
}

