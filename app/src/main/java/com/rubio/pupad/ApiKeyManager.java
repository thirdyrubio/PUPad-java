package com.rubio.pupad;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class ApiKeyManager {
    private static final String PREFS_NAME = "api_key_prefs";
    private static final String PREFS_KEY = "api_key";
    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static SecretKey secretKey;

    // Method to save API key securely
    public static void saveApiKey(Context context, String apiKey) {
        try {
            // Generate secret key if not already generated
            if (secretKey == null) {
                secretKey = KeyStoreUtils.getSecretKey();
            }

            // Encrypt API key
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();
            byte[] encryptedApiKey = cipher.doFinal(apiKey.getBytes());

            // Store encrypted API key in SharedPreferences
            SharedPreferences sharedPreferences = getEncryptedSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PREFS_KEY, Base64.encodeToString(iv, Base64.DEFAULT) + ":" + Base64.encodeToString(encryptedApiKey, Base64.DEFAULT));
            editor.apply();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Handle exception (e.g., log, notify user)
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Method to retrieve API key securely
    public static String getApiKey(Context context) {
        try {
            SharedPreferences sharedPreferences = getEncryptedSharedPreferences(context);
            String encryptedData = sharedPreferences.getString(PREFS_KEY, null);

            if (encryptedData == null) {
                return null;
            }

            String[] parts = encryptedData.split(":");
            byte[] iv = Base64.decode(parts[0], Base64.DEFAULT);
            byte[] encryptedApiKey = Base64.decode(parts[1], Base64.DEFAULT);

            // Decrypt API key
            if (secretKey == null) {
                secretKey = KeyStoreUtils.getSecretKey();
            }
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] decryptedApiKey = cipher.doFinal(encryptedApiKey);
            return new String(decryptedApiKey);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Handle exception (e.g., log, notify user)
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method to create EncryptedSharedPreferences
    private static SharedPreferences getEncryptedSharedPreferences(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Handle exception (e.g., log, notify user)
            return null;
        }
    }
}
