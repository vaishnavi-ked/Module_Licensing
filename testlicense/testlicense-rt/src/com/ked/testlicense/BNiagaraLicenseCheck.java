package com.ked.testlicense;

import javax.baja.nre.annotations.NiagaraType;
import javax.baja.license.*;
import javax.baja.sys.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@NiagaraType
public class BNiagaraLicenseCheck extends BComponent {
    private static final Logger logger = Logger.getLogger(BNiagaraLicenseCheck.class.getName());

    // License configuration (must match your .lic file)
    private static final String VENDOR_NAME = "Acme";
    private static final String FEATURE_NAME = "MyFeature";

    // Action definition
    public static final Action checkLicense = newAction(Flags.SUMMARY, null);

    // Type system
    public static final Type TYPE = Sys.loadType(BNiagaraLicenseCheck.class);
    @Override
    public Type getType() { return TYPE; }

    // License check implementation
    public void doCheckLicense() {
        try {
            LicenseManager lm = Sys.getLicenseManager();
            Feature feature = lm.checkFeature(VENDOR_NAME, FEATURE_NAME);

            if (feature.isExpired()) {
                logger.log(Level.SEVERE, "License EXPIRED for: " + FEATURE_NAME);
            } else {
                logger.log(Level.INFO, "License valid until: " + feature.getExpiration());
            }
        } catch (FeatureNotLicensedException e) {
            logger.log(Level.SEVERE, "Feature not licensed: " + FEATURE_NAME, e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "License check failed", e);
        }
    }
}