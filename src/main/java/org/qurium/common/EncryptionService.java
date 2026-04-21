package org.qurium.common;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.qurium.common.exception.QuriumException;
import org.qurium.common.exception.QuriumExceptionCode;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@ApplicationScoped
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    @ConfigProperty(name = "qurium.encryption.secret")
    String base64Secret;

    private Key secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plainText) {

        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] ivAndCipher = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, ivAndCipher, 0, iv.length);
            System.arraycopy(encrypted, 0, ivAndCipher, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(ivAndCipher);
        } catch (Exception e) {
            throw new QuriumException(QuriumExceptionCode.UNABLE_TO_ENCRYPT);
        }
    }

    public String decrypt(String cypherText) {

        if (cypherText == null || cypherText.isEmpty()) {
            return cypherText;
        }
        try {
            byte[] ivAndCipher = Base64.getDecoder().decode(cypherText);
            byte[] iv = Arrays.copyOfRange(ivAndCipher, 0, GCM_IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(ivAndCipher, GCM_IV_LENGTH, ivAndCipher.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new QuriumException(QuriumExceptionCode.UNABLE_TO_DECRYPT);
        }
    }

}
