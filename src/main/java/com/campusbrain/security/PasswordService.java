package com.campusbrain.security;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;

@Service
public class PasswordService {

    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    public String hashPassword(String password, String saltHex) {
        byte[] salt = HexFormat.of().parseHex(saltHex);
        char[] passwordChars = password.toCharArray();
        try {
            PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public boolean verifyPassword(String password, String saltHex, String expectedHashHex) {
        String calculatedHash = hashPassword(password, saltHex);
        return calculatedHash.equalsIgnoreCase(expectedHashHex);
    }
}
