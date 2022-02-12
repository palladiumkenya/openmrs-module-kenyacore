/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.kenyacore.etl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.ContentManager;
import org.openmrs.module.kenyacore.CoreUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ETLConfiguration manager
 * Scans through ETL extensions and build procedures for DDL, DML, and Datatool
 */
@Component
public class ETLConfigurationManager implements ContentManager {

	protected static final Log log = LogFactory.getLog(ETLConfigurationManager.class);


	private List<String> ddlProcedures = new ArrayList<String>();
	private List<String> dmlProcedures = new ArrayList<String>();
	private List<String> incrementalUpdatesProcedures = new ArrayList<String>();
	private List<String> datatoolDatabaseProcedures = new ArrayList<String>();


	/**
	 * @see org.openmrs.module.kenyacore.ContentManager#getPriority()
	 */
	@Override
	public int getPriority() {
		return 90;
	}

	/**
	 * @see org.openmrs.module.kenyacore.ContentManager#refresh()
	 */
	@Override
	public synchronized void refresh() {

		ddlProcedures.clear();
		dmlProcedures.clear();
		incrementalUpdatesProcedures.clear();
		datatoolDatabaseProcedures.clear();

		System.out.println("Preparing to execute ETL extensions ....");

		// Process ETLConfiguration beans
		for (ETLConfiguration configuration : Context.getRegisteredComponents(ETLConfiguration.class)) {
			// Register DDL procedures
			ddlProcedures.addAll(configuration.getDdlProcedures());

			// Register DDL procedures
			dmlProcedures.addAll(configuration.getDmlProcedures());

			incrementalUpdatesProcedures.addAll(configuration.getIncrementalUpdatesProcedures());

			datatoolDatabaseProcedures.addAll(configuration.getDataToolDbProcedures());	
		}

		ddlProcedures = CoreUtils.merge(ddlProcedures); // Sorts and removes duplicates
		dmlProcedures = CoreUtils.merge(dmlProcedures);
		incrementalUpdatesProcedures = CoreUtils.merge(incrementalUpdatesProcedures);
		datatoolDatabaseProcedures = CoreUtils.merge(datatoolDatabaseProcedures);

		System.out.println("DDLs: " + StringUtils.join(ddlProcedures, ","));

		ETLConfigurationProcessorOnStartup.executeETLRoutines(ddlProcedures);
		System.out.println("DMLs: " + StringUtils.join(dmlProcedures, ","));
		ETLConfigurationProcessorOnStartup.executeETLRoutines(dmlProcedures);

		System.out.println("Datatools: " + StringUtils.join(datatoolDatabaseProcedures, ","));
		ETLConfigurationProcessorOnStartup.executeETLRoutines(datatoolDatabaseProcedures);

	}

	public List<String> getDDLProcedures() {
		return ddlProcedures;
	}

	public List<String> getDMLProcedures() {
		return dmlProcedures;
	}

	public List<String> getInrementalUpdatesProcedures() {
		return incrementalUpdatesProcedures;
	}

	public List<String> getDatatoolDatabaseProcedures() {
		return datatoolDatabaseProcedures;
	}

	
}