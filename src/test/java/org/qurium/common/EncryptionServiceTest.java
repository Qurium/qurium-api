package org.qurium.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qurium.common.exception.QuriumException;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
        encryptionService.base64Secret = Base64.getEncoder().encodeToString(new byte[32]);
        encryptionService.init();
    }

    @Test
    void encrypt_thenDecrypt_returnsOriginal() {
        String plainText = "my-secret-password";
        String decrypted = encryptionService.decrypt(encryptionService.encrypt(plainText));
        assertEquals(plainText, decrypted);
    }

    @Test
    void encrypt_producesDifferentOutputEachTime() {
        String plainText = "same-input";
        assertNotEquals(encryptionService.encrypt(plainText), encryptionService.encrypt(plainText));
    }

    @Test
    void encrypt_nullInput_returnsNull() {
        assertNull(encryptionService.encrypt(null));
    }

    @Test
    void encrypt_emptyInput_returnsEmpty() {
        assertEquals("", encryptionService.encrypt(""));
    }

    @Test
    void decrypt_nullInput_returnsNull() {
        assertNull(encryptionService.decrypt(null));
    }

    @Test
    void decrypt_emptyInput_returnsEmpty() {
        assertEquals("", encryptionService.decrypt(""));
    }

    @Test
    void decrypt_tamperedCipherText_throwsQuriumException() {
        String encrypted = encryptionService.encrypt("some-text");
        String tampered = encrypted.substring(0, encrypted.length() - 4) + "ZZZZ";
        assertThrows(QuriumException.class, () -> encryptionService.decrypt(tampered));
    }

    @Test
    void encrypt_preservesSpecialCharacters() {
        String plainText = "p@$$w0rd!#%&*()";
        assertEquals(plainText, encryptionService.decrypt(encryptionService.encrypt(plainText)));
    }

    @Test
    void encrypt_preservesUnicodeCharacters() {
        String plainText = "pässwörд日本語";
        assertEquals(plainText, encryptionService.decrypt(encryptionService.encrypt(plainText)));
    }
}
