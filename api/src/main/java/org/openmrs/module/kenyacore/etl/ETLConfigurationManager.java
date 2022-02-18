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
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.ContentManager;
import org.openmrs.module.kenyacore.CoreUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * ETLConfiguration manager
 * Scans through ETL extensions and build procedures for DDL, DML, and Datatool
 */
@Component
public class ETLConfigurationManager implements ContentManager {

	protected static final Log log = LogFactory.getLog(ETLConfigurationManager.class);


	private List<String> coreDDlProcedures = new ArrayList<String>();
	private List<String> addonDDlProcedures = new ArrayList<String>();
	private List<String> coreDMLProcedures = new ArrayList<String>();
	private List<String> addonDMLProcedures = new ArrayList<String>();
	private List<String> coreIncrementalUpdatesProcedures = new ArrayList<String>();
	private List<String> addonIncrementalUpdatesProcedures = new ArrayList<String>();
	private List<String> coreDatatoolDatabaseProcedures = new ArrayList<String>();
	private List<String> addonDatatoolDatabaseProcedures = new ArrayList<String>();


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

		String recreateEtlsOnStartupConfig = Context.getAdministrationService().getGlobalProperty("kenyaemr.reacreate_etls_on_startup");

		if (recreateEtlsOnStartupConfig != null && recreateEtlsOnStartupConfig.equalsIgnoreCase("no")) {
			System.out.println("Skipping recreation of ETL tables. Please set the value of kenyaemr.reacreate_etls_on_startup global property to yes to enable recreation on startup");
			return;
		}

		coreDDlProcedures.clear();
		addonDDlProcedures.clear();
		coreDMLProcedures.clear();
		addonDMLProcedures.clear();
		coreIncrementalUpdatesProcedures.clear();
		addonIncrementalUpdatesProcedures.clear();
		coreDatatoolDatabaseProcedures.clear();
		addonDatatoolDatabaseProcedures.clear();

		System.out.println("Preparing to execute ETL extensions ....");

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
						coreIncrementalUpdatesProcedures.add(spName);
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

		//TODO: provide a way of sorting/removing duplicates
		System.out.println("------------------------------------------------------------------------------");
		System.out.println("Core modules DDLs: " + StringUtils.join(coreDDlProcedures, ","));
		System.out.println("Addon modules DDLs: " + StringUtils.join(addonDDlProcedures, ","));
		System.out.println("==============================================================================");

		ETLConfigurationProcessorOnStartup.executeETLRoutines(coreDDlProcedures);
		if (!addonDDlProcedures.isEmpty()) {
			ETLConfigurationProcessorOnStartup.executeETLRoutines(addonDDlProcedures);
		}
		System.out.println("------------------------------------------------------------------------------");
		System.out.println("Core modules DMLs: " + StringUtils.join(coreDMLProcedures, ","));
		System.out.println("Addon modules DMLs: " + StringUtils.join(addonDMLProcedures, ","));
		System.out.println("==============================================================================");

		ETLConfigurationProcessorOnStartup.executeETLRoutines(coreDMLProcedures);
		if (!addonDMLProcedures.isEmpty()) {
			ETLConfigurationProcessorOnStartup.executeETLRoutines(addonDMLProcedures);
		}

		System.out.println("------------------------------------------------------------------------------");
		System.out.println("Core modules Datatools: " + StringUtils.join(coreDatatoolDatabaseProcedures, ","));
		System.out.println("Addon modules Datatools: " + StringUtils.join(addonDatatoolDatabaseProcedures, ","));
		System.out.println("==============================================================================");

		ETLConfigurationProcessorOnStartup.executeETLRoutines(coreDatatoolDatabaseProcedures);

		if (!addonDatatoolDatabaseProcedures.isEmpty()) {
			ETLConfigurationProcessorOnStartup.executeETLRoutines(addonDatatoolDatabaseProcedures);
		}
		System.out.println("------------------------------------------------------------------------------");
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