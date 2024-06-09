package com.quietwind01.Utils;

public class Formatting extends Placeholder {
    
    public static String formatNumber(double number) {

        // Check if the double is a whole number
        if (number == Math.floor(number)) {
            // Convert to int and then to String
            return String.valueOf((int) number);
        } else {
            // Convert to String directly
            return String.valueOf(number);
        }

    }

    public static String formatNumber(Double number) {
        // Delegate to the double method
        return formatNumber(number.doubleValue());
    }

    public static String formatNumber(int number) {
        // Convert to String directly
        return String.valueOf(number);
    }

    public static String formatNumber(Integer number) {
        // Delegate to the int method
        return formatNumber(number.intValue());
    }

}


