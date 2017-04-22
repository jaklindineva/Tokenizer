package server_part;
import java.util.Random;

/**
 * @author Jaklin
 */
public class Tokenizer {

    private final String cardNumber;
    private static final int CARD_LENGTH = 16; // expected length

    //constructor
    public Tokenizer(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    //converting the string with the card number to an array of the digits
    private int[] toArray() {
        String[] chars = cardNumber.split("");
        int[] digits = new int[chars.length];
        for (int i = 0; i < chars.length; i++) {
            digits[i] = Integer.parseInt(chars[i]);
        }
        return digits;
    } // end toArray method

    //chacking if the number of the card is valid
    public boolean isValidCardNumber() {
        int sum = 0;
        int[] digits = toArray();
        if (digits.length != CARD_LENGTH) { // should contain CARD_NELGTH digits
            return false; // incorrect card length
        }        //checking if the card starts with a valid digit - 3,4,5 or 6
        if (digits[0] != 3 && digits[0] != 4 && digits[0] != 5 && digits[0] != 6) {
            return false;
        }
        //multipling every second digit from the rightmost digit moving left
        for (int i = CARD_LENGTH - 2; i >= 0; i -= 2) {
            digits[i] = digits[i] * 2;
            if (digits[i] > 10) {
                digits[i] -= 9;
            }
        }
        //taking the sum of all the digits.
        for (int i = 0; i < CARD_LENGTH; i++) {
            sum += digits[i];
        }
        //checking if  the total modulo 10 is equal to 0
        return sum % 10 == 0; // returns whether the number is valid according to the Luhn formula 

    }

    // returns the token to the card number
    public String getToken() {
        String result = null;
        int sum;
        if (isValidCardNumber()) { // checkin if the number is valid 
            int[] digits = toArray(); // converting the string with the card number to an array of the digits
            int[] token = new int[CARD_LENGTH]; // creating array for the token
            for (int i = CARD_LENGTH - 1; i > CARD_LENGTH - 5; i--) {
                token[i] = digits[i]; // the last 4 digits of the token are the same as in the card
            }
            do { // generate the first 12 digits, each one different than the one in the card at the same possition
                sum = 0;
                for (int i = 0; i < CARD_LENGTH - 4; i++) {
                    token[i] = generateRandomDigitDifferentThan(digits[i], i);
                    sum += token[i];
                }
                for (int i = CARD_LENGTH - 1; i > CARD_LENGTH - 5; i--) {
                    sum += token[i];
                }
            } while (sum % 10 == 0); // the sum of the digits of the token modulo 10 should not be equal to 0
            result = convetToString(token);
        }
        return result;
    } // end of getToken method

    //generates random digit different than the one in the paramether
    private int generateRandomDigitDifferentThan(int digit, int position) {
        Random randomDigit = new Random();
        int result;
        if (position != 0) {
            do {
                result = randomDigit.nextInt(10); // generate digit
            } while (result == digit); // check if different than the digit at the same position in the card number
            return result;
        } else {
            do {
                result = randomDigit.nextInt(10); // generate digit
            } while (result == digit || result == 3 || result == 4 || result == 5 || result == 6); // check if different
            return result;
        }
    }
    
    // converts the int array to string
    private String convetToString(int[] token) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < token.length; i++) {
            builder.append(token[i]);
        }
        return builder.toString();
    } // end convetToString method

} // end Tokenizer class
