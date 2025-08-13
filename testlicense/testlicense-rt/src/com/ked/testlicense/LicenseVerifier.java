package com.ked.testlicense;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

public class LicenseVerifier {
    private static final Logger logger = Logger.getLogger(LicenseVerifier.class.getName());

    // Path to Niagara license
    private static final String LICENSE_PATH = "C:\\Niagara\\Niagara-4.14.0.162\\security\\license.lic";
    // Path to public key (DER format is safer, but PEM works too)
    private static final String PUBLIC_KEY_PATH = "public.der";

    public static void main(String[] args) {
        try {
            // 1️⃣ Load public key from DER (binary)
            logger.info("Loading public key from DER file...");
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get(PUBLIC_KEY_PATH));
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            // 2️⃣ Read license file
            logger.info("Reading license file...");
            String licenseContent = new String(Files.readAllBytes(Paths.get(LICENSE_PATH)), StandardCharsets.UTF_8);

            // 3️⃣ Extract <signature>...</signature>
            String signatureBase64 = licenseContent.replaceAll("(?s).*<signature>(.*?)</signature>.*", "$1").trim();
            logger.info("Extracted Signature (Base64): " + signatureBase64);

            // 4️⃣ Remove signature from XML for verification
            String licenseWithoutSig = licenseContent.replaceAll("(?s)<signature>.*?</signature>", "").trim();
            String normalizedLicense = normalizeXml(licenseWithoutSig);
            logger.info("Normalized License Data: " + normalizedLicense);

            // 5️⃣ Check Host ID match
            String hostIdFromLic = licenseContent.replaceAll("(?s).*hostId=\"(.*?)\".*", "$1");
            String currentHostId = "Win-DDA5-7041-22BE-E3C7"; // Replace with real system value if dynamic
            logger.info("Host ID from License: " + hostIdFromLic);
            if (!currentHostId.equals(hostIdFromLic)) {
                throw new SecurityException("Host ID mismatch: expected " + currentHostId + ", found " + hostIdFromLic);
            }

            // 6️⃣ Verify signature
            logger.info("Verifying license signature...");
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(normalizedLicense.getBytes(StandardCharsets.UTF_8));

            boolean valid = sig.verify(Base64.getDecoder().decode(signatureBase64));
            if (valid) {
                logger.info("✅ License is valid!");
            } else {
                throw new SecurityException("❌ Invalid license signature");
            }

        } catch (Exception e) {
            logger.severe("License verification failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String normalizeXml(String xml) {
        return xml.replaceAll(">\\s+<", "><").trim();
    }
}
