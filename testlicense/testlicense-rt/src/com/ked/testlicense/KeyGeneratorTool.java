package com.ked.testlicense;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.logging.Logger;

public class KeyGeneratorTool {
    private static final Logger logger = Logger.getLogger(KeyGeneratorTool.class.getName());
    private static final String PEM_PRIVATE_HEADER = "-----BEGIN PRIVATE KEY-----\n";
    private static final String PEM_PRIVATE_FOOTER = "\n-----END PRIVATE KEY-----\n";
    private static final String PEM_PUBLIC_HEADER = "-----BEGIN PUBLIC KEY-----\n";
    private static final String PEM_PUBLIC_FOOTER = "\n-----END PUBLIC KEY-----\n";

    public static void main(String[] args) throws Exception {
        logger.info("Generating RSA key pair...");

        // Generate key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        // Save private key
        try (FileOutputStream fos = new FileOutputStream("private.key")) {
            String privateKeyPem = PEM_PRIVATE_HEADER +
                    Base64.getMimeEncoder().encodeToString(pair.getPrivate().getEncoded()) +
                    PEM_PRIVATE_FOOTER;
            fos.write(privateKeyPem.getBytes());
        }

        // Save public key
//        try (FileOutputStream fos = new FileOutputStream("public.key")) {
//            String publicKeyPem = PEM_PUBLIC_HEADER +
//                    Base64.getMimeEncoder().encodeToString(pair.getPublic().getEncoded()) +
//                    PEM_PUBLIC_FOOTER;
//            fos.write(publicKeyPem.getBytes());
//        }

        // Save public key in PEM (optional)
        try (FileOutputStream fos = new FileOutputStream("public.key")) {
            String publicKeyPem = PEM_PUBLIC_HEADER +
                    Base64.getMimeEncoder().encodeToString(pair.getPublic().getEncoded()) +
                    PEM_PUBLIC_FOOTER;
            fos.write(publicKeyPem.getBytes());
        }

// Save public key in DER format (binary)
        try (FileOutputStream fos = new FileOutputStream("public.der")) {
            fos.write(pair.getPublic().getEncoded()); // Direct DER-encoded bytes
        }

        // Verify key pair
        verifyKeyPair(pair);
        logger.info("Key pair generated and verified successfully");
    }

    private static void verifyKeyPair(KeyPair pair) throws Exception {
        // Test signature
        byte[] testData = "test".getBytes(StandardCharsets.UTF_8);

        // Sign with private key
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(pair.getPrivate());
        sig.update(testData);
        byte[] signature = sig.sign();

        // Verify with public key
        sig.initVerify(pair.getPublic());
        sig.update(testData);

        if (!sig.verify(signature)) {
            throw new SecurityException("Key pair verification failed");
        }
    }
}