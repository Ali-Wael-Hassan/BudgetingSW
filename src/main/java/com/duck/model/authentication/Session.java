package com.duck.model.authentication;

import java.io.*;

public class Session {
    private static Session instance;
    private String token;
    private static final String SESSION_FILE = "session.dat";

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void saveToken(String token) {
        try (PrintWriter out = new PrintWriter(new FileWriter(SESSION_FILE))) {
            String encryptedToken = EncryptionUtil.encrypt(token);
            out.println(encryptedToken);
            this.token = token;
        } catch (Exception e) {
            System.err.println("Failed to encrypt and save token: " + e.getMessage());
        }
    }

    public String getToken() {
        if (token == null) {
            try (BufferedReader br = new BufferedReader(new FileReader(SESSION_FILE))) {
                String encryptedLine = br.readLine();
                if (encryptedLine != null) {
                    token = EncryptionUtil.decrypt(encryptedLine);
                }
            } catch (Exception e) {
                return null;
            }
        }
        return token;
    }
}