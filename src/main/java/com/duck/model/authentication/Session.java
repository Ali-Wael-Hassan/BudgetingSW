package com.duck.model.authentication;

import java.io.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Session {
    private static Session instance;
    private String token;
    private static final String SESSION_FILE = "session.dat";

    private final PropertyChangeSupport support;

    private Session() {
        support = new PropertyChangeSupport(this);
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void saveToken(String token) {
        String oldToken = this.token;
        try {
            if (token == null) {
                this.token = null;
                File f = new File(SESSION_FILE);
                if (f.exists()) f.delete();
                support.firePropertyChange("token", oldToken, null);
                return;
            }
            try (PrintWriter out = new PrintWriter(new FileWriter(SESSION_FILE))) {
                String encryptedToken = EncryptionUtil.encrypt(token);
                out.println(encryptedToken);
                this.token = token;
                support.firePropertyChange("token", oldToken, token);
            }
        } catch (Exception e) {
            System.err.println("Failed to encrypt and save token: " + e.getMessage());
        }
    }

    public String getToken() {
        if (token == null) {
            try (BufferedReader br = new BufferedReader(new FileReader(SESSION_FILE))) {
                String encryptedLine = br.readLine();
                if (encryptedLine != null) {
                    String decryptedToken = EncryptionUtil.decrypt(encryptedLine);
                    
                    String oldToken = this.token;
                    this.token = decryptedToken;
                    
                    support.firePropertyChange("token", oldToken, this.token);
                }
            } catch (Exception e) {
                return null;
            }
        }
        return token;
    }
}