package com.example.bankcards.util;

/**
 * валидация карты алгоритмом Луна
 */
public class Validator {

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

}
