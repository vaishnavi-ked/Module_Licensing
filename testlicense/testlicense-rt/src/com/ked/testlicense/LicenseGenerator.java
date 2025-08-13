package com.ked.testlicense;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

public class LicenseGenerator {
    private static final Logger logger = Logger.getLogger(LicenseGenerator.class.getName());

    private static final String LICENSE_PATH = "C:\\\\Niagara\\\\Niagara-4.14.0.162\\\\security\\\\license.lic";

    public static void main(String[] args) {
        try {
            // 1. Load private key from PEM
            logger.info("Loading private key...");
            String privateKeyPem = new String(Files.readAllBytes(Paths.get("private.key")), StandardCharsets.UTF_8);
            privateKeyPem = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPem);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);

            // 2. Prepare license data
            String hostId = "Win-DDA5-7041-22BE-E3C7";
            logger.info("Using Host ID: " + hostId);

            String licenseData = String.format(
                    "<license version=\"1.0\" vendor=\"Acme\" generated=\"%s\" expiration=\"never\" hostId=\"%s\">" +
                            "<feature name=\"vaishnavi\"/>" +
                            "<feature name=\"Aditya\" />" +
                            "<feature name=\"kedtechnology\" count=\"10\"/>" +
                            "</license>",
                    java.time.LocalDate.now(), hostId
            );

            // 3. Sign license
            logger.info("Signing license...");
            String normalizedLicense = normalizeXml(licenseData);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(normalizedLicense.getBytes(StandardCharsets.UTF_8));
            String signatureBase64 = Base64.getEncoder().encodeToString(signature.sign());

            // 4. Add signature tag
            String finalLicense = licenseData.replace("</license>",
                    "<signature>" + signatureBase64 + "</signature></license>");

            // 5. Save license file to Niagara licenses folder
            saveLicenseFile(LICENSE_PATH, finalLicense);
            logger.info("License generated successfully!");
        } catch (Exception e) {
            logger.severe("License generation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String normalizeXml(String xml) {
        return xml.replaceAll(">\\s+<", "><").trim();
    }

//    private static void saveLicenseFile(String path, String content) throws IOException {
//        Path filePath = Paths.get(path);
//        Files.createDirectories(filePath.getParent());
//        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
//        logger.info("Saved license to: " + filePath.toAbsolutePath());
//
//        // Remove inherited permissions and explicitly set read-only ACL
//        try {
//            String file = filePath.toAbsolutePath().toString();
//
//            // 1. Remove inherited permissions
//            new ProcessBuilder("icacls", file, "/inheritance:r")
//                    .inheritIO().start().waitFor();
//
//            // 2. Remove all existing permissions
//            new ProcessBuilder("icacls", file, "/remove:g", "Everyone")
//                    .inheritIO().start().waitFor();
//            new ProcessBuilder("icacls", file, "/remove:g", "Users")
//                    .inheritIO().start().waitFor();
//
//            // 3. Grant SYSTEM read-only
//            new ProcessBuilder("icacls", file, "/grant", "SYSTEM:R")
//                    .inheritIO().start().waitFor();
//
//            // 4. Grant Administrators read-only
//            new ProcessBuilder("icacls", file, "/grant", "Administrators:R")
//                    .inheritIO().start().waitFor();
//
//            logger.info("NTFS permissions set to read-only (no write allowed).");
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            logger.warning("Failed to set NTFS permissions: " + e.getMessage());
//        }
//    }


    private static void saveLicenseFile(String path, String content) throws IOException {
        Path filePath = Paths.get(path);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
        logger.info("Saved license to: " + filePath.toAbsolutePath());

        // Set DOS attributes (remove read-only and allow write)
        DosFileAttributeView dosView = Files.getFileAttributeView(filePath, DosFileAttributeView.class);
        if (dosView != null) {
            dosView.setReadOnly(false); // Ensure not read-only
            logger.info("Set file as writable (DOS attribute)");
        }

        // Also make sure the Java File object is writable
        File file = filePath.toFile();
        boolean writable = file.setWritable(true);
        logger.info("Set file writable: " + writable);
    }

}
