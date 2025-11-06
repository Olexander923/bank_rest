package com.example.bankcards.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//todo еще поразмыслить, нужен или нет
public class Validator {
    private static final String USERNAME_PATTERN =
            "^(?![0-9]+$)[a-zA-Z0-9]([._ -]?[a-zA-Z0-9]){1,18}[a-zA-Z0-9]$";
    private static final Pattern pattern = Pattern.compile(USERNAME_PATTERN);

    public String loginValidator(String username) {
        while (true) {
            Matcher matcher = pattern.matcher(username);
            try {
                if (username.isBlank()) {
                    throw new IllegalArgumentException("Login cannot be empty");
                }

                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Invalid login format. " +
                            "Login must be 3-20 characters long, start and end with a letter or digit, "  +
                            "and may contain '.', '-', or '_' in the middle.");
                }
                return username;
            } catch (IllegalArgumentException e) {
                System.err.println("Error: invalid login value. Please try again.");
            }
        }
    }

}
