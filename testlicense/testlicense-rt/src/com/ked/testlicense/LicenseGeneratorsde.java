package com.ked.testlicense;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.DosFileAttributeView;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

public class LicenseGeneratorsde {
    private static final Logger logger = Logger.getLogger(LicenseGenerator.class.getName());

    // Save license in current directory as "license.lic"
    private static final Path LICENSE_PATH = Paths.get("licenses.lic");

    public static void main(String[] args) {
        try {
            // 1. Load private key from PEM file named "private.key" in current directory
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

            // 5. Save license file to current directory
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

    private static void saveLicenseFile(Path filePath, String content) throws IOException {
        // If parent directory is null (means current directory), create current directory (no-op)
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
        logger.info("Saved license to: " + filePath.toAbsolutePath());

        // Set DOS read-only attribute on Windows if possible
        DosFileAttributeView dosView = Files.getFileAttributeView(filePath, DosFileAttributeView.class);
        if (dosView != null) {
            dosView.setReadOnly(true);
            logger.info("Set file as read-only (DOS attribute)");
        } else {
            // Fallback: use File.setWritable(false)
            File file = filePath.toFile();
            boolean writable = file.setWritable(false);
            logger.info("Set file writable: " + writable);
        }
    }



}
