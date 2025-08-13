package com.ked.testlicense;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PublicKeyLoader {
    public static PublicKey loadPublicKeyFromDer(String filePath) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filePath)); // DER is raw binary
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public static void main(String[] args) throws Exception {
        // Load public key from DER file
        PublicKey publicKey = loadPublicKeyFromDer("public.der");

        // Example: verify a signature
        byte[] data = "test".getBytes();
        byte[] signatureBytes = Base64.getDecoder().decode("ykLHEv9nURo7nZaWHdbphrwDNaHJoGtQ61cJVYb7fEVRLSISfhSrFrznwZTW4pFxb/sK7EtB1awigJ55kZsk7c8CyEVXluIZeIcKFJ1cKXkh9u889UgRNNNlqCBfiO4WYCk9ZUL3RPmDnTtmHvvvnXNUn2qXhOX8Voiq2KxqpOAFmeI8854Jteja0Z1nAz1JY8ckfzAbToZrm8xg4LC5ZmW2VeIEtiO//rPpqEsitiLp5Rgdku44fEVseTs8Y30rU00Ct/iHfikAb6tragqvjgiCNthxZZe5yf0CzqtJhmfN6mqnyJJjo6XOrMh2iV4Gc4EnQRcgMAI88lATZ9CPAg==");

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data);
        boolean valid = sig.verify(signatureBytes);

        System.out.println("Signature valid? " + valid);
    }
}

