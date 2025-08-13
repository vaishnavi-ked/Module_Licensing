package com.ked.testlicense;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.*;
import java.security.spec.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.Base64;

public class LicenseGeneratorRestrict {
    private static final Logger logger = Logger.getLogger(LicenseGenerator.class.getName());

    // Change this to your target path
    private static final String LICENSE_PATH = "C:\\\\Niagara\\\\Niagara-4.14.0.162\\\\security\\\\licenses\\\\license.lic";

    // PEM key filenames used on the generation host only -- keep private.key secure and off target machine
    private static final String PRIVATE_KEY_PEM = "private.key";
    private static final String PUBLIC_KEY_PEM = "public.pem"; // for verification tests if needed

    public static void main(String[] args) {
        try {
            logger.info("Loading private key...");
            PrivateKey privateKey = loadPrivateKeyFromPem(Paths.get(PRIVATE_KEY_PEM));

            // Prepare license data
            String hostId = "Win-DDA5-7041-22BE-E3C7";
            logger.info("Using Host ID: " + hostId);

            String licenseData = String.format(
                    "<license version=\"1.0\" vendor=\"Acme\" generated=\"%s\" expiration=\"never\" hostId=\"%s\">" +
                            "<feature name=\"vaishnavi\"/>" +
                            "<feature name=\"Aditya\"/>" +
                            "<feature name=\"kedtechnology\" count=\"10\"/>" +
                            "</license>",
                    LocalDate.now(), hostId
            );

            // Sign license
            logger.info("Signing license...");
            String normalizedLicense = normalizeXml(licenseData);
            String signatureBase64 = sign(normalizedLicense, privateKey);

            // Append signature element
            String finalLicense = licenseData.replace("</license>",
                    "<signature>" + signatureBase64 + "</signature></license>");

            // Save license file
            Path licensePath = Paths.get(LICENSE_PATH);
            saveLicenseFile(licensePath, finalLicense);

            // Set ACLs: allow read for Users (or whichever principal), full for SYSTEM (owner)
            // Replace principals below as per your environment
            try {
                restrictFileWindows(licensePath, "Users", "SYSTEM");
                logger.info("File ACLs updated using AclFileAttributeView.");
            } catch (UnsupportedOperationException uoe) {
                logger.warning("AclFileAttributeView not supported. Trying icacls fallback: " + uoe.getMessage());
                try {
                    runIcaclsFallback(licensePath);
                    logger.info("icacls fallback applied.");
                } catch (IOException ex) {
                    logger.severe("icacls fallback failed: " + ex.getMessage());
                }
            }

            logger.info("License generated and protected successfully!");
        } catch (Exception e) {
            logger.severe("License generation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // -------------------------
    // Signing helpers
    // -------------------------
    private static String sign(String normalizedXml, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(normalizedXml.getBytes(StandardCharsets.UTF_8));
        byte[] sigBytes = signature.sign();
        return Base64.getEncoder().encodeToString(sigBytes);
    }

    private static PrivateKey loadPrivateKeyFromPem(Path pemPath) throws Exception {
        String pem = new String(Files.readAllBytes(pemPath), StandardCharsets.UTF_8);
        pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    // -------------------------
    // Public key loader (for verification)
    // -------------------------
    public static PublicKey loadPublicKeyFromPem(Path pemPath) throws Exception {
        String pem = new String(Files.readAllBytes(pemPath), StandardCharsets.UTF_8);
        pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    // -------------------------
    // Verify license signature (use in Niagara module)
    // -------------------------
    public static boolean verifyLicenseSignature(String licenseXmlWithSignature, PublicKey publicKey) throws Exception {
        int sigStart = licenseXmlWithSignature.indexOf("<signature>");
        int sigEnd = licenseXmlWithSignature.indexOf("</signature>");
        if (sigStart == -1 || sigEnd == -1 || sigEnd <= sigStart) {
            logger.warning("Signature element not found or malformed.");
            return false;
        }
        String signatureBase64 = licenseXmlWithSignature.substring(sigStart + "<signature>".length(), sigEnd).trim();
        String xmlWithoutSignature = licenseXmlWithSignature.substring(0, sigStart) + "</license>";
        String normalized = normalizeXml(xmlWithoutSignature);

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(normalized.getBytes(StandardCharsets.UTF_8));
        byte[] sigBytes = Base64.getDecoder().decode(signatureBase64);
        boolean ok = sig.verify(sigBytes);
        if (!ok) logger.warning("License signature verification FAILED.");
        return ok;
    }

    // -------------------------
    // Normalization (must be identical on sign & verify)
    // -------------------------
    private static String normalizeXml(String xml) {
        // Keep this deterministic and identical in sign + verify. For production, prefer XML Canonicalization (XML DSig).
        return xml.replaceAll(">\\s+<", "><").trim();
    }

    // -------------------------
    // Save file and set DOS read only if possible
    // -------------------------
    private static void saveLicenseFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        logger.info("Saved license to: " + path.toAbsolutePath());

        try {
            DosFileAttributeView dosView = Files.getFileAttributeView(path, DosFileAttributeView.class);
            if (dosView != null) {
                dosView.setReadOnly(true);
                logger.info("Set file as read-only (DOS attribute).");
            } else {
                File file = path.toFile();
                boolean writable = file.setWritable(false);
                logger.info("Fallback: setWritable(false) returned: " + writable);
            }
        } catch (Exception e) {
            logger.warning("Could not set DOS read-only attribute: " + e.getMessage());
        }
    }

    // -------------------------
    // Windows ACL restriction using AclFileAttributeView
    // -------------------------
    private static void restrictFileWindows(Path path, String allowedReadPrincipal, String writeOwnerPrincipal) throws Exception {
        UserPrincipalLookupService lookup = path.getFileSystem().getUserPrincipalLookupService();

        UserPrincipal everyone = lookup.lookupPrincipalByName("Everyone");
        UserPrincipal allowedRead = lookup.lookupPrincipalByName(allowedReadPrincipal);
        UserPrincipal writeOwner = null;

        try {
            writeOwner = lookup.lookupPrincipalByName(writeOwnerPrincipal);
            // Only try to set owner if running elevated
            if (isRunningAsAdmin()) {
                Files.setOwner(path, writeOwner);
            } else {
                logger.warning("Skipping owner change (not running as admin).");
            }
        } catch (IOException e) {
            logger.warning("Could not change file owner: " + e.getMessage());
        }

        AclFileAttributeView aclView = Files.getFileAttributeView(path, AclFileAttributeView.class);
        if (aclView == null) {
            throw new UnsupportedOperationException("AclFileAttributeView not supported.");
        }

        List<AclEntry> acl = new ArrayList<>();

        // DENY write/delete for Everyone
        Set<AclEntryPermission> denyPerms = EnumSet.of(
                AclEntryPermission.WRITE_DATA,
                AclEntryPermission.APPEND_DATA,
                AclEntryPermission.DELETE,
                AclEntryPermission.WRITE_NAMED_ATTRS,
                AclEntryPermission.DELETE_CHILD,
                AclEntryPermission.WRITE_ATTRIBUTES
        );
        acl.add(AclEntry.newBuilder().setType(AclEntryType.DENY)
                .setPrincipal(everyone)
                .setPermissions(denyPerms).build());

        // ALLOW read for allowed principal
        Set<AclEntryPermission> readPerms = EnumSet.of(
                AclEntryPermission.READ_DATA,
                AclEntryPermission.READ_ACL,
                AclEntryPermission.READ_ATTRIBUTES,
                AclEntryPermission.READ_NAMED_ATTRS,
                AclEntryPermission.SYNCHRONIZE
        );
        acl.add(AclEntry.newBuilder().setType(AclEntryType.ALLOW)
                .setPrincipal(allowedRead)
                .setPermissions(readPerms).build());

        // ALLOW full control to owner if known
        if (writeOwner != null) {
            Set<AclEntryPermission> ownerPerms = EnumSet.allOf(AclEntryPermission.class);
            acl.add(AclEntry.newBuilder().setType(AclEntryType.ALLOW)
                    .setPrincipal(writeOwner)
                    .setPermissions(ownerPerms).build());
        }

        aclView.setAcl(acl);
    }

    private static boolean isRunningAsAdmin() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"cmd", "/c", "net session"});
            return proc.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------------
    // icacls fallback (requires Administrator)
    // -------------------------
    private static void runIcaclsFallback(Path path) throws IOException {
        // Danger: this executes platform command. Ensure path is Windows.
        String windowsPath = path.toAbsolutePath().toString();
        // Remove inheritance, grant SYSTEM full, Users read, deny Everyone write
        String[] cmd = new String[] {
                "cmd.exe", "/c",
                String.join(" && ",
                        "icacls \"" + windowsPath + "\" /inheritance:r",
                        "icacls \"" + windowsPath + "\" /grant SYSTEM:F",
                        "icacls \"" + windowsPath + "\" /grant \"Users\":R",
                        "icacls \"" + windowsPath + "\" /deny \"Everyone\":W"
                )
        };
        Process p = Runtime.getRuntime().exec(cmd);
        try {
            int rc = p.waitFor();
            if (rc != 0) {
                throw new IOException("icacls failed with exit code: " + rc);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("icacls interrupted", e);
        }
    }
}

