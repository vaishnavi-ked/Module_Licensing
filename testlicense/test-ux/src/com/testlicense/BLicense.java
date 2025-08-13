package com.testlicense;

import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@NiagaraType
public class BLicense extends BComponent {
    private static final Logger logger = Logger.getLogger(BLicense.class.getName());

    private boolean isVerified = false;

    // Config: license folder under shared user home
    private static final String LICENSE_DIR = "ked-licenses";
    private static final String LICENSE_FILE = "license.lic";

    // Public key resource inside module JAR (adjust path as needed)
    private static final String PUBLIC_KEY_RESOURCE = "/rc/key/public.der";

    // Properties - READONLY, so no setters here!
    public static final Property isValid = newProperty(Flags.READONLY | Flags.SUMMARY, BBoolean.FALSE, null);
    public static final Property validationMessage = newProperty(Flags.READONLY | Flags.SUMMARY, BString.DEFAULT, null);

    // Niagara property getters only
    public boolean getIsValid() { return getBoolean(isValid); }
    public void setIsValid(boolean v) { setBoolean(isValid, v, null); }

    public String getValidationMessage() { return getString(validationMessage); }
    public void setValidationMessage(String v) { setString(validationMessage, v, null); }

    public boolean isVerified() { return isVerified; }

    public boolean verifyLicense() {
        Path licensePath = null;
        String currentHostId = Sys.getHostId();

        try {
            // Locate license directory
            Path sharedHome = Paths.get(Sys.getNiagaraSharedUserHome().toString());
            Path licensesDir = sharedHome.resolve(LICENSE_DIR);

            if (!Files.exists(licensesDir)) {
                Files.createDirectories(licensesDir);
                logger.info("Created license directory: " + licensesDir);
            }

            // License file path
            licensePath = licensesDir.resolve(LICENSE_FILE);

            if (!Files.exists(licensePath)) {
                throw new SecurityException("License file not found: " + licensePath);
            }

            // Load public key from resource stream inside module JAR
            PublicKey publicKey = loadPublicKeyFromResource();

            // Perform signature verification and host ID check
            verifyLicenseFile(licensePath, publicKey, currentHostId);

            // Set properties internally using set()
            set(isValid, BBoolean.make(true), null);
            isVerified = true;
            set(validationMessage, BString.make("License verification successful"), null);
            logger.info("✓ License verified successfully");
            return true;

        } catch (SecurityException se) {
            logger.log(Level.SEVERE, "Security error during license verification", se);
            set(isValid, BBoolean.FALSE, null);
            isVerified = false;
            set(validationMessage, BString.make(se.getMessage()), null);
            return false;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "General error during license verification", e);
            set(isValid, BBoolean.FALSE, null);
            isVerified = false;
            set(validationMessage, BString.make("License verification failed: " + e.getMessage()), null);
            return false;
        }
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private PublicKey loadPublicKeyFromResource() throws Exception {
        logger.info("Loading public key from resource: " + PUBLIC_KEY_RESOURCE);
        try (InputStream is = getClass().getResourceAsStream(PUBLIC_KEY_RESOURCE)) {
            if (is == null) {
                throw new SecurityException("Public key resource not found: " + PUBLIC_KEY_RESOURCE);
            }
            byte[] keyBytes = readAllBytes(is);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        }
    }

    private void verifyLicenseFile(Path licensePath, PublicKey publicKey, String currentHostId) throws Exception {
        String licenseContent = new String(Files.readAllBytes(licensePath), StandardCharsets.UTF_8);

        // Extract signature base64
        String signatureBase64 = licenseContent.replaceAll("(?s).*<signature>(.*?)</signature>.*", "$1").trim();

        // Remove signature tag from license XML
        String licenseWithoutSig = licenseContent.replaceAll("(?s)<signature>.*?</signature>", "").trim();

        // Extract hostId attribute value
        String hostIdFromLic = licenseContent.replaceAll("(?s).*hostId=\"(.*?)\".*", "$1");

        logger.info("Current Host ID: " + currentHostId);
        logger.info("License Host ID: " + hostIdFromLic);

        if (!currentHostId.equals(hostIdFromLic)) {
            throw new SecurityException("Host ID mismatch. Expected: " + currentHostId + ", Found: " + hostIdFromLic);
        }

        // Verify digital signature with SHA256withRSA
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(normalizeXml(licenseWithoutSig).getBytes(StandardCharsets.UTF_8));

        if (!sig.verify(Base64.getDecoder().decode(signatureBase64))) {
            throw new SecurityException("Invalid license signature");
        }
    }

    private String normalizeXml(String xml) {
        // Remove whitespace between tags for consistent signature verification
        return xml.replaceAll(">\\s+<", "><").trim();
    }

    @Override
    public Type getType() { return TYPE; }
    public static final Type TYPE = Sys.loadType(BLicense.class);
}



