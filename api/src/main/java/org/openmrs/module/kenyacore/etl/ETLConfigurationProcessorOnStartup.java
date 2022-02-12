package org.openmrs.module.kenyacore.etl;

import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.SimpleObject;

import java.util.List;

/**
 * Executes SQL based stored procedures
 */
public class ETLConfigurationProcessorOnStartup {

    public static SimpleObject executeETLRoutines(final List<String> etlStoredProcedures) {

        final SimpleObject sampleTypeObject = new SimpleObject();
        Context.openSession();

        for (String spName : etlStoredProcedures) {
            StringBuilder sb = new StringBuilder("call ").append(spName).append("();"); // we should have something like call create_etl_tables();
            System.out.println("Currently executing query: " + sb);
            Context.getAdministrationService().executeSQL(sb.toString(), false);
        }
       // Context.closeSession();
        return sampleTypeObject;
    }
}
