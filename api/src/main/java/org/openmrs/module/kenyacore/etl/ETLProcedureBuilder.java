package org.openmrs.module.kenyacore.etl;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Populates all the DDL, DML, ScheduledUpdates, and Datatools
 */
public class ETLProcedureBuilder {


    private List<String> coreDDlProcedures = new ArrayList<String>();
    private List<String> addonDDlProcedures = new ArrayList<String>();
    private List<String> coreDMLProcedures = new ArrayList<String>();
    private List<String> addonDMLProcedures = new ArrayList<String>();
    private List<String> coreIncrementalUpdatesProcedures = new ArrayList<String>();
    private List<String> addonIncrementalUpdatesProcedures = new ArrayList<String>();
    private List<String> coreDatatoolDatabaseProcedures = new ArrayList<String>();
    private List<String> addonDatatoolDatabaseProcedures = new ArrayList<String>();

    public ETLProcedureBuilder() {

        System.out.println("Preparing to execute ETL extensions ....");
        coreDDlProcedures.clear();
        addonDDlProcedures.clear();
        coreDMLProcedures.clear();
        addonDMLProcedures.clear();
        coreIncrementalUpdatesProcedures.clear();
        addonIncrementalUpdatesProcedures.clear();
        coreDatatoolDatabaseProcedures.clear();
        addonDatatoolDatabaseProcedures.clear();

    }

    public void buildProcedures() {
        // Process ETLConfiguration beans
        for (ETLConfiguration configuration : Context.getRegisteredComponents(ETLConfiguration.class)) {
            // Register DDL procedures
            int moduleSource = configuration.getSourceModule();
            Set<String> ddlProcedures = configuration.getDdlProcedures();
            Set<String> dmlProcedures = configuration.getDmlProcedures();
            Set<String> incrementalUpdatesProcedures = configuration.getIncrementalUpdatesProcedures();
            Set<String> datatoolDbProcedures = configuration.getDataToolDbProcedures();

            if (ddlProcedures != null && !ddlProcedures.isEmpty()) {
                for (String spName : configuration.getDdlProcedures()) {
                    if (moduleSource == 1) {
                        coreDDlProcedures.add(spName);
                    } else {
                        addonDDlProcedures.add(spName);
                    }
                }
            }

            if (dmlProcedures != null && !dmlProcedures.isEmpty()) {
                for (String spName : configuration.getDmlProcedures()) {
                    if (moduleSource == 1) {
                        coreDMLProcedures.add(spName);
                    } else {
                        addonDMLProcedures.add(spName);
                    }
                }
            }

            if (incrementalUpdatesProcedures != null && !incrementalUpdatesProcedures.isEmpty()) {
                for (String spName : configuration.getIncrementalUpdatesProcedures()) {
                    if (moduleSource == 1) {
                        coreIncrementalUpdatesProcedures.add(spName);
                    } else {
                        addonIncrementalUpdatesProcedures.add(spName);
                    }
                }
            }

            if (datatoolDbProcedures != null && !datatoolDbProcedures.isEmpty()) {
                for (String spName : configuration.getDataToolDbProcedures()) {
                    if (moduleSource == 1) {
                        coreDatatoolDatabaseProcedures.add(spName);
                    } else {
                        addonDatatoolDatabaseProcedures.add(spName);
                    }
                }
            }

        }
    }

    public void runDDL() {
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("Core modules DDLs: " + StringUtils.join(coreDDlProcedures, ","));
        System.out.println("Addon modules DDLs: " + StringUtils.join(addonDDlProcedures, ","));
        System.out.println("==============================================================================");

        ETLConfigurationProcessorOnStartup.executeETLRoutines(coreDDlProcedures);
        if (!addonDDlProcedures.isEmpty()) {
            ETLConfigurationProcessorOnStartup.executeETLRoutines(addonDDlProcedures);
        }
    }

    public void runDML() {
        System.out.println("------------------------------------------------------------------------------");
        System.out.println("Core modules DMLs: " + StringUtils.join(coreDMLProcedures, ","));
        System.out.println("Addon modules DMLs: " + StringUtils.join(addonDMLProcedures, ","));
        System.out.println("==============================================================================");

        ETLConfigurationProcessorOnStartup.executeETLRoutines(coreDMLProcedures);
        if (!addonDMLProcedures.isEmpty()) {
            ETLConfigurationProcessorOnStartup.executeETLRoutines(addonDMLProcedures);
        }
    }

    public void runIncrementalUpdates() {

    }

    public void runDatatoolProcedures() {

        System.out.println("------------------------------------------------------------------------------");
        System.out.println("Core modules Datatools: " + StringUtils.join(coreDatatoolDatabaseProcedures, ","));
        System.out.println("Addon modules Datatools: " + StringUtils.join(addonDatatoolDatabaseProcedures, ","));
        System.out.println("==============================================================================");

        ETLConfigurationProcessorOnStartup.executeETLRoutines(coreDatatoolDatabaseProcedures);

        if (!addonDatatoolDatabaseProcedures.isEmpty()) {
            ETLConfigurationProcessorOnStartup.executeETLRoutines(addonDatatoolDatabaseProcedures);
        }
    }

    public List<String> getCoreDDlProcedures() {
        return coreDDlProcedures;
    }

    public void setCoreDDlProcedures(List<String> coreDDlProcedures) {
        this.coreDDlProcedures = coreDDlProcedures;
    }

    public List<String> getAddonDDlProcedures() {
        return addonDDlProcedures;
    }

    public void setAddonDDlProcedures(List<String> addonDDlProcedures) {
        this.addonDDlProcedures = addonDDlProcedures;
    }

    public List<String> getCoreDMLProcedures() {
        return coreDMLProcedures;
    }

    public void setCoreDMLProcedures(List<String> coreDMLProcedures) {
        this.coreDMLProcedures = coreDMLProcedures;
    }

    public List<String> getAddonDMLProcedures() {
        return addonDMLProcedures;
    }

    public void setAddonDMLProcedures(List<String> addonDMLProcedures) {
        this.addonDMLProcedures = addonDMLProcedures;
    }

    public List<String> getCoreIncrementalUpdatesProcedures() {
        return coreIncrementalUpdatesProcedures;
    }

    public void setCoreIncrementalUpdatesProcedures(List<String> coreIncrementalUpdatesProcedures) {
        this.coreIncrementalUpdatesProcedures = coreIncrementalUpdatesProcedures;
    }

    public List<String> getAddonIncrementalUpdatesProcedures() {
        return addonIncrementalUpdatesProcedures;
    }

    public void setAddonIncrementalUpdatesProcedures(List<String> addonIncrementalUpdatesProcedures) {
        this.addonIncrementalUpdatesProcedures = addonIncrementalUpdatesProcedures;
    }

    public List<String> getCoreDatatoolDatabaseProcedures() {
        return coreDatatoolDatabaseProcedures;
    }

    public void setCoreDatatoolDatabaseProcedures(List<String> coreDatatoolDatabaseProcedures) {
        this.coreDatatoolDatabaseProcedures = coreDatatoolDatabaseProcedures;
    }

    public List<String> getAddonDatatoolDatabaseProcedures() {
        return addonDatatoolDatabaseProcedures;
    }

    public void setAddonDatatoolDatabaseProcedures(List<String> addonDatatoolDatabaseProcedures) {
        this.addonDatatoolDatabaseProcedures = addonDatatoolDatabaseProcedures;
    }
}
