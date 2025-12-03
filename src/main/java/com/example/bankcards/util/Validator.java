package com.example.bankcards.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * валидация карты алгоритмом Луна и пароля
 */
public class Validator {
    //private static final String PASSWORD_PATTERN_REGEX = "((?=.*[az])(?=.*d)(?=.*[@#$%])(?=.*[AZ]).{6,16})";

     public static boolean isCardValidLuhn(String ccNumber){
         int sum = Character.getNumericValue(ccNumber.charAt(ccNumber.length() - 1));
         int parity = ccNumber.length() % 2;
         for (int i = ccNumber.length() - 2; i>=0; i--){
             int summand = Character.getNumericValue(ccNumber.charAt(i));
             if(i % 2 == parity) {
                 int product = summand * 2;
                 summand = (product > 9) ? (product - 9) : product;
             }
             sum +=summand;
         }
         return (sum % 10) == 0;
     }

//     public static boolean isValidPassword(String password){
//         Pattern pattern = Pattern.compile(PASSWORD_PATTERN_REGEX);
//
//         if (password == null || password.isEmpty())
//             return false;
//         Matcher matcher = pattern.matcher(password);
//         return matcher.matches();
//     }

     //todo добавить оценку надежности пароля или попробовать библиотеку Passay или через @ValidPassword
}
