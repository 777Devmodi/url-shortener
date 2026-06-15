package com.urlshortener.util;

public class Base62Encoder {
    private static final String ALPHABET =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = ALPHABET.length();

    public static String encoder(long number){
        if (number == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            int remainder = (int) (number % BASE); 
            sb.append(ALPHABET.charAt(remainder));
            number /= BASE; 
        }
        //On mapping reversing the String 
        return sb.reverse().toString();
    }

    public static long decode(String code){
        long result = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            int index = ALPHABET.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid character : "+ c);
            }
            result  = result * BASE + index;
        }
        return result;
    }
}
