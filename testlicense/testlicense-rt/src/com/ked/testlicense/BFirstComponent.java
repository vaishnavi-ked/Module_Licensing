package com.ked.testlicense;

import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@NiagaraType
public class BFirstComponent extends BComponent {
    private static final Logger logger = Logger.getLogger(BFirstComponent.class.getName());
    private BLicense license;

    // Properties
    public static final Property timeExecuted = newProperty(Flags.SUMMARY, BAbsTime.DEFAULT, null);
    public static final Property alarmType = newProperty(Flags.SUMMARY, "Heating Coil", null);
    public static final Action execute = newAction(Flags.SUMMARY, null);

    @Override
    public void started() throws Exception {
        super.started();

        logger.info("Starting license verification...");
        license = new BLicense();

        try {
            if (!license.verifyLicense()) {
                String errorMsg = "License verification failed: " + license.getValidationMessage();
                logger.severe(errorMsg);
                throw new SecurityException(errorMsg);
            }
            logger.info("License verification completed successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Critical error during license verification", e);
            throw e; // Re-throw to prevent component from starting
        }
    }

    // Existing methods
    public BAbsTime getTimeExecuted() { return (BAbsTime) get(timeExecuted); }
    public void setTimeExecuted(BAbsTime v) { set(timeExecuted, v, null); }
    public String getAlarmType() { return getString(alarmType); }
    public void setAlarmType(String v) { setString(alarmType, v, null); }
    public void execute() { invoke(execute, null, null); }

    public void doExecute() {
        // Check license verification status before executing
        if (license != null && license.isVerified()) {
            logger.info("Executing action for alarm type: " + getAlarmType());
            setTimeExecuted(BAbsTime.now());
        } else {
            logger.severe("Execution blocked - License not verified");
            throw new SecurityException("Execution not allowed - License verification failed");
        }
    }

    @Override
    public Type getType() { return TYPE; }
    public static final Type TYPE = Sys.loadType(BFirstComponent.class);
}