package io.github.jhanvi857.nioflow.auth;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordHasher {
    private static final int COST = 12;

    public static String hash(String plainText) {
        if (plainText == null) return null;
        return BCrypt.withDefaults().hashToString(COST, plainText.toCharArray());
    }
    
    public static boolean verify(String plainText, String hashed) {
        if (plainText == null || hashed == null) return false;
        return BCrypt.verifyer().verify(plainText.toCharArray(), hashed).verified;
    }
}
