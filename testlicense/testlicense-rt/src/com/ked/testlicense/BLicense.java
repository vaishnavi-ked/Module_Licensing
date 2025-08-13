package com.ked.testlicense;

import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@NiagaraType
public class BLicense extends BComponent {
    private static final Logger logger = Logger.getLogger(BLicense.class.getName());
    private boolean isVerified = false;  // Simple boolean field for verification status

    // Configuration
    private static final String LICENSE_DIR = "ked-licenses";
    private static final String LICENSE_FILE = "license.lic";
    private static final String PUBLIC_KEY_FILE = "public.key";

    public static final Property isValid = newProperty(Flags.READONLY | Flags.SUMMARY, BBoolean.FALSE, null);
    public static final Property validationMessage = newProperty(Flags.READONLY | Flags.SUMMARY, "", null);

    // Property getters and setters
    public boolean getIsValid() { return getBoolean(isValid); }
    public void setIsValid(boolean v) { setBoolean(isValid, v, null); }

    public String getValidationMessage() { return getString(validationMessage); }
    public void setValidationMessage(String v) { setString(validationMessage, v, null); }

    // Simple getter for isVerified
    public boolean isVerified() {
        return isVerified;
    }

    public boolean verifyLicense() {
        Path publicKeyPath = null;
        Path licensePath = null;
        String currentHostId = Sys.getHostId();

        try {
            // 1. Get the shared user home directory path
            Path sharedHome = Paths.get(Sys.getNiagaraSharedUserHome().toString());

            // 2. Create licenses directory if it doesn't exist
            Path licensesDir = sharedHome.resolve(LICENSE_DIR);
            if (!Files.exists(licensesDir)) {
                Files.createDirectories(licensesDir);
                logger.info("Created license directory at: " + licensesDir);
            }

            // 3. Define file paths
            publicKeyPath = licensesDir.resolve(PUBLIC_KEY_FILE);
            licensePath = licensesDir.resolve(LICENSE_FILE);

            // 4. Verify files exist
            if (!Files.exists(publicKeyPath)) {
                throw new SecurityException("Public key file not found at: " + publicKeyPath);
            }
            if (!Files.exists(licensePath)) {
                throw new SecurityException("License file not found at: " + licensePath);
            }

            // 5. Read and verify files
            PublicKey publicKey = loadPublicKey(publicKeyPath);
            verifyLicenseFile(licensePath, publicKey, currentHostId);

            setIsValid(true);
            isVerified = true;  // Set verified flag to true
            setValidationMessage("License verification successful");
            logger.info("✓ License verification successful");
            return true;

        } catch (AccessControlException e) {
            String message = "Security permission denied for: " +
                    (publicKeyPath != null ? publicKeyPath : "unknown file");
            logger.log(Level.SEVERE, message, e);
            setIsValid(false);
            isVerified = false;  // Set verified flag to false
            setValidationMessage(message);
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "License verification failed", e);
            setIsValid(false);
            isVerified = false;  // Set verified flag to false
            setValidationMessage("License verification failed: " + e.getMessage());
            return false;
        }
    }

    private PublicKey loadPublicKey(Path keyPath) throws Exception {
        String pubKeyPem = new String(Files.readAllBytes(keyPath), StandardCharsets.UTF_8)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] publicKeyBytes = Base64.getDecoder().decode(pubKeyPem);
        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    }

    private void verifyLicenseFile(Path licensePath, PublicKey publicKey, String currentHostId) throws Exception {
        String licenseContent = new String(Files.readAllBytes(licensePath), StandardCharsets.UTF_8);

        // Extract signature
        String signatureBase64 = licenseContent.replaceAll("(?s).*<signature>(.*?)</signature>.*", "$1");
        String licenseWithoutSig = licenseContent.replaceAll("(?s)<signature>.*?</signature>", "").trim();

        // Verify host ID
        String hostIdFromLic = licenseContent.replaceAll("(?s).*hostId=\"(.*?)\".*", "$1");
        logger.info("Current Host ID: " + currentHostId);
        logger.info("License Host ID: " + hostIdFromLic);

        if (!currentHostId.equals(hostIdFromLic)) {
            logger.severe("Host ID mismatch! Expected: " + currentHostId + " Found: " + hostIdFromLic);
            throw new SecurityException("Host ID mismatch. Expected: " + currentHostId + ", Found: " + hostIdFromLic);
        }

        // Verify signature
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(normalizeXml(licenseWithoutSig).getBytes(StandardCharsets.UTF_8));
        if (!sig.verify(Base64.getDecoder().decode(signatureBase64))) {
            throw new SecurityException("Invalid license signature");
        }
    }

    private String normalizeXml(String xml) {
        return xml.replaceAll(">\\s+<", "><").trim();
    }

    @Override
    public Type getType() { return TYPE; }
    public static final Type TYPE = Sys.loadType(BLicense.class);
}