//package com.testlicense;
//
//import javax.baja.nre.annotations.NiagaraType;
//import javax.baja.sys.*;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.security.AccessControlException;
//import java.security.KeyFactory;
//import java.security.PublicKey;
//import java.security.Signature;
//import java.security.spec.X509EncodedKeySpec;
//import java.util.Base64;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//@NiagaraType
//public class BLicense extends BComponent {
//    private static final Logger logger = Logger.getLogger(BLicense.class.getName());
//    private boolean isVerified = false;  // Simple boolean field for verification status
//
//    // Configuration
//    private static final String LICENSE_DIR = "ked-licenses";
//    private static final String LICENSE_FILE = "license.lic";
//    private static final String PUBLIC_KEY_FILE = "public.key";
//
//    public static final Property isValid = newProperty(Flags.READONLY | Flags.SUMMARY, BBoolean.FALSE, null);
//    public static final Property validationMessage = newProperty(Flags.READONLY | Flags.SUMMARY, "", null);
//
//    // Property getters and setters
//    public boolean getIsValid() { return getBoolean(isValid); }
//    public void setIsValid(boolean v) { setBoolean(isValid, v, null); }
//
//    public String getValidationMessage() { return getString(validationMessage); }
//    public void setValidationMessage(String v) { setString(validationMessage, v, null); }
//
//    // Simple getter for isVerified
//    public boolean isVerified() {
//        return isVerified;
//    }
//
//    public boolean verifyLicense() {
//        Path publicKeyPath = null;
//        Path licensePath = null;
//        String currentHostId = Sys.getHostId();
//
//        try {
//            // 1. Get the shared user home directory path
//            Path sharedHome = Paths.get(Sys.getNiagaraSharedUserHome().toString());
//
//            // 2. Create licenses directory if it doesn't exist
//            Path licensesDir = sharedHome.resolve(LICENSE_DIR);
//            if (!Files.exists(licensesDir)) {
//                Files.createDirectories(licensesDir);
//                logger.info("Created license directory at: " + licensesDir);
//            }
//
//            // 3. Define file paths
//            publicKeyPath = licensesDir.resolve(PUBLIC_KEY_FILE);
//            licensePath = licensesDir.resolve(LICENSE_FILE);
//
//            // 4. Verify files exist
//            if (!Files.exists(publicKeyPath)) {
//                throw new SecurityException("Public key file not found at: " + publicKeyPath);
//            }
//            if (!Files.exists(licensePath)) {
//                throw new SecurityException("License file not found at: " + licensePath);
//            }
//
//            // 5. Read and verify files
//            PublicKey publicKey = loadPublicKey(publicKeyPath);
//            verifyLicenseFile(licensePath, publicKey, currentHostId);
//
//            setIsValid(true);
//            isVerified = true;  // Set verified flag to true
//            setValidationMessage("License verification successful");
//            logger.info("✓ License verification successful");
//            return true;
//
//        } catch (AccessControlException e) {
//            String message = "Security permission denied for: " +
//                    (publicKeyPath != null ? publicKeyPath : "unknown file");
//            logger.log(Level.SEVERE, message, e);
//            setIsValid(false);
//            isVerified = false;  // Set verified flag to false
//            setValidationMessage(message);
//            return false;
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, "License verification failed", e);
//            setIsValid(false);
//            isVerified = false;  // Set verified flag to false
//            setValidationMessage("License verification failed: " + e.getMessage());
//            return false;
//        }
//    }
//
//    private PublicKey loadPublicKey(Path keyPath) throws Exception {
//        String pubKeyPem = new String(Files.readAllBytes(keyPath), StandardCharsets.UTF_8)
//                .replace("-----BEGIN PUBLIC KEY-----", "")
//                .replace("-----END PUBLIC KEY-----", "")
//                .replaceAll("\\s+", "");
//        byte[] publicKeyBytes = Base64.getDecoder().decode(pubKeyPem);
//        return KeyFactory.getInstance("RSA")
//                .generatePublic(new X509EncodedKeySpec(publicKeyBytes));
//    }
//
//    private void verifyLicenseFile(Path licensePath, PublicKey publicKey, String currentHostId) throws Exception {
//        String licenseContent = new String(Files.readAllBytes(licensePath), StandardCharsets.UTF_8);
//
//        // Extract signature
//        String signatureBase64 = licenseContent.replaceAll("(?s).*<signature>(.*?)</signature>.*", "$1");
//        String licenseWithoutSig = licenseContent.replaceAll("(?s)<signature>.*?</signature>", "").trim();
//
//        // Verify host ID
//        String hostIdFromLic = licenseContent.replaceAll("(?s).*hostId=\"(.*?)\".*", "$1");
//        logger.info("Current Host ID: " + currentHostId);
//        logger.info("License Host ID: " + hostIdFromLic);
//
//        if (!currentHostId.equals(hostIdFromLic)) {
//            logger.severe("Host ID mismatch! Expected: " + currentHostId + " Found: " + hostIdFromLic);
//            throw new SecurityException("Host ID mismatch. Expected: " + currentHostId + ", Found: " + hostIdFromLic);
//        }
//
//        // Verify signature
//        Signature sig = Signature.getInstance("SHA256withRSA");
//        sig.initVerify(publicKey);
//        sig.update(normalizeXml(licenseWithoutSig).getBytes(StandardCharsets.UTF_8));
//        if (!sig.verify(Base64.getDecoder().decode(signatureBase64))) {
//            throw new SecurityException("Invalid license signature");
//        }
//    }
//
//    private String normalizeXml(String xml) {
//        return xml.replaceAll(">\\s+<", "><").trim();
//    }
//
//    @Override
//    public Type getType() { return TYPE; }
//    public static final Type TYPE = Sys.loadType(BLicense.class);
//